package org.reactnative.barcodedetector.tasks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.reactnative.barcodedetector.BarcodeFormatUtils;
import org.reactnative.barcodedetector.RNBarcodeDetector;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Decodes barcodes/QR codes from a static local image file (e.g. one picked from the
// gallery), as opposed to BarcodeDetectorAsyncTask which processes live camera frames.
// Mirrors FileFaceDetectionAsyncTask's shape exactly — same validation, same executor
// pattern, same promise-based contract — just swapping the face detector for the
// barcode scanner the fork already depends on for live scanning.
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

        try {
          InputImage image = InputImage.fromFilePath(mContext, Uri.parse(mUri));
          BarcodeScanner detector = mRNBarcodeDetector.getDetector();
          detector.process(image)
              .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                  serializeEventData(barcodes);
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                  Log.e(ERROR_TAG, "Barcode detection task failed", e);
                  mPromise.reject(ERROR_TAG, "Barcode detection task failed", e);
                }
              });
        } catch (IOException e) {
          Log.e(ERROR_TAG, "Creating InputImage from uri " + mUri + " failed", e);
          mPromise.reject(ERROR_TAG, "Creating InputImage from uri " + mUri + " failed", e);
        }
      }
    });
  }

  private void serializeEventData(List<Barcode> barcodes) {
    WritableArray barcodesArray = Arguments.createArray();

    for (Barcode barcode : barcodes) {
      WritableMap serializedBarcode = Arguments.createMap();
      serializedBarcode.putString("data", barcode.getDisplayValue());
      serializedBarcode.putString("dataRaw", barcode.getRawValue());
      serializedBarcode.putString("type", BarcodeFormatUtils.get(barcode.getValueType()));
      serializedBarcode.putString("format", BarcodeFormatUtils.getFormat(barcode.getFormat()));
      barcodesArray.pushMap(serializedBarcode);
    }

    WritableMap result = Arguments.createMap();
    result.putArray("barcodes", barcodesArray);

    mRNBarcodeDetector.release();
    mPromise.resolve(result);
  }
}
