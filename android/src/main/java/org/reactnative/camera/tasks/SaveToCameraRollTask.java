package org.reactnative.camera.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.react.bridge.Promise;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Saves an arbitrary local image file (e.g. a react-native-view-shot snapshot) into the
// device's shared photo gallery. This is deliberately independent of picture-taking (see
// CameraModule's own capture path) — it just needs a file URI in. Uses the modern
// MediaStore collection API (API 29+, matches this fork's minSdk) so no WRITE_EXTERNAL_STORAGE
// permission is required for the app's own inserts, and no deprecated android.os.AsyncTask
// (see migrationV5/blockers.md B4) — a plain background executor is enough for this one-shot
// file copy.
public class SaveToCameraRollTask {
  private static final String ERROR_TAG = "E_SAVE_TO_CAMERA_ROLL_FAILED";
  private static final ExecutorService sExecutor = Executors.newCachedThreadPool();

  private final String mUri;
  private final Promise mPromise;
  private final Context mContext;

  public SaveToCameraRollTask(Context context, String uri, Promise promise) {
    mContext = context;
    mUri = uri;
    mPromise = promise;
  }

  public void execute() {
    if (mUri == null) {
      mPromise.reject(ERROR_TAG, "You have to provide a URI of an image.");
      return;
    }

    Uri parsed = Uri.parse(mUri);
    final String path = "file".equals(parsed.getScheme()) || parsed.getScheme() == null
        ? parsed.getPath()
        : null;
    if (path == null || !new File(path).exists()) {
      mPromise.reject(ERROR_TAG, "The file does not exist. Given URI: `" + mUri + "`.");
      return;
    }

    sExecutor.submit(() -> {
      try {
        String savedUri = insertIntoMediaStore(path);
        mPromise.resolve(savedUri);
      } catch (IOException e) {
        Log.e(ERROR_TAG, "Saving `" + path + "` to the camera roll failed.", e);
        mPromise.reject(ERROR_TAG, "Saving the image to the camera roll failed.", e);
      }
    });
  }

  private String insertIntoMediaStore(String path) throws IOException {
    ContentResolver resolver = mContext.getContentResolver();
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DISPLAY_NAME, new File(path).getName());
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures");
      values.put(MediaStore.Images.Media.IS_PENDING, 1);
    }

    Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    Uri item = resolver.insert(collection, values);
    if (item == null) {
      throw new IOException("MediaStore rejected the insert.");
    }

    try (InputStream in = new FileInputStream(path);
         OutputStream out = resolver.openOutputStream(item)) {
      if (out == null) {
        throw new IOException("Could not open an output stream for the inserted item.");
      }
      byte[] buffer = new byte[8192];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      values.clear();
      values.put(MediaStore.Images.Media.IS_PENDING, 0);
      resolver.update(item, values, null, null);
    }

    return item.toString();
  }
}
