package org.reactnative.camera.tasks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;
import android.util.Base64;

import org.reactnative.camera.RNCameraViewHelper;
import org.reactnative.camera.utils.RNFileUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResolveTakenPictureAsyncTask {
    private static final String ERROR_TAG = "E_TAKING_PICTURE_FAILED";
    private static final ExecutorService sExecutor = Executors.newCachedThreadPool();

    private Promise mPromise;
    private Bitmap mBitmap;
    private byte[] mImageData;
    private ReadableMap mOptions;
    private File mCacheDirectory;
    private int mDeviceOrientation;
    private int mSoftwareRotation;
    private PictureSavedDelegate mPictureSavedDelegate;

    public ResolveTakenPictureAsyncTask(byte[] imageData, Promise promise, ReadableMap options, File cacheDirectory, int deviceOrientation, int softwareRotation, PictureSavedDelegate delegate) {
        mPromise = promise;
        mOptions = options;
        mImageData = imageData;
        mCacheDirectory = cacheDirectory;
        mDeviceOrientation = deviceOrientation;
        mSoftwareRotation = softwareRotation;
        mPictureSavedDelegate = delegate;
    }

    private int getQuality() {
        return (int) (mOptions.getDouble("quality") * 100);
    }

    // loads bitmap only if necessary
    private void loadBitmap() throws IOException {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length);
        }
        if (mBitmap == null) {
            throw new IOException("Failed to decode Image Bitmap");
        }
    }

    public void execute() {
        sExecutor.submit(new Runnable() {
            @Override
            public void run() {
                WritableMap response = processImage();
                if (response != null) {
                    if (mOptions.hasKey("fastMode") && mOptions.getBoolean("fastMode")) {
                        WritableMap wrapper = Arguments.createMap();
                        wrapper.putInt("id", mOptions.getInt("id"));
                        wrapper.putMap("data", response);
                        mPictureSavedDelegate.onPictureSaved(wrapper);
                    } else {
                        mPromise.resolve(response);
                    }
                }
            }
        });
    }

    private WritableMap processImage() {
        WritableMap response = Arguments.createMap();
        ByteArrayInputStream inputStream = null;
        ExifInterface exifInterface = null;
        WritableMap exifData = null;
        ReadableMap exifExtraData = null;

        boolean exifOrientationFixed = false;

        response.putInt("deviceOrientation", mDeviceOrientation);
        response.putInt("pictureOrientation", mOptions.hasKey("orientation") ? mOptions.getInt("orientation") : mDeviceOrientation);

        try {
            inputStream = new ByteArrayInputStream(mImageData);

            if (mSoftwareRotation != 0) {
                loadBitmap();
                mBitmap = rotateBitmap(mBitmap, mSoftwareRotation);
            }

            if (mOptions.hasKey("fixOrientation") && mOptions.getBoolean("fixOrientation")) {
                exifInterface = new ExifInterface(inputStream);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                if (orientation != ExifInterface.ORIENTATION_UNDEFINED && getImageRotation(orientation) != 0) {
                    loadBitmap();
                    int angle = getImageRotation(orientation);
                    mBitmap = rotateBitmap(mBitmap, angle);
                    exifOrientationFixed = true;
                }
            }

            if (mOptions.hasKey("width")) {
                loadBitmap();
                mBitmap = resizeBitmap(mBitmap, mOptions.getInt("width"));
            }

            if (mOptions.hasKey("mirrorImage") && mOptions.getBoolean("mirrorImage")) {
                loadBitmap();
                mBitmap = flipHorizontally(mBitmap);
            }

            boolean writeExifToResponse = mOptions.hasKey("exif") && mOptions.getBoolean("exif");
            boolean writeExifToFile = true;

            if (mOptions.hasKey("writeExif")) {
                switch (mOptions.getType("writeExif")) {
                    case Boolean:
                        writeExifToFile = mOptions.getBoolean("writeExif");
                        break;
                    case Map:
                        exifExtraData = mOptions.getMap("writeExif");
                        writeExifToFile = true;
                        break;
                }
            }

            if (writeExifToResponse || writeExifToFile) {
                if (mBitmap != null || exifExtraData != null || writeExifToResponse) {
                    if (exifInterface == null) {
                        exifInterface = new ExifInterface(inputStream);
                    }
                    exifData = RNCameraViewHelper.getExifData(exifInterface);
                    if (exifExtraData != null) {
                        exifData.merge(exifExtraData);
                    }
                }

                if (mBitmap != null) {
                    exifData.putInt("width", mBitmap.getWidth());
                    exifData.putInt("height", mBitmap.getHeight());
                    if (exifOrientationFixed) {
                        exifData.putInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    }
                }

                if (writeExifToResponse) {
                    final WritableMap exifDataCopy = Arguments.createMap();
                    exifDataCopy.merge(exifData);
                    response.putMap("exif", exifDataCopy);
                }
            }

            if (mBitmap == null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length, options);
                if (options != null) {
                    response.putInt("width", options.outWidth);
                    response.putInt("height", options.outHeight);
                }

                if (!mOptions.hasKey("doNotSave") || !mOptions.getBoolean("doNotSave")) {
                    File imageFile = new File(getImagePath());
                    imageFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(imageFile);
                    fOut.write(mImageData);
                    fOut.flush();
                    fOut.close();

                    if (writeExifToFile && exifExtraData != null) {
                        ExifInterface fileExifInterface = new ExifInterface(imageFile.getAbsolutePath());
                        RNCameraViewHelper.setExifData(fileExifInterface, exifExtraData);
                        fileExifInterface.saveAttributes();
                    } else if (!writeExifToFile) {
                        ExifInterface fileExifInterface = new ExifInterface(imageFile.getAbsolutePath());
                        RNCameraViewHelper.clearExifData(fileExifInterface);
                        fileExifInterface.saveAttributes();
                    }

                    String fileUri = Uri.fromFile(imageFile).toString();
                    response.putString("uri", fileUri);
                }

                if (mOptions.hasKey("base64") && mOptions.getBoolean("base64")) {
                    response.putString("base64", Base64.encodeToString(mImageData, Base64.NO_WRAP));
                }
            } else {
                response.putInt("width", mBitmap.getWidth());
                response.putInt("height", mBitmap.getHeight());

                ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                if (!mBitmap.compress(Bitmap.CompressFormat.JPEG, getQuality(), imageStream)) {
                    mPromise.reject(ERROR_TAG, "Could not compress image to JPEG");
                    return null;
                }

                if (!mOptions.hasKey("doNotSave") || !mOptions.getBoolean("doNotSave")) {
                    String filePath = writeStreamToFile(imageStream);
                    if (writeExifToFile && exifData != null) {
                        ExifInterface fileExifInterface = new ExifInterface(filePath);
                        RNCameraViewHelper.setExifData(fileExifInterface, exifData);
                        fileExifInterface.saveAttributes();
                    }
                    File imageFile = new File(filePath);
                    String fileUri = Uri.fromFile(imageFile).toString();
                    response.putString("uri", fileUri);
                }

                if (mOptions.hasKey("base64") && mOptions.getBoolean("base64")) {
                    response.putString("base64", Base64.encodeToString(imageStream.toByteArray(), Base64.NO_WRAP));
                }
            }

            return response;

        } catch (Resources.NotFoundException e) {
            mPromise.reject(ERROR_TAG, "Documents directory of the app could not be found.", e);
            e.printStackTrace();
        } catch (IOException e) {
            mPromise.reject(ERROR_TAG, "An unknown I/O exception has occurred.", e);
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap resizeBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleRatio = (float) newWidth / (float) width;
        return Bitmap.createScaledBitmap(bm, newWidth, (int) (height * scaleRatio), true);
    }

    private Bitmap flipHorizontally(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private int getImageRotation(int orientation) {
        int rotationDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = 270;
                break;
        }
        return rotationDegrees;
    }

    private String getImagePath() throws IOException {
        if (mOptions.hasKey("path")) {
            return mOptions.getString("path");
        }
        return RNFileUtils.getOutputFilePath(mCacheDirectory, ".jpg");
    }

    private String writeStreamToFile(ByteArrayOutputStream imageDataStream) throws IOException {
        String outputPath = null;
        IOException exception = null;
        FileOutputStream fileOutputStream = null;

        try {
            outputPath = getImagePath();
            fileOutputStream = new FileOutputStream(outputPath);
            imageDataStream.writeTo(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (exception != null) {
            throw exception;
        }

        return outputPath;
    }
}
