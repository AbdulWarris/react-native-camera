// @flow
import RNCamera, { type Status as _CameraStatus, hasTorch } from './RNCamera';
import FaceDetector from './FaceDetector';
import CameraRoll from './CameraRoll';
import BarcodeFileDetector from './BarcodeFileDetector';

export type CameraStatus = _CameraStatus;
export { RNCamera, FaceDetector, CameraRoll, BarcodeFileDetector, hasTorch };
