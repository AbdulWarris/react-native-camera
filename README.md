# React Native Camera MLKit & Firebase Ready Fork [![Support this project](https://opencollective.com/react-native-camera/sponsors/badge.svg)](#support) [![npm version](https://badge.fury.io/js/react-native-camera.svg)](http://badge.fury.io/js/react-native-camera) [![npm downloads](https://img.shields.io/npm/dm/react-native-camera.svg)](https://www.npmjs.com/package/react-native-camera)

## Fork Notice

This is a maintained fork of `react-native-camera` with the following updates:

- **Google MLKit support**: Uses the latest `GoogleMLKit` pods for Face, Barcode, and Text detection.
- **Firebase compatibility**: Fully compatible with the latest Firebase iOS SDK (tested up to v14).
- **CocoaPods**: Podspec updated for modern MLKit and Firebase.
- **Tested**: `FaceDetectorMLKit` and other MLKit features tested and working with new Firebase (v14).
- **Actively maintained**: This fork will receive updates and fixes as needed.

If you use this fork, please open issues or PRs here for support and improvements.

---

## ⚠️ Troubleshooting: "Module 'GoogleMLKit' not found"

If you see the error:

```
Module 'GoogleMLKit' not found (in target 'react-native-camera' from project 'Pods')
```

**How to fix:**

1. **Make sure you are using CocoaPods 1.10+ and Xcode 12+**
2. **Clean your build and Pods:**
   - Run `cd ios`
   - Run `pod deintegrate`
   - Run `pod install --repo-update`
   - If you see warnings about GoogleMLKit, run `pod cache clean --all` and then `pod install` again.
3. **Open the `.xcworkspace` file in Xcode, not `.xcodeproj`**
4. **If you use M1/M2 Mac, run:**
   - `sudo arch -x86_64 gem install ffi`
   - `arch -x86_64 pod install`
5. **Make sure your Podfile does NOT use `use_frameworks!` or `use_modular_headers!` unless required by your project.**
6. **If you still have issues, try deleting the `Pods/`, `Podfile.lock`, and `DerivedData/` directories and reinstalling.**

---

## Docs
Follow our docs here [https://react-native-camera.github.io/react-native-camera/](https://react-native-camera.github.io/react-native-camera/)

## Sponsors

If you use this library on your commercial/personal projects, you can help us by funding the work on specific issues that you choose by using IssueHunt.io!

This gives you the power to prioritize our work and support the project contributors. Moreover it'll guarantee the project will be updated and maintained in the long run.

[![issuehunt-image](https://issuehunt.io/static/embed/issuehunt-button-v1.svg)](https://issuehunt.io/repos/33218414)

## react-native-camera for enterprise

Available as part of the Tidelift Subscription

The maintainers of react-native-camera and thousands of other packages are working with Tidelift to deliver commercial support and maintenance for the open source dependencies you use to build your applications. Save time, reduce risk, and improve code health, while paying the maintainers of the exact dependencies you use. [Learn more.](https://tidelift.com/subscription/pkg/npm-react-native-camera?utm_source=npm-react-native-camera&utm_medium=referral&utm_campaign=enterprise&utm_term=repo)

## Open Collective

You can also fund this project using open collective

## Sponsors

Become a sponsor and get your logo on our README on Github with a link to your site. [[Become a sponsor](https://opencollective.com/react-native-camera#sponsor)]

<a href="https://opencollective.com/react-native-camera/sponsor/0/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/1/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/1/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/2/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/2/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/3/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/3/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/4/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/4/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/5/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/5/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/6/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/6/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/7/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/7/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/8/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/8/avatar.svg"></a>
<a href="https://opencollective.com/react-native-camera/sponsor/9/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/9/avatar.svg"></a>

The comprehensive camera module for React Native.

Supports:

- photographs.
- videos
- face detection (Android & iOS only)
- barcode scanning
- text recognition (optional installation for iOS using CocoaPods)

### Example import

```jsx
import { RNCamera, FaceDetector } from 'react-native-camera';
```

#### How to use master branch?

We recommend using the releases from npm, however if you need some features that are not published on npm yet you can install react-native-camera from git.

**yarn**: `yarn add react-native-camera@git+https://git@github.com/react-native-community/react-native-camera.git`

**npm**: `npm install --save react-native-camera@git+https://git@github.com/react-native-community/react-native-camera.git`

### Contributing

- Pull Requests are welcome, if you open a pull request we will do our best to get to it in a timely manner
- Pull Request Reviews are even more welcome! we need help testing, reviewing, and updating open PRs
- If you are interested in contributing more actively, please contact me (same username on Twitter, Facebook, etc.) Thanks!
- We are now on [Open Collective](https://opencollective.com/react-native-camera#sponsor)! Contributions are appreciated and will be used to fund core contributors. [more details](#open-collective)
- If you want to help us coding, join Expo slack https://slack.expo.io/, so we can chat over there. (#react-native-camera)

##### Permissions

To use the camera,

1) On Android you must ask for camera permission:

```java
  <uses-permission android:name="android.permission.CAMERA" />
```

To enable `video recording` feature you have to add the following code to the `AndroidManifest.xml`:

```java
  <uses-permission android:name="android.permission.RECORD_AUDIO"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

![5j2jduk](https://cloud.githubusercontent.com/assets/2302315/22190752/6bc6ccd0-e0da-11e6-8e2f-6f22a3567a57.gif)

2) On iOS, you must update Info.plist with a usage description for camera

```xml
...
<key>NSCameraUsageDescription</key>
<string>Your own description of the purpose</string>
...
	
```
For more information on installation, please refer to [installation requirements](./docs/installation.md#requirements).

For general introduction, please take a look into this [RNCamera](./docs/RNCamera.md).

## Security contact information

To report a security vulnerability, please use the

[Tidelift security contact](https://tidelift.com/security).

Tidelift will coordinate the fix and disclosure.
