package org.reactnative.camera.tasks;

import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.cameraview.CameraView;
import com.google.mlkit.vision.face.Face;

import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.FaceDetectorUtils;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;
import org.reactnative.facedetector.RNFaceDetector;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceDetectorAsyncTask {
  private static final String TAG = "RNCamera";
  // Use a bounded pool to limit threads during sustained preview-frame analysis.
  private static final ExecutorService sExecutor = Executors.newFixedThreadPool(2);

  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNFaceDetector mFaceDetector;
  private FaceDetectorAsyncTaskDelegate mDelegate;
  private ImageDimensions mImageDimensions;
  private double mScaleX;
  private double mScaleY;
  private int mPaddingLeft;
  private int mPaddingTop;

  public FaceDetectorAsyncTask(
      FaceDetectorAsyncTaskDelegate delegate,
      RNFaceDetector faceDetector,
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
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mFaceDetector = faceDetector;
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
          Log.w(TAG, "FaceDetectorAsyncTask: delegate null, skipping");
          return;
        }
        if (mFaceDetector == null || !mFaceDetector.isOperational()) {
          Log.w(TAG, "FaceDetectorAsyncTask: detector not operational");
          mDelegate.onFaceDetectionError(mFaceDetector);
          return;
        }

        RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
        List<Face> faces;
        try {
          faces = mFaceDetector.detect(frame);
        } catch (Exception e) {
          Log.e(TAG, "FaceDetectorAsyncTask: detect() failed", e);
          mDelegate.onFaceDetectionError(mFaceDetector);
          return;
        }

        if (faces == null) {
          Log.w(TAG, "FaceDetectorAsyncTask: null result from detect()");
          mDelegate.onFaceDetectionError(mFaceDetector);
        } else {
          if (faces.size() > 0) {
            mDelegate.onFacesDetected(serializeEventData(faces));
          }
          mDelegate.onFaceDetectingTaskCompleted();
        }
      }
    });
  }

  private WritableArray serializeEventData(List<Face> faces) {
    WritableArray facesList = Arguments.createArray();

    for (int i = 0; i < faces.size(); i++) {
      Face face = faces.get(i);
      WritableMap serializedFace = FaceDetectorUtils.serializeFace(face, mScaleX, mScaleY, mWidth, mHeight, mPaddingLeft, mPaddingTop);
      if (mImageDimensions.getFacing() == CameraView.FACING_FRONT) {
        serializedFace = FaceDetectorUtils.rotateFaceX(serializedFace, mImageDimensions.getWidth(), mScaleX);
      } else {
        serializedFace = FaceDetectorUtils.changeAnglesDirection(serializedFace);
      }
      facesList.pushMap(serializedFace);
    }

    return facesList;
  }
}
