package org.reactnative.barcodedetector.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import org.reactnative.barcodedetector.BarcodeFormatUtils;
import org.reactnative.barcodedetector.RNBarcodeDetector;
import org.reactnative.frame.RNFrame;
import org.reactnative.frame.RNFrameFactory;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// `general`-flavor counterpart of the `mlkit`-flavor FileBarcodeDetectionAsyncTask —
// this flavor's RNBarcodeDetector only exposes a synchronous detect(RNFrame) (matching
// its live-camera-frame API), not getDetector(), so this decodes the file into a
// Bitmap/RNFrame instead of an InputImage, mirroring FileFaceDetectionAsyncTask's own
// general/mlkit split for the exact same reason.
public class FileBarcodeDetectionAsyncTask {
  private static final String ERROR_TAG = "E_BARCODE_DETECTION_FAILED";
  private static final ExecutorService sExecutor = Executors.newCachedThreadPool();

  private String mUri;
  private String mPath;
  private Promise mPromise;
  private Context mContext;
  private RNBarcodeDetector mRNBarcodeDetector;

  public FileBarcodeDetectionAsyncTask(Context context, ReadableMap options, Promise promise) {
    mUri = options.getString("uri");
    mPromise = promise;
    mContext = context;
  }

  public void execute() {
    if (mUri == null) {
      mPromise.reject(ERROR_TAG, "You have to provide an URI of an image.");
      return;
    }

    Uri uri = Uri.parse(mUri);
    mPath = uri.getPath();

    if (mPath == null) {
      mPromise.reject(ERROR_TAG, "Invalid URI provided: `" + mUri + "`.");
      return;
    }

    boolean fileIsInSafeDirectories =
        mPath.startsWith(mContext.getCacheDir().getPath()) || mPath.startsWith(mContext.getFilesDir().getPath());

    if (!fileIsInSafeDirectories) {
      mPromise.reject(ERROR_TAG, "The image has to be in the local app's directories.");
      return;
    }

    if (!new File(mPath).exists()) {
      mPromise.reject(ERROR_TAG, "The file does not exist. Given path: `" + mPath + "`.");
      return;
    }

    sExecutor.submit(new Runnable() {
      @Override
      public void run() {
        mRNBarcodeDetector = new RNBarcodeDetector(mContext);

        Bitmap bitmap = BitmapFactory.decodeFile(mPath);
        if (bitmap == null) {
          mPromise.reject(ERROR_TAG, "Failed to decode image file: `" + mPath + "`.");
          return;
        }

        try {
          RNFrame frame = RNFrameFactory.buildFrame(bitmap);
          List<Barcode> barcodes = mRNBarcodeDetector.detect(frame);
          serializeEventData(barcodes);
        } catch (Exception e) {
          Log.e(ERROR_TAG, "Barcode detection task failed", e);
          mPromise.reject(ERROR_TAG, "Barcode detection task failed", e);
        }
      }
    });
  }

  private void serializeEventData(List<Barcode> barcodes) {
    WritableArray barcodesArray = Arguments.createArray();

    if (barcodes != null) {
      for (Barcode barcode : barcodes) {
        WritableMap serializedBarcode = Arguments.createMap();
        serializedBarcode.putString("data", barcode.getDisplayValue());
        serializedBarcode.putString("dataRaw", barcode.getRawValue());
        // The `general` flavor's BarcodeFormatUtils only maps the raw format code (unlike
        // `mlkit`'s, which separately exposes a value-type mapping too) — same "format"
        // output key as the mlkit flavor, just sourced from the one mapping this flavor has.
        serializedBarcode.putString("type", BarcodeFormatUtils.get(barcode.getFormat()));
        serializedBarcode.putString("format", BarcodeFormatUtils.get(barcode.getFormat()));
        barcodesArray.pushMap(serializedBarcode);
      }
    }

    WritableMap result = Arguments.createMap();
    result.putArray("barcodes", barcodesArray);

    mRNBarcodeDetector.release();
    mPromise.resolve(result);
  }
}
