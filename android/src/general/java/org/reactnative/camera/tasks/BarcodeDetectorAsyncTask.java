package org.reactnative.camera.tasks;

import android.graphics.Rect;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.mlkit.vision.barcode.Barcode;

import org.reactnative.barcodedetector.BarcodeFormatUtils;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;
import org.reactnative.barcodedetector.RNBarcodeDetector;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeDetectorAsyncTask {
  private static final String TAG = "RNCamera";
  private static final ExecutorService sExecutor = Executors.newCachedThreadPool();

  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNBarcodeDetector mBarcodeDetector;
  private BarcodeDetectorAsyncTaskDelegate mDelegate;
  private double mScaleX;
  private double mScaleY;
  private ImageDimensions mImageDimensions;
  private int mPaddingLeft;
  private int mPaddingTop;

  public BarcodeDetectorAsyncTask(
          BarcodeDetectorAsyncTaskDelegate delegate,
          RNBarcodeDetector barcodeDetector,
          byte[] imageData,
          int width,
          int height,
          int rotation,
          float density,
          int facing,
          int viewWidth,
          int viewHeight,
          int viewPaddingLeft,
          int viewPaddingTop) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mBarcodeDetector = barcodeDetector;
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
        final BarcodeDetectorAsyncTaskDelegate delegate = mDelegate;
        final RNBarcodeDetector barcodeDetector = mBarcodeDetector;

        if (delegate == null) {
          Log.w(TAG, "BarcodeDetectorAsyncTask: delegate null");
          return;
        }

        if (barcodeDetector == null || !barcodeDetector.isOperational()) {
          Log.w(TAG, "BarcodeDetectorAsyncTask: detector not operational");
          delegate.onBarcodeDetectionError(barcodeDetector);
          return;
        }

        RNFrame frame = RNFrameFactory.buildFrame(mImageData, mWidth, mHeight, mRotation);
        List<Barcode> barcodes;
        try {
          barcodes = barcodeDetector.detect(frame);
        } catch (Exception e) {
          Log.e(TAG, "BarcodeDetectorAsyncTask: detect() failed", e);
          delegate.onBarcodeDetectionError(barcodeDetector);
          return;
        }

        if (barcodes == null) {
          Log.w(TAG, "BarcodeDetectorAsyncTask: null result from detect()");
          delegate.onBarcodeDetectionError(barcodeDetector);
        } else {
          if (barcodes.size() > 0) {
            delegate.onBarcodesDetected(serializeEventData(barcodes), mWidth, mHeight, mImageData);
          }
          delegate.onBarcodeDetectingTaskCompleted();
        }
      }
    });
  }

  private WritableArray serializeEventData(List<Barcode> barcodes) {
    WritableArray barcodesList = Arguments.createArray();
    for (int i = 0; i < barcodes.size(); i++) {
      Barcode barcode = barcodes.get(i);
      WritableMap serializedBarcode = Arguments.createMap();
      serializedBarcode.putString("data", barcode.getDisplayValue());
      serializedBarcode.putString("rawData", barcode.getRawValue());
      serializedBarcode.putString("type", BarcodeFormatUtils.get(barcode.getFormat()));
      serializedBarcode.putMap("bounds", processBounds(barcode.getBoundingBox()));
      barcodesList.pushMap(serializedBarcode);
    }
    return barcodesList;
  }

  private WritableMap processBounds(Rect frame) {
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
}
