/**
 * Smoke tests for the advanced example.
 *
 * The app uses native-base + react-navigation + react-native-camera, all of
 * which require native modules unavailable in Jest.  Instead of attempting a
 * full render (which would need dozens of mocks for every native bridge) we
 * verify that:
 *  1. The react-native-camera mock exports the expected API surface.
 *  2. Key constants that the app reads at runtime are defined.
 *  3. The FaceDetector stub works correctly.
 *
 * The react-native-camera module is resolved to __mocks__/react-native-camera.js
 * via the moduleNameMapper entry in package.json — no explicit jest.mock() needed.
 */

// ── Tests ──────────────────────────────────────────────────────────────────

describe('react-native-camera mock', () => {
  let rnCamera;

  beforeAll(() => {
    rnCamera = require('react-native-camera');
  });

  test('exports RNCamera', () => {
    expect(rnCamera.RNCamera).toBeDefined();
  });

  test('exports FaceDetector', () => {
    expect(rnCamera.FaceDetector).toBeDefined();
  });

  test('RNCamera.Constants.Type has back and front', () => {
    expect(rnCamera.RNCamera.Constants.Type.back).toBeDefined();
    expect(rnCamera.RNCamera.Constants.Type.front).toBeDefined();
  });

  test('RNCamera.Constants.FlashMode has off/on/auto/torch', () => {
    const fm = rnCamera.RNCamera.Constants.FlashMode;
    expect(fm.off).toBeDefined();
    expect(fm.on).toBeDefined();
    expect(fm.auto).toBeDefined();
    expect(fm.torch).toBeDefined();
  });

  test('RNCamera.Constants.VideoQuality has 288p', () => {
    expect(rnCamera.RNCamera.Constants.VideoQuality['288p']).toBeDefined();
  });

  test('RNCamera.Constants.GoogleVisionBarcodeDetection is defined', () => {
    expect(rnCamera.RNCamera.Constants.GoogleVisionBarcodeDetection).toBeDefined();
    expect(rnCamera.RNCamera.Constants.GoogleVisionBarcodeDetection.BarcodeType.ALL).toBeDefined();
    expect(rnCamera.RNCamera.Constants.GoogleVisionBarcodeDetection.BarcodeMode.ALTERNATE).toBeDefined();
  });

  test('RNCamera.Constants.CameraStatus has READY', () => {
    expect(rnCamera.RNCamera.Constants.CameraStatus.READY).toBe('READY');
  });

  test('FaceDetector.detectFacesAsync returns a promise', () => {
    const result = rnCamera.FaceDetector.detectFacesAsync({});
    expect(typeof result.then).toBe('function');
    return result.then((res) => {
      expect(res.faces).toEqual([]);
    });
  });
});
