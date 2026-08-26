// @flow
import { NativeModules } from 'react-native';

const cameraRollDisabledMessage = 'Camera roll saving has not been included in this build.';

const CameraRollModule: Object = NativeModules.RNCameraRoll || {
  stubbed: true,
  save: () => new Promise((_, reject) => reject(cameraRollDisabledMessage)),
};

export default class CameraRoll {
  // Saves a local image file (e.g. a react-native-view-shot / captureRef snapshot) into
  // the device's photo gallery. Resolves with the saved asset's identifier/URI.
  static save(uri: string): Promise<string> {
    return CameraRollModule.save(uri);
  }
}
