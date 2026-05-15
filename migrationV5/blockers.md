# Blockers — RN 0.81 Migration

> Status legend: 🔴 Not Started · 🟡 In Progress · 🟢 Done · ⛔ Blocked

---

## B1 — `ViewPropTypes` removed from `react-native`

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🔴 CRITICAL — crashes app at JS startup |
| **Affected files** | `src/RNCamera.js:8` (import), `src/RNCamera.js:397` (spread into propTypes) |
| **RN version removed** | 0.68 |
| **Effort** | 1–2 hours |

**Problem:**  
```js
// src/RNCamera.js:8
import { ..., ViewPropTypes, ... } from 'react-native';

// src/RNCamera.js:397
static propTypes = {
  ...ViewPropTypes,   // crashes — ViewPropTypes is undefined
  ...
}
```

**Fix:**  
Option A — Install `deprecated-react-native-prop-types` and update the import:
```js
import { ViewPropTypes } from 'deprecated-react-native-prop-types';
```
Option B — Remove PropTypes entirely and rely on TypeScript types in `types/index.d.ts`.

---

## B2 — `requireNativeComponent` three-argument form removed

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🔴 CRITICAL — component fails to register |
| **Affected files** | `src/RNCamera.js:921` |
| **RN version removed** | 0.70 |
| **Effort** | 0.5 hours |

**Problem:**  
```js
// src/RNCamera.js:921
const RNCamera = requireNativeComponent('RNCamera', Camera, { nativeOnly: { ... } });
```
The three-argument form is no longer accepted.

**Fix:**  
```js
const RNCamera = requireNativeComponent('RNCamera');
```

---

## B3 — `UIManagerModule.getEventDispatcher()` removed (13 call sites)

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🔴 CRITICAL — all native→JS events silently fail or throw |
| **Affected files** | `android/src/main/java/org/reactnative/camera/RNCameraViewHelper.java` (lines 173, 187, 201, 216, 230, 242, 254, 268, 280, 294, 306, 319, 332) |
| **Also affects** | All 12 Event classes (see list below) |
| **RN version removed** | ~0.76 |
| **Effort** | 3–5 days |

**Problem:**  
Every event dispatch in `RNCameraViewHelper` follows this pattern:
```java
reactContext.getNativeModule(UIManagerModule.class)
    .getEventDispatcher()
    .dispatchEvent(event);
```
`UIManagerModule.getEventDispatcher()` no longer exists.

**Events affected (12 classes):**
- `FacesDetectedEvent` — face detection
- `FaceDetectionErrorEvent` — face detection errors
- `TextRecognizedEvent` — OCR results
- `BarCodeReadEvent` — ZXing barcode
- `BarcodesDetectedEvent` — MLKit barcode
- `BarcodeDetectionErrorEvent` — MLKit barcode errors
- `CameraReadyEvent` — camera mount
- `CameraMountErrorEvent` — mount errors
- `PictureTakenEvent` — photo shutter
- `PictureSavedEvent` — photo saved
- `RecordingStartEvent` / `RecordingEndEvent` — video lifecycle
- `TouchEvent` — tap/double-tap

**Fix:**  
Replace with `UIManagerHelper`:
```java
// Replace old pattern:
reactContext.getNativeModule(UIManagerModule.class)
    .getEventDispatcher()
    .dispatchEvent(event);

// With new pattern:
int surfaceId = UIManagerHelper.getSurfaceId(view);
EventDispatcher dispatcher = UIManagerHelper.getEventDispatcherForContext(
    reactContext, surfaceId);
if (dispatcher != null) {
    dispatcher.dispatchEvent(event);
}
```

Also update all 12 Event classes to implement `dispatchModern()` alongside `dispatch()`:
```java
@Override
public void dispatch(RCTEventEmitter rctEventEmitter) {
    rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
}

@Override
public void dispatchModern(RCTEventEmitter2 eventEmitter) {
    eventEmitter.receiveEvent(getSurfaceId(), getViewTag(), getEventName(), serializeEventData());
}
```

---

## B4 — `android.os.AsyncTask` deprecated/removed (8 files)

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🟠 HIGH — compile error when compileSdk ≥ 33 |
| **Affected files** | See list below |
| **API deprecated** | API 30; removed from SDK API 33 |
| **Effort** | 3–4 days |

**Files using `AsyncTask`:**
| File | Usage |
|---|---|
| `android/src/main/java/org/reactnative/camera/RNCameraView.java` | Inline `executeOnExecutor` calls |
| `android/src/main/java/org/reactnative/camera/CameraModule.java` | `GuardedAsyncTask` |
| `android/src/main/java/org/reactnative/camera/tasks/ResolveTakenPictureAsyncTask.java` | Extends `AsyncTask` |
| `android/src/main/java/org/reactnative/camera/tasks/BarCodeScannerAsyncTask.java` | Extends `AsyncTask` |
| `android/src/general/java/…/tasks/FaceDetectorAsyncTask.java` | Extends `AsyncTask` |
| `android/src/mlkit/java/…/tasks/FaceDetectorAsyncTask.java` | Extends `AsyncTask` |
| `android/src/general/java/…/tasks/TextRecognizerAsyncTask.java` | Extends `AsyncTask` |
| `android/src/mlkit/java/…/tasks/TextRecognizerAsyncTask.java` | Extends `AsyncTask` |
| `android/src/general/java/…/tasks/BarcodeDetectorAsyncTask.java` | Extends `AsyncTask` |
| `android/src/mlkit/java/…/tasks/BarcodeDetectorAsyncTask.java` | Extends `AsyncTask` |
| `android/src/main/java/com/lwansbrough/…/RCTCameraModule.java` | Legacy Camera1 stack |
| `android/src/main/java/com/lwansbrough/…/RCTCameraViewFinder.java` | Legacy Camera1 stack |

**Fix:**  
Replace with `ExecutorService`:
```java
private static final ExecutorService sExecutor =
    Executors.newSingleThreadExecutor();

// In place of .execute() / .executeOnExecutor(THREAD_POOL_EXECUTOR)
sExecutor.submit(() -> {
    // doInBackground logic
    new Handler(Looper.getMainLooper()).post(() -> {
        // onPostExecute logic
    });
});
```

---

## B5 — Android build toolchain far behind RN 0.77 requirements

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🟠 HIGH — build fails entirely |
| **Affected files** | `android/build.gradle` |
| **Effort** | 2–3 days (including downstream breakages) |

**Required changes:**

| Setting | Current | Required for RN 0.77+ |
|---|---|---|
| AGP | `4.1.0` | `8.3.0+` |
| compileSdk | `29` | `35` |
| buildTools | `29.0.2` | `34.0.0` / `35.0.0` |
| targetSdk | `29` | `34` (Play Store minimum Aug 2024) |
| minSdk | `16` | `24` (RN 0.77 requirement) |

---

## B6 — `jcenter()` repository sunset

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🟠 HIGH — build may fail to resolve dependencies |
| **Affected files** | `android/build.gradle:13,59` |
| **Effort** | 2–4 hours |

**Problem:**  
`jcenter()` is referenced in both `buildscript` and `allprojects` repository blocks. JCenter was shut down on May 1, 2021 — packages may be unavailable.

**Fix:**  
Remove all `jcenter()` lines. `mavenCentral()` (already present) is the replacement. Verify that `com.drewnoakes:metadata-extractor` and all other deps resolve from Maven Central.

---

## B7 — `AVCaptureStillImageOutput` removed in Xcode 16 / iOS 18 SDK

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🔴 CRITICAL — iOS build fails with Xcode 16 |
| **Affected files** | `ios/RN/RNCamera.h:22`, `ios/RN/RNCamera.m:774,790,1340`, `ios/RCT/RCTCameraManager.m:483,646,649`, `ios/RCT/RCTCameraManager.h:66` |
| **Deprecated** | iOS 10; **removed** in iOS 18 SDK (Xcode 16) |
| **Effort** | 2–3 days |

**Problem:**  
```objc
// RNCamera.h:22
@property(nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;

// RNCamera.m:774
[self.stillImageOutput captureStillImageAsynchronouslyFromConnection:connection
    completionHandler:^(CMSampleBufferRef imageSampleBuffer, NSError *error) {
        // RNCamera.m:790
        NSData *imageData = [AVCaptureStillImageOutput
            jpegStillImageNSDataRepresentation:imageSampleBuffer];
    }];
```

**Fix:**  
Replace `AVCaptureStillImageOutput` with `AVCapturePhotoOutput` and implement `AVCapturePhotoCaptureDelegate`:
```objc
// New property
@property(nonatomic, strong) AVCapturePhotoOutput *photoOutput;

// Capture call
AVCapturePhotoSettings *settings = [AVCapturePhotoSettings
    photoSettingsWithFormat:@{AVVideoCodecKey: AVVideoCodecTypeJPEG}];
[self.photoOutput capturePhotoWithSettings:settings delegate:self];

// Delegate callback
- (void)captureOutput:(AVCapturePhotoOutput *)output
didFinishProcessingPhoto:(AVCapturePhoto *)photo
                error:(NSError *)error {
    NSData *imageData = [photo fileDataRepresentation];
    // ...
}
```

---

## B8 — Bundled MLKit `mlkit` flavor dependencies outdated

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🟡 MEDIUM — potential 16KB page-size non-compliance; missing bug fixes |
| **Affected files** | `android/build.gradle:79-81` |
| **Effort** | 2–4 hours |

**Current vs required:**

| Dependency | Current | Recommended |
|---|---|---|
| `com.google.mlkit:barcode-scanning` | `16.2.0` | `17.3.0` |
| `com.google.mlkit:face-detection` | `16.1.2` | `16.1.7` |
| `com.google.android.gms:play-services-mlkit-text-recognition` | `16.3.0` | `19.0.1` |
| `com.google.android.gms:play-services-mlkit-barcode-scanning` | `16.2.0` | `18.3.1` |
| `com.google.android.gms:play-services-mlkit-face-detection` | `16.2.0` | `17.1.0` |

---

## B9 — iOS `initWithBridge:` incompatible with new architecture

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🟠 HIGH — `self.bridge` may be `nil` when new arch is enabled |
| **Affected files** | `ios/RN/RNCamera.m:58`, `ios/RN/RNCameraManager.m:40`, `ios/RN/RNCamera.h:63` |
| **Effort** | 1–2 days |

**Problem:**  
The view is exclusively initialised through:
```objc
- (id)initWithBridge:(RCTBridge *)bridge { ... }
```
Under the new architecture (Fabric), `RCTViewManager` uses `initWithFrame:` and the bridge reference may be `nil`.

**Fix:**  
Add `initWithFrame:` initializer; access `self.bridge` conditionally and defer any bridge-dependent setup to when the view is attached:
```objc
- (instancetype)initWithFrame:(CGRect)frame {
    if ((self = [super initWithFrame:frame])) {
        // bridge-independent setup
    }
    return self;
}

- (id)initWithBridge:(RCTBridge *)bridge {
    if ((self = [self initWithFrame:CGRectZero])) {
        self.bridge = bridge;
        // bridge-dependent setup
    }
    return self;
}
```

---

## B10 — Synchronous `Task.getResult()` in Android MLKit calls

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🟡 MEDIUM — throws `RuntimeExecutionException` on any MLKit error instead of graceful failure |
| **Affected files** | `android/src/general/java/…/TextRecognizerAsyncTask.java:77`, `android/src/mlkit/java/…/TextRecognizerAsyncTask.java:77`, `android/src/general/java/…/facedetector/RNFaceDetector.java:62`, `android/src/mlkit/java/…/facedetector/RNFaceDetector.java:62` |
| **Effort** | 1–2 days |

**Problem:**  
```java
// TextRecognizerAsyncTask.java:77
return mTextRecognizer.process(frame.getFrame()).getResult().getTextBlocks();

// RNFaceDetector.java:62
return mFaceDetector.process(frame.getFrame()).getResult();
```
`Task.getResult()` is synchronous and throws if the task has failed.

**Fix:**  
Restructure async tasks to use callbacks instead of synchronous returns. Use `Tasks.await()` (acceptable within a background thread) or convert the entire task to a callback-based design with `addOnSuccessListener`/`addOnFailureListener`.

---

## B11 — `android.hardware.Camera` (Camera1) legacy stack

| Field | Value |
|---|---|
| **Status** | 🔴 Not Started |
| **Severity** | 🟢 LOW — still compiles, but deprecated since API 21 |
| **Affected files** | `android/src/main/java/com/lwansbrough/RCTCamera/RCTCamera.java`, `RCTCameraModule.java`, `RCTCameraUtils.java`, `RCTCameraViewFinder.java` |
| **Effort** | 1 day |

**Problem:**  
The entire `com.lwansbrough.RCTCamera` package uses the deprecated Camera1 API (`android.hardware.Camera`). It is registered alongside the modern Camera2 stack in `RNCameraPackage.java`. With minSdk raised to 24, Camera1 is still technically available but Google hides it from the SDK at API 30+.

**Recommended action:**  
Deprecate or remove the entire `com.lwansbrough.RCTCamera` package. Update `RNCameraPackage.java` to no longer register `RCTCameraModule` and `RCTCameraViewManager`.
