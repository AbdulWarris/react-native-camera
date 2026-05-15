# Migration Plan — RN 0.81 (v5)

> Status legend: 🔴 Not Started · 🟡 In Progress · 🟢 Done · ⛔ Blocked  
> Each phase must pass before the next begins. Phases 3–4 (Android) and 5–6 (iOS) can be parallelised by separate developers.

---

## Phase 0 — Preparation & Baseline

> **Status: 🔴 Not Started** | Estimated: 1 day

- [ ] Create and freeze `compat/rn-0.81` branch from current `main`
- [ ] Set up three CI configurations: RN 0.63 (baseline), RN 0.77 (intermediate), RN 0.81 (target)
- [ ] Run example apps on RN 0.63 — document passing/failing tests as baseline
- [ ] Set up Android emulator with 16KB page-size emulation (AVD with `ro.dalvik.vm.native.bridge.16k_pages=1`)
- [ ] Ensure Xcode 16 is available in iOS CI lane

---

## Phase 1 — JS Layer Fixes

> **Status: 🔴 Not Started** | Estimated: 1–2 days  
> Blockers addressed: [B1](./blockers.md#b1--viewproptypes-removed-from-react-native), [B2](./blockers.md#b2--requirenativecomponent-three-argument-form-removed)

- [ ] **B1** — Remove `ViewPropTypes` import from `src/RNCamera.js:8`
  - [ ] Install `deprecated-react-native-prop-types` OR remove PropTypes spread at line 397
  - [ ] Verify app no longer crashes on import in RN 0.68+ example
- [ ] **B2** — Remove third argument from `requireNativeComponent` call at `src/RNCamera.js:921`
  - [ ] Change to `requireNativeComponent('RNCamera')`
- [ ] Update `package.json` `peerDependencies` to cover RN 0.63 – 0.81
- [ ] Run JS type-check and linter (`yarn lint`)
- [ ] Smoke test JS layer in RN 0.77 example app

---

## Phase 2 — Android Build Toolchain

> **Status: 🔴 Not Started** | Estimated: 2–3 days  
> Blockers addressed: [B5](./blockers.md#b5--android-build-toolchain-far-behind-rn-077-requirements), [B6](./blockers.md#b6--jcenter-repository-sunset), [B8](./blockers.md#b8--bundled-mlkit-mlkit-flavor-dependencies-outdated)

- [ ] **B5** — Update `android/build.gradle`:
  - [ ] AGP: `4.1.0` → `8.3.0`
  - [ ] `compileSdkVersion`: `29` → `35`
  - [ ] `buildToolsVersion`: `29.0.2` → `34.0.0`
  - [ ] `targetSdkVersion`: `29` → `34`
  - [ ] `minSdkVersion`: `16` → `24`
- [ ] **B6** — Remove all `jcenter()` references from `android/build.gradle` (lines 13 and 59)
  - [ ] Verify `com.drewnoakes:metadata-extractor` resolves from Maven Central
- [ ] **B8** — Update MLKit dependency versions in `android/build.gradle`:
  - [ ] `com.google.mlkit:barcode-scanning`: `16.2.0` → `17.3.0`
  - [ ] `com.google.mlkit:face-detection`: `16.1.2` → `16.1.7`
  - [ ] `com.google.android.gms:play-services-mlkit-text-recognition`: `16.3.0` → `19.0.1`
  - [ ] `com.google.android.gms:play-services-mlkit-barcode-scanning`: `16.2.0` → `18.3.1`
  - [ ] `com.google.android.gms:play-services-mlkit-face-detection`: `16.2.0` → `17.1.0`
- [ ] Confirm `./gradlew assembleGeneralDebug` and `./gradlew assembleMlkitDebug` succeed
- [ ] Run 16KB page-size check on any `.so` files in `mlkit` flavor AAR outputs

---

## Phase 3 — Android `AsyncTask` Removal

> **Status: 🔴 Not Started** | Estimated: 3–4 days  
> Blockers addressed: [B4](./blockers.md#b4--androidosasyntask-deprecatedremoved-8-files), [B10](./blockers.md#b10--synchronous-taskgetresult-in-android-mlkit-calls)

- [ ] **B4** — Replace `AsyncTask` with `ExecutorService` pattern in all task files:
  - [ ] `android/src/main/java/org/reactnative/camera/tasks/ResolveTakenPictureAsyncTask.java`
  - [ ] `android/src/main/java/org/reactnative/camera/tasks/BarCodeScannerAsyncTask.java`
  - [ ] `android/src/general/java/…/tasks/FaceDetectorAsyncTask.java`
  - [ ] `android/src/mlkit/java/…/tasks/FaceDetectorAsyncTask.java`
  - [ ] `android/src/general/java/…/tasks/TextRecognizerAsyncTask.java`
  - [ ] `android/src/mlkit/java/…/tasks/TextRecognizerAsyncTask.java`
  - [ ] `android/src/general/java/…/tasks/BarcodeDetectorAsyncTask.java`
  - [ ] `android/src/mlkit/java/…/tasks/BarcodeDetectorAsyncTask.java`
  - [ ] `android/src/general/java/…/facedetector/tasks/FileFaceDetectionAsyncTask.java`
  - [ ] `android/src/mlkit/java/…/facedetector/tasks/FileFaceDetectionAsyncTask.java`
  - [ ] Inline `executeOnExecutor` calls in `RNCameraView.java`
  - [ ] `GuardedAsyncTask` in `CameraModule.java`
  - [ ] Legacy Camera1 stack: `RCTCameraModule.java`, `RCTCameraViewFinder.java` (or defer if B11 removes this code)
- [ ] **B10** — Convert synchronous `Task.getResult()` to async callbacks:
  - [ ] `TextRecognizerAsyncTask.java` (both flavors) — use `Tasks.await()` on background thread or callback pattern
  - [ ] `RNFaceDetector.java` (both flavors) — wrap `process().getResult()` in `Tasks.await()` with try/catch
- [ ] **B11** (optional, recommended) — Remove or deprecate `com.lwansbrough.RCTCamera` legacy Camera1 package:
  - [ ] Remove `RCTCameraModule` and `RCTCameraViewManager` registrations from `RNCameraPackage.java`
  - [ ] Delete or archive `android/src/main/java/com/lwansbrough/` directory
- [ ] Confirm both flavor builds succeed and no `AsyncTask` imports remain

---

## Phase 4 — Android Event System

> **Status: 🔴 Not Started** | Estimated: 3–5 days  
> Blockers addressed: [B3](./blockers.md#b3--uimanagermodulegeteventsidpatcher-removed-13-call-sites)

- [ ] **B3** — Update `RNCameraViewHelper.java` (13 call sites):
  - [ ] Replace all `reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(event)` with `UIManagerHelper` pattern
  - [ ] Add null-safety guard around `dispatcher` reference
- [ ] Update all 12 Event classes to support both old-arch and new-arch dispatch:
  - [ ] `FacesDetectedEvent` — add `dispatchModern()`
  - [ ] `FaceDetectionErrorEvent` — add `dispatchModern()`
  - [ ] `TextRecognizedEvent` — add `dispatchModern()`
  - [ ] `BarCodeReadEvent` — add `dispatchModern()`
  - [ ] `BarcodesDetectedEvent` — add `dispatchModern()`
  - [ ] `BarcodeDetectionErrorEvent` — add `dispatchModern()`
  - [ ] `CameraReadyEvent` — add `dispatchModern()`
  - [ ] `CameraMountErrorEvent` — add `dispatchModern()`
  - [ ] `PictureTakenEvent` — add `dispatchModern()`
  - [ ] `PictureSavedEvent` — add `dispatchModern()`
  - [ ] `RecordingStartEvent` / `RecordingEndEvent` — add `dispatchModern()`
  - [ ] `TouchEvent` — add `dispatchModern()`
- [ ] Test: verify `onFacesDetected`, `onTextRecognized`, `onBarCodeRead` fire in old-arch example
- [ ] Test: verify same events fire in new-arch example (`newArchEnabled=true` in `gradle.properties`)

---

## Phase 5 — iOS Photo Capture Migration

> **Status: 🔴 Not Started** | Estimated: 2–3 days  
> Blockers addressed: [B7](./blockers.md#b7--avcapturestillimageoutput-removed-in-xcode-16--ios-18-sdk)

- [ ] **B7** — Migrate `ios/RN/RNCamera.m` and `ios/RN/RNCamera.h`:
  - [ ] Replace `AVCaptureStillImageOutput *stillImageOutput` property with `AVCapturePhotoOutput *photoOutput`
  - [ ] Add `AVCapturePhotoCaptureDelegate` conformance to `RNCamera`
  - [ ] Replace `captureStillImageAsynchronouslyFromConnection:completionHandler:` with `capturePhotoWithSettings:delegate:`
  - [ ] Implement `captureOutput:didFinishProcessingPhoto:error:` delegate method
  - [ ] Handle `[AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:]` → `[photo fileDataRepresentation]`
  - [ ] Update session configuration at `RNCamera.m:1340` to add `photoOutput` to session
- [ ] Apply same migration to legacy `ios/RCT/RCTCameraManager.m` and `ios/RCT/RCTCameraManager.h`
- [ ] Build with Xcode 16 — confirm no deprecation warnings or errors for still-image capture
- [ ] Test `takePicture()` returns valid JPEG on physical device

---

## Phase 6 — iOS New Architecture Compatibility

> **Status: 🔴 Not Started** | Estimated: 1–2 days  
> Blockers addressed: [B9](./blockers.md#b9--ios-initwithbridge-incompatible-with-new-architecture)

- [ ] **B9** — Add `initWithFrame:` initializer to `ios/RN/RNCamera.m`:
  - [ ] Move bridge-independent setup to `initWithFrame:`
  - [ ] Keep `initWithBridge:` calling `initWithFrame:` first
  - [ ] Guard all `self.bridge` accesses with nil check
- [ ] Update `RNCameraManager.m` to implement Fabric-compatible view creation if `self.bridge` is nil
- [ ] Verify `<React/RCTEventDispatcher.h>` import in `RNCamera.m:5` still resolves (or remove if unused)
- [ ] Test with `RCT_NEW_ARCH_ENABLED=1` pod install: camera preview renders without crash
- [ ] Test with `RCT_NEW_ARCH_ENABLED=0`: existing behaviour unchanged

---

## Phase 7 — Podspec / Dependency Cleanup

> **Status: 🔴 Not Started** | Estimated: 0.5 days

- [ ] Verify `min_ios_version_supported` in `react-native-camera.podspec:16` resolves to `"13.4"` with RN 0.77
- [ ] Add `s.swift_version = '5.0'` if any Swift migration is introduced (Phase 6 may require it)
- [ ] Confirm MLKit pod subspecs (`GoogleMLKit/TextRecognition`, `GoogleMLKit/FaceDetection`, `GoogleMLKit/BarcodeScanning`) are available at current versions
- [ ] Run `pod install` in each example app and confirm no resolution errors
- [ ] Remove `createJSModules()` dead code from `RNCameraPackage.java` (deprecated since RN 0.47)

---

## Phase 8 — Testing & CI

> **Status: 🔴 Not Started** | Estimated: 2–3 days

### Manual Test Matrix

| Test Case | iOS (old arch) | iOS (new arch) | Android old arch | Android new arch |
|---|---|---|---|---|
| Camera preview renders | ☐ | ☐ | ☐ | ☐ |
| `takePicture()` returns JPEG | ☐ | ☐ | ☐ | ☐ |
| `record()` / `stopRecording()` | ☐ | ☐ | ☐ | ☐ |
| `onFacesDetected` fires with bounding box | ☐ | ☐ | ☐ | ☐ |
| `FaceDetector.detectFacesAsync()` (file) | ☐ | ☐ | ☐ | ☐ |
| `onTextRecognized` fires with text blocks | ☐ | ☐ | ☐ | ☐ |
| Barcode scan `onBarCodeRead` (ZXing) | ☐ | ☐ | ☐ | ☐ |
| MLKit barcode `onGoogleVisionBarcodesDetected` | ☐ | ☐ | ☐ | ☐ |
| Camera permission request | ☐ | ☐ | ☐ | ☐ |
| App background → foreground (preview resumes) | ☐ | ☐ | ☐ | ☐ |
| 16KB page-size emulator (`mlkit` flavor) | N/A | N/A | ☐ | ☐ |

### CI Checks to Add

- [ ] **Android:** `./gradlew assembleGeneralRelease assembleMlkitRelease` with AGP 8.x — must pass
- [ ] **Android:** `./gradlew lintGeneralRelease lintMlkitRelease` — zero errors
- [ ] **iOS:** `xcodebuild -scheme react-native-camera -sdk iphonesimulator IPHONEOS_DEPLOYMENT_TARGET=13.4` with Xcode 16
- [ ] **iOS new arch:** `RCT_NEW_ARCH_ENABLED=1 pod install && xcodebuild …` — must build
- [ ] **16KB check:** Scan any `.so` in `mlkit` AAR outputs for page alignment (`readelf -lW lib.so | grep LOAD`)
- [ ] **Upgrade helper diff:** Apply `react-native-upgrade-helper` patches for 0.63→0.81 to all example apps (`examples/basic`, `examples/mlkit`, `examples/advanced`)

---

## Definition of Done

The migration is complete when:
1. All Phase 0–8 checkboxes are ticked
2. All 11 blockers in `blockers.md` show status 🟢 Done
3. Full manual test matrix shows no regressions on both platforms, both architectures
4. CI pipeline passes for both `general` and `mlkit` Android flavors
5. `package.json` version is bumped to `5.0.0` and `peerDependencies` includes `"react-native": ">=0.63 <=0.81"`
