package org.reactnative.camera;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import org.reactnative.facedetector.FaceDetectorModule;

import java.util.Arrays;
import java.util.List;

public class RNCameraPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
        return Arrays.<NativeModule>asList(
                new CameraModule(reactApplicationContext),
                new FaceDetectorModule(reactApplicationContext)
        );
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        return Arrays.<ViewManager>asList(new CameraViewManager());
    }
}
