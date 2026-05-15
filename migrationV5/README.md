# react-native-camera → v5: Migration to React Native 0.81

> **Status: 🔴 NOT STARTED**  
> Tracking upgrade from the current codebase (RN 0.63 devDep / v4.2.1) to full RN 0.81 compatibility.

---

## Contents

| Document | Description |
|---|---|
| [README.md](./README.md) | This file — overview, architecture summary, and risk matrix |
| [blockers.md](./blockers.md) | All 11 blockers with status, file references, and fix details |
| [migration-plan.md](./migration-plan.md) | 8-phase stepwise plan with per-step status and CI/test matrix |

---

## Current Architecture Summary

### Android

| Aspect | Current State |
|---|---|
| Android Gradle Plugin | `4.1.0` (`android/build.gradle:18`) |
| compileSdk / buildTools | `29 / 29.0.2` (`android/build.gradle:26-27`) |
| minSdk / targetSdk | `16 / 29` (`android/build.gradle:31-32`) |
| Language | Pure Java — no Kotlin, no C++, no NDK |
| Native `.so` files | **None** bundled by this library |
| Product flavors | `general` (Play Services MLKit) / `mlkit` (bundled MLKit) |
| RN bridge style | Classic legacy bridge: `ReactPackage`, `ReactContextBaseJavaModule`, `ViewGroupManager` |
| Camera stacks | `com.lwansbrough.RCTCamera` (Camera1 legacy) + `org.reactnative.camera` (Camera2) |

### iOS

| Aspect | Current State |
|---|---|
| Language | Pure Objective-C |
| iOS deployment target | `min_ios_version_supported` (resolves from RN podspec at install time) |
| Capture API | `AVCaptureStillImageOutput` — deprecated iOS 10, **removed Xcode 16** |
| Bridge style | Classic `RCTBridgeModule`, `RCTViewManager`, `initWithBridge:` |
| MLKit integration | Optional subspecs via `__has_include` guards (`TextDetector`, `FaceDetectorMLKit`, `BarcodeDetectorMLKit`) |

---

## Risk Matrix

| Feature | Risk | Primary Cause |
|---|---|---|
| Camera preview | 🟠 HIGH | iOS: `AVCaptureStillImageOutput` removal; Android: AGP/SDK update required |
| Photo capture | 🔴 CRITICAL | `captureStillImageAsynchronouslyFromConnection:` removed in Xcode 16 / iOS 18 SDK |
| Video recording | 🟠 HIGH | iOS pipeline audit needed; Android: `AsyncTask` removal |
| Face detection (live) | 🔴 CRITICAL | Android: `UIManagerModule.getEventDispatcher()` removed → events never dispatched |
| Face detection (file) | 🟡 MEDIUM | Android: same broken event path in `FileFaceDetectionAsyncTask` |
| OCR text extraction (live) | 🔴 CRITICAL | Android: same `UIManagerModule` breakage + synchronous `Task.getResult()` may throw |
| Barcode scanning (ZXing) | 🔴 CRITICAL | Same `UIManagerModule` breakage for `BarCodeReadEvent` |
| Barcode scanning (MLKit) | 🔴 CRITICAL | Same `UIManagerModule` + `BarcodesDetectedEvent` |
| JS prop types (app startup) | 🔴 CRITICAL | `ViewPropTypes` removed from `react-native` in 0.68 — **crashes on import** |
| Permissions | 🟡 MEDIUM | `PermissionsAndroid` API stable; minSdk 16 must raise to 24 |
| Lifecycle | 🟡 MEDIUM | `LifecycleEventListener` stable on bridge; Fabric uses different lifecycle |
| Background/foreground | 🟡 MEDIUM | `onHostPause`/`onHostResume` work on bridge; Fabric lifecycle differs |

---

## Android 16KB Page-Size Readiness

The library itself contains **no native `.so` files** — all ML work is delegated to Google MLKit JARs/AARs. This minimises risk significantly.

| Dependency | Risk | Notes |
|---|---|---|
| Library's own `.so` | **None** | No CMakeLists, no JNI confirmed |
| `play-services-mlkit-*` (general flavor) | Low | Native code runs in Google Play Services process, OTA-updated by Google |
| `com.google.mlkit:*` (mlkit flavor) v16.1.x | **Medium** | Bundled 2021 SDK may ship non-compliant `.so` inside APK — must update to `17.x+` |
| `com.google.zxing:core:3.3.3` | Low | Pure Java |
| `com.drewnoakes:metadata-extractor:2.11.0` | Low | Pure Java |
| React Native itself | Managed upstream | RN 0.77+ Hermes/JSI `.so` files are 16KB-aligned |

---

## Effort Summary

| Phase | Focus | Estimated Effort | Status |
|---|---|---|---|
| 0 | Preparation & baseline | 1 day | 🔴 Not Started |
| 1 | JS layer fixes | 1–2 days | 🔴 Not Started |
| 2 | Android build toolchain | 2–3 days | 🔴 Not Started |
| 3 | Android `AsyncTask` removal | 3–4 days | 🔴 Not Started |
| 4 | Android event system | 3–5 days | 🔴 Not Started |
| 5 | iOS photo capture migration | 2–3 days | 🔴 Not Started |
| 6 | iOS new architecture compatibility | 1–2 days | 🔴 Not Started |
| 7 | Podspec / dependency cleanup | 0.5 days | 🔴 Not Started |
| 8 | Testing & CI | 2–3 days | 🔴 Not Started |
| **Total** | | **~3–4 weeks** | |

See [migration-plan.md](./migration-plan.md) for full phase details and [blockers.md](./blockers.md) for individual blocker tracking.
