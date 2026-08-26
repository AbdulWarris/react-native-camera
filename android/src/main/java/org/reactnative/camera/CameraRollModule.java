package org.reactnative.camera;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.reactnative.camera.tasks.SaveToCameraRollTask;

public class CameraRollModule extends ReactContextBaseJavaModule {
  public CameraRollModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "RNCameraRoll";
  }

  @ReactMethod
  public void save(String uri, Promise promise) {
    new SaveToCameraRollTask(getReactApplicationContext(), uri, promise).execute();
  }
}
