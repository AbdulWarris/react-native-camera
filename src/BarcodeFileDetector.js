// @flow
import { NativeModules } from 'react-native';

const barcodeDetectionDisabledMessage = 'Barcode file detection has not been included in this build.';

const BarcodeDetectorModule: Object = NativeModules.RNBarcodeDetector || {
  stubbed: true,
  detectBarcodes: () => new Promise((_, reject) => reject(barcodeDetectionDisabledMessage)),
};

export type BarcodeFeature = {
  data: string,
  dataRaw: string,
  type: string,
};

export default class BarcodeFileDetector {
  // Decodes barcodes/QR codes from a static local image file (e.g. one picked from the
  // gallery), as opposed to RNCamera's own onBarCodeRead, which only fires on live
  // camera frames. Resolves with { barcodes: BarcodeFeature[] }.
  static detectBarcodesAsync(uri: string): Promise<{ barcodes: Array<BarcodeFeature> }> {
    return BarcodeDetectorModule.detectBarcodes({ uri });
  }
}
