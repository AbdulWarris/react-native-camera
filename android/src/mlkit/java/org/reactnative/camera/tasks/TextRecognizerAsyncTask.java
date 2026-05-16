package org.reactnative.camera.tasks;

import android.graphics.Rect;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;

import com.google.android.cameraview.CameraView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.reactnative.camera.utils.ImageDimensions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class TextRecognizerAsyncTask {
  private static final ExecutorService sExecutor = Executors.newCachedThreadPool();
  private static final String TAG = "RNCameraView";

  private TextRecognizerAsyncTaskDelegate mDelegate;
  private ThemedReactContext mThemedReactContext;
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private double mScaleX;
  private double mScaleY;
  private ImageDimensions mImageDimensions;
  private int mPaddingLeft;
  private int mPaddingTop;
  private final AtomicBoolean mTaskCompleted = new AtomicBoolean(false);

  public TextRecognizerAsyncTask(
      TextRecognizerAsyncTaskDelegate delegate,
      ThemedReactContext themedReactContext,
      byte[] imageData,
      int width,
      int height,
      int rotation,
      float density,
      int facing,
      int viewWidth,
      int viewHeight,
      int viewPaddingLeft,
      int viewPaddingTop
  ) {
    mDelegate = delegate;
    mThemedReactContext = themedReactContext;
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mImageDimensions = new ImageDimensions(width, height, rotation, facing);
    mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
    mScaleY = (double) (viewHeight) / (mImageDimensions.getHeight() * density);
    mPaddingLeft = viewPaddingLeft;
    mPaddingTop = viewPaddingTop;
  }

  public void execute() {
    sExecutor.submit(new Runnable() {
      @Override
      public void run() {
        if (mDelegate == null) {
          Log.w(TAG, "TextRecognizerAsyncTask aborted: delegate is null");
          return;
        }
        final TextRecognizer detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        try {
          Log.d(TAG, "TextRecognizerAsyncTask start: frame=" + mWidth + "x" + mHeight + ", bytes=" + mImageData.length + ", rotation=" + mRotation);
          InputImage image;
          try {
            image = InputImage.fromByteArray(mImageData, mWidth, mHeight, getFirebaseRotation(), InputImage.IMAGE_FORMAT_NV21);
          } catch (RuntimeException nv21Error) {
            Log.w(TAG, "NV21 frame rejected by ML Kit, retrying with YV12", nv21Error);
            image = InputImage.fromByteArray(mImageData, mWidth, mHeight, getFirebaseRotation(), InputImage.IMAGE_FORMAT_YV12);
          }

          detector.process(image)
              .addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text firebaseVisionText) {
                  List<Text.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();
                  Log.d(TAG, "Text recognition success: blocks=" + textBlocks.size() + ", allTextLen=" + firebaseVisionText.getText().length());
                  try {
                    WritableArray serializedData = serializeEventData(textBlocks);
                    mDelegate.onTextRecognized(serializedData);
                  } catch (Exception dispatchError) {
                    Log.e(TAG, "Text recognition dispatch failed", dispatchError);
                  } finally {
                    notifyTaskCompletedOnce();
                  }
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                  Log.e(TAG, "Text recognition task failed", e);
                  notifyTaskCompletedOnce();
                }
              })
              .addOnCompleteListener(new OnCompleteListener<Text>() {
                @Override
                public void onComplete(Task<Text> task) {
                  Log.d(TAG, "Text recognition complete: success=" + task.isSuccessful());
                  if (!task.isSuccessful() && task.getException() != null) {
                    Log.e(TAG, "Text recognition complete with exception", task.getException());
                  }
                  detector.close();
                }
              });
        } catch (Exception e) {
          Log.e(TAG, "Failed to start text recognition task", e);
          detector.close();
          notifyTaskCompletedOnce();
        }
      }
    });
  }

  private void notifyTaskCompletedOnce() {
    if (mTaskCompleted.compareAndSet(false, true)) {
      mDelegate.onTextRecognizerTaskCompleted();
    }
  }

  private int getFirebaseRotation() {
    switch (mRotation) {
      case 0: return 0;
      case 90: return 90;
      case 180: return 180;
      case -90:
      case 270: return 270;
      default:
        Log.e(TAG, "Bad rotation value: " + mRotation);
        return 0;
    }
  }

  private WritableArray serializeEventData(List<Text.TextBlock> textBlocks) {
    WritableArray textBlocksList = Arguments.createArray();
    for (Text.TextBlock block : textBlocks) {
      WritableMap serializedTextBlock = serializeBloc(block);
      if (mImageDimensions.getFacing() == CameraView.FACING_FRONT) {
        serializedTextBlock = rotateTextX(serializedTextBlock);
      }
      textBlocksList.pushMap(serializedTextBlock);
    }
    return textBlocksList;
  }

  private WritableMap serializeBloc(Text.TextBlock block) {
    WritableMap encodedText = Arguments.createMap();
    WritableArray lines = Arguments.createArray();
    for (Text.Line line : block.getLines()) {
      lines.pushMap(serializeLine(line));
    }
    encodedText.putArray("components", lines);
    encodedText.putString("value", block.getText());
    WritableMap bounds = processBounds(block.getBoundingBox());
    encodedText.putMap("bounds", bounds);
    encodedText.putString("type", "block");
    return encodedText;
  }

  private WritableMap serializeLine(Text.Line line) {
    WritableMap encodedText = Arguments.createMap();
    WritableArray lines = Arguments.createArray();
    for (Text.Element element : line.getElements()) {
      lines.pushMap(serializeElement(element));
    }
    encodedText.putArray("components", lines);
    encodedText.putString("value", line.getText());
    WritableMap bounds = processBounds(line.getBoundingBox());
    encodedText.putMap("bounds", bounds);
    encodedText.putString("type", "line");
    return encodedText;
  }

  private WritableMap serializeElement(Text.Element element) {
    WritableMap encodedText = Arguments.createMap();
    encodedText.putString("value", element.getText());
    WritableMap bounds = processBounds(element.getBoundingBox());
    encodedText.putMap("bounds", bounds);
    encodedText.putString("type", "element");
    return encodedText;
  }

  private WritableMap processBounds(Rect frame) {
    if (frame == null) {
      WritableMap origin = Arguments.createMap();
      origin.putDouble("x", 0);
      origin.putDouble("y", 0);
      WritableMap size = Arguments.createMap();
      size.putDouble("width", 0);
      size.putDouble("height", 0);
      WritableMap bounds = Arguments.createMap();
      bounds.putMap("origin", origin);
      bounds.putMap("size", size);
      return bounds;
    }

    WritableMap origin = Arguments.createMap();
    int x = frame.left;
    int y = frame.top;
    if (frame.left < mWidth / 2) {
      x = x + mPaddingLeft / 2;
    } else if (frame.left > mWidth / 2) {
      x = x - mPaddingLeft / 2;
    }
    if (frame.top < mHeight / 2) {
      y = y + mPaddingTop / 2;
    } else if (frame.top > mHeight / 2) {
      y = y - mPaddingTop / 2;
    }
    origin.putDouble("x", x * mScaleX);
    origin.putDouble("y", y * mScaleY);
    WritableMap size = Arguments.createMap();
    size.putDouble("width", frame.width() * mScaleX);
    size.putDouble("height", frame.height() * mScaleY);
    WritableMap bounds = Arguments.createMap();
    bounds.putMap("origin", origin);
    bounds.putMap("size", size);
    return bounds;
  }

  private WritableMap rotateTextX(WritableMap text) {
    ReadableMap faceBounds = text.getMap("bounds");
    ReadableMap oldOrigin = faceBounds.getMap("origin");
    WritableMap mirroredOrigin = positionMirroredHorizontally(oldOrigin, mImageDimensions.getWidth(), mScaleX);
    double translateX = -faceBounds.getMap("size").getDouble("width");
    WritableMap translatedMirroredOrigin = positionTranslatedHorizontally(mirroredOrigin, translateX);
    WritableMap newBounds = Arguments.createMap();
    newBounds.merge(faceBounds);
    newBounds.putMap("origin", translatedMirroredOrigin);
    text.putMap("bounds", newBounds);
    ReadableArray oldComponents = text.getArray("components");
    WritableArray newComponents = Arguments.createArray();
    for (int i = 0; i < oldComponents.size(); ++i) {
      WritableMap component = Arguments.createMap();
      component.merge(oldComponents.getMap(i));
      rotateTextX(component);
      newComponents.pushMap(component);
    }
    text.putArray("components", newComponents);
    return text;
  }

  public static WritableMap positionTranslatedHorizontally(ReadableMap position, double translateX) {
    WritableMap newPosition = Arguments.createMap();
    newPosition.merge(position);
    newPosition.putDouble("x", position.getDouble("x") + translateX);
    return newPosition;
  }

  public static WritableMap positionMirroredHorizontally(ReadableMap position, int containerWidth, double scaleX) {
    WritableMap newPosition = Arguments.createMap();
    newPosition.merge(position);
    newPosition.putDouble("x", valueMirroredHorizontally(position.getDouble("x"), containerWidth, scaleX));
    return newPosition;
  }

  public static double valueMirroredHorizontally(double elementX, int containerWidth, double scaleX) {
    double originalX = elementX / scaleX;
    double mirroredX = containerWidth - originalX;
    return mirroredX * scaleX;
  }
}
