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
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.text.TextRecognition;

import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TextRecognizerAsyncTask {
  private static final ExecutorService sExecutor = Executors.newCachedThreadPool();
  private static final String TAG = "RNCamera";

  private TextRecognizerAsyncTaskDelegate mDelegate;
  private ThemedReactContext mThemedReactContext;
  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private ImageDimensions mImageDimensions;
  private double mScaleX;
  private double mScaleY;
  private int mPaddingLeft;
  private int mPaddingTop;

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
          return;
        }
        final TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
        textRecognizer.process(frame.getFrame())
            .addOnSuccessListener(new OnSuccessListener<Text>() {
              @Override
              public void onSuccess(Text text) {
                WritableArray textBlocksList = Arguments.createArray();
                for (TextBlock textBlock : text.getTextBlocks()) {
                  WritableMap serializedTextBlock = serializeText(textBlock);
                  if (mImageDimensions.getFacing() == CameraView.FACING_FRONT) {
                    serializedTextBlock = rotateTextX(serializedTextBlock);
                  }
                  textBlocksList.pushMap(serializedTextBlock);
                }
                mDelegate.onTextRecognized(textBlocksList);
                mDelegate.onTextRecognizerTaskCompleted();
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(Exception e) {
                Log.e(TAG, "Text recognition task failed", e);
                mDelegate.onTextRecognizerTaskCompleted();
              }
            })
            .addOnCompleteListener(new OnCompleteListener<Text>() {
              @Override
              public void onComplete(Task<Text> task) {
                textRecognizer.close();
              }
            });
      }
    });
  }

  private WritableMap serializeText(TextBlock text) {
    WritableMap encodedText = Arguments.createMap();
    WritableArray components = Arguments.createArray();
    for (Line component : text.getLines()) {
      components.pushMap(serializeText(component));
    }
    encodedText.putArray("components", components);
    encodedText.putString("value", text.getText());
    WritableMap bounds = serializeBounds(text.getBoundingBox());
    encodedText.putMap("bounds", bounds);
    encodedText.putString("type", "block");
    return encodedText;
  }

  private WritableMap serializeText(Line text) {
    WritableMap encodedText = Arguments.createMap();
    WritableArray components = Arguments.createArray();
    for (Text.Element component : text.getElements()) {
      components.pushMap(serializeText(component));
    }
    encodedText.putArray("components", components);
    encodedText.putString("value", text.getText());
    WritableMap bounds = serializeBounds(text.getBoundingBox());
    encodedText.putMap("bounds", bounds);
    encodedText.putString("type", "line");
    return encodedText;
  }

  private WritableMap serializeText(Text.Element text) {
    WritableMap encodedText = Arguments.createMap();
    WritableArray components = Arguments.createArray();
    encodedText.putArray("components", components);
    encodedText.putString("value", text.getText());
    WritableMap bounds = serializeBounds(text.getBoundingBox());
    encodedText.putMap("bounds", bounds);
    encodedText.putString("type", "element");
    return encodedText;
  }

  private WritableMap serializeBounds(Rect boundingBox) {
    int x = boundingBox.left;
    int y = boundingBox.top;
    int width = boundingBox.width();
    int height = boundingBox.height();
    if (x < mWidth / 2) {
      x = x + mPaddingLeft / 2;
    } else if (x > mWidth / 2) {
      x = x - mPaddingLeft / 2;
    }
    if (height < mHeight / 2) {
      y = y + mPaddingTop / 2;
    } else if (height > mHeight / 2) {
      y = y - mPaddingTop / 2;
    }
    WritableMap origin = Arguments.createMap();
    origin.putDouble("x", x * this.mScaleX);
    origin.putDouble("y", y * this.mScaleY);
    WritableMap size = Arguments.createMap();
    size.putDouble("width", width * this.mScaleX);
    size.putDouble("height", height * this.mScaleY);
    WritableMap bounds = Arguments.createMap();
    bounds.putMap("origin", origin);
    bounds.putMap("size", size);
    return bounds;
  }

  private WritableMap rotateTextX(WritableMap text) {
    ReadableMap faceBounds = text.getMap("bounds");
    ReadableMap oldOrigin = faceBounds.getMap("origin");
    WritableMap mirroredOrigin = FaceDetectorUtils.positionMirroredHorizontally(oldOrigin, mImageDimensions.getWidth(), mScaleX);
    double translateX = -faceBounds.getMap("size").getDouble("width");
    WritableMap translatedMirroredOrigin = FaceDetectorUtils.positionTranslatedHorizontally(mirroredOrigin, translateX);
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
}
