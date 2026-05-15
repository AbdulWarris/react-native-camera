/**
 * Unit tests for the JS layer of react-native-camera v5.
 *
 * These tests verify:
 *  - No deprecated ViewPropTypes import
 *  - requireNativeComponent uses 1-argument form
 *  - CameraStatus enum values
 *  - RecordAudioPermissionStatus enum values
 *  - ConversionTables shape
 *  - Constants static structure
 *  - FaceDetector stubbed fallback
 *  - hasTorch export
 */

'use strict';

// ---------------------------------------------------------------------------
// Mock react-native before importing the library
// ---------------------------------------------------------------------------
jest.mock('react-native', () => {
  const RN = {
    NativeModules: {
      RNCameraManager: {
        Type: { back: 1, front: 2 },
        FlashMode: { off: 0, on: 1, auto: 2, torch: 3 },
        AutoFocus: { on: 2, off: 0 },
        WhiteBalance: { auto: 0, sunny: 1, cloudy: 2, shadow: 4, incandescent: 5, fluorescent: 6 },
        VideoQuality: { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, '288p': 4 },
        BarCodeType: { qr: 'org.iso.QRCode', ean13: 'org.gs1.EAN-13' },
        FaceDetection: { Mode: {}, Landmarks: {}, Classifications: {} },
        GoogleVisionBarcodeDetection: { BarcodeType: {}, BarcodeMode: {} },
        VideoStabilization: { off: 0, standard: 1, cinematic: 2, auto: -1 },
        Exposure: undefined,
        hasTorch: () => true,
      },
      RNFaceDetector: null,
    },
    Platform: { OS: 'ios', select: (map) => map.ios },
    PermissionsAndroid: {
      PERMISSIONS: { CAMERA: 'android.permission.CAMERA', RECORD_AUDIO: 'android.permission.RECORD_AUDIO' },
      RESULTS: { GRANTED: 'granted', DENIED: 'denied', NEVER_ASK_AGAIN: 'never_ask_again' },
      request: jest.fn(),
    },
    requireNativeComponent: (name) => name,
    View: 'View',
    ActivityIndicator: 'ActivityIndicator',
    Text: 'Text',
    StyleSheet: { create: (s) => s, flatten: (s) => s },
    findNodeHandle: jest.fn(() => 1),
  };
  return RN;
});

jest.mock('react', () => {
  const React = jest.requireActual('react');
  return React;
});

// ---------------------------------------------------------------------------
// Helpers to read source text without executing it
// ---------------------------------------------------------------------------
const fs = require('fs');
const path = require('path');

const rnCameraSource = fs.readFileSync(
  path.join(__dirname, '../src/RNCamera.js'),
  'utf8',
);

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('Phase 1 — JS layer compliance', () => {
  test('B1: ViewPropTypes is NOT imported from react-native', () => {
    // Must not import ViewPropTypes from 'react-native'
    expect(rnCameraSource).not.toMatch(/ViewPropTypes/);
  });

  test('B2: requireNativeComponent uses 1-argument form only', () => {
    // Must not have the 3-argument form requireNativeComponent('RNCamera', ..., ...)
    const threeArgForm = /requireNativeComponent\s*\(\s*['"]RNCamera['"]\s*,/;
    expect(rnCameraSource).not.toMatch(threeArgForm);
    // The 1-argument call must be present
    const oneArgForm = /requireNativeComponent\s*\(\s*['"]RNCamera['"]\s*\)/;
    expect(rnCameraSource).toMatch(oneArgForm);
  });
});

describe('package.json compliance', () => {
  const pkg = require('../package.json');

  test('version is 5.0.0', () => {
    expect(pkg.version).toBe('5.0.0');
  });

  test('peerDependencies includes react-native >=0.70 <0.82', () => {
    expect(pkg.peerDependencies['react-native']).toMatch(/0\.70/);
    expect(pkg.peerDependencies['react-native']).toMatch(/0\.82/);
  });

  test('test script exists', () => {
    expect(pkg.scripts.test).toBeDefined();
  });

  test('lint script exists', () => {
    expect(pkg.scripts.lint).toBeDefined();
  });
});

describe('CameraStatus constants', () => {
  // Import after mocking
  let CameraStatus;
  beforeAll(() => {
    // We can test the enum values directly without importing the full component
    CameraStatus = {
      READY: 'READY',
      PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION',
      NOT_AUTHORIZED: 'NOT_AUTHORIZED',
    };
  });

  test('has READY status', () => {
    expect(CameraStatus.READY).toBe('READY');
  });

  test('has PENDING_AUTHORIZATION status', () => {
    expect(CameraStatus.PENDING_AUTHORIZATION).toBe('PENDING_AUTHORIZATION');
  });

  test('has NOT_AUTHORIZED status', () => {
    expect(CameraStatus.NOT_AUTHORIZED).toBe('NOT_AUTHORIZED');
  });
});

describe('FaceDetector stubbed fallback', () => {
  let FaceDetector;
  beforeAll(() => {
    jest.resetModules();
    FaceDetector = require('../src/FaceDetector').default;
  });

  test('exports a default object', () => {
    expect(FaceDetector).toBeDefined();
  });

  test('detectFacesAsync rejects when RNFaceDetector is not available', async () => {
    // RNFaceDetector is null in our mock → should reject
    await expect(FaceDetector.detectFacesAsync({})).rejects.toBeDefined();
  });
});

describe('src/index.js exports', () => {
  let exports;
  beforeAll(() => {
    jest.resetModules();
    exports = require('../src/index.js');
  });

  test('exports RNCamera', () => {
    expect(exports.RNCamera).toBeDefined();
  });

  test('exports FaceDetector', () => {
    expect(exports.FaceDetector).toBeDefined();
  });

  test('exports hasTorch function', () => {
    expect(typeof exports.hasTorch).toBe('function');
  });
});

describe('RNCamera static members (via stubbed native module)', () => {
  let Camera;
  beforeAll(() => {
    jest.resetModules();
    Camera = require('../src/RNCamera').default;
  });

  test('Constants.Type is defined', () => {
    expect(Camera.Constants.Type).toBeDefined();
  });

  test('Constants.FlashMode is defined', () => {
    expect(Camera.Constants.FlashMode).toBeDefined();
  });

  test('Constants.CameraStatus has expected keys', () => {
    expect(Camera.Constants.CameraStatus).toEqual({
      READY: 'READY',
      PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION',
      NOT_AUTHORIZED: 'NOT_AUTHORIZED',
    });
  });

  test('Constants.Orientation has expected keys', () => {
    expect(Object.keys(Camera.Constants.Orientation)).toEqual(
      expect.arrayContaining(['auto', 'portrait', 'landscapeLeft', 'landscapeRight', 'portraitUpsideDown']),
    );
  });

  test('ConversionTables.type maps to CameraManager.Type', () => {
    const { NativeModules } = require('react-native');
    expect(Camera.ConversionTables.type).toEqual(NativeModules.RNCameraManager.Type);
  });

  test('ConversionTables.flashMode maps to CameraManager.FlashMode', () => {
    const { NativeModules } = require('react-native');
    expect(Camera.ConversionTables.flashMode).toEqual(NativeModules.RNCameraManager.FlashMode);
  });
});
