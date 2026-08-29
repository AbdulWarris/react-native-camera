package org.reactnative.barcodedetector;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.reactnative.barcodedetector.tasks.FileBarcodeDetectionAsyncTask;

// Mirrors FaceDetectorModule — a small standalone native module for running detection
// against a static file (gallery pick), as opposed to RNBarcodeDetector's live-camera-
// frame path, which the CameraView itself drives directly.
public class BarcodeDetectorModule extends ReactContextBaseJavaModule {
  private static final String TAG = "RNBarcodeDetector";
  private static ReactApplicationContext mScopedContext;

  public BarcodeDetectorModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mScopedContext = reactContext;
  }

  @Override
  public String getName() {
    return TAG;
  }

  @ReactMethod
  public void detectBarcodes(ReadableMap options, final Promise promise) {
    new FileBarcodeDetectionAsyncTask(mScopedContext, options, promise).execute();
  }
}
