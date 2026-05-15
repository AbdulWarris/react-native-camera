/**
 * Manual mock for react-native-camera used in Jest tests.
 * The real package requires native modules not available in Jest.
 */
const React = require('react');
const { View } = require('react-native');

const RNCamera = class RNCamera extends React.Component {
  render() {
    return React.createElement(View, this.props);
  }
};

RNCamera.Constants = {
  Type: { back: 1, front: 2 },
  FlashMode: { off: 0, on: 1, auto: 2, torch: 3 },
  AutoFocus: { on: 1, off: 0 },
  WhiteBalance: { auto: 0 },
  VideoQuality: { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, '288p': 4 },
  BarCodeType: { qr: 'qr', ean13: 'ean13' },
  FaceDetection: {
    Mode: { fast: 1, accurate: 2 },
    Landmarks: { none: 0, all: 1 },
    Classifications: { none: 0, all: 1 },
  },
  GoogleVisionBarcodeDetection: {
    BarcodeType: { ALL: 0, QR_CODE: 256 },
    BarcodeMode: { NORMAL: 0, ALTERNATE: 1, INVERTED: 2 },
  },
  CameraStatus: { READY: 'READY', PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION', NOT_AUTHORIZED: 'NOT_AUTHORIZED' },
  Orientation: { auto: 'auto', portrait: 'portrait', landscapeLeft: 'landscapeLeft', landscapeRight: 'landscapeRight', portraitUpsideDown: 'portraitUpsideDown' },
};

const FaceDetector = {
  detectFacesAsync: jest.fn().mockResolvedValue({ faces: [] }),
};

module.exports = {
  RNCamera,
  FaceDetector,
};
