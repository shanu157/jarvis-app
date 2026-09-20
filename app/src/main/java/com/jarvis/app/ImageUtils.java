package com.jarvis.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class ImageUtils {

    private static final int MAX_SIZE = 1600;
    private static final int JPEG_QUALITY = 82;

    private ImageUtils() {
    }

    public static String uriToBase64(
            Context context,
            Uri uri
    ) throws Exception {

        if (context == null) {
            throw new IllegalArgumentException(
                    "Context is required."
            );
        }

        if (uri == null) {
            throw new IllegalArgumentException(
                    "Image URI is required."
            );
        }

        Bitmap bitmap =
                decodeBitmap(
                        context,
                        uri
                );

        if (bitmap == null) {
            throw new IllegalArgumentException(
                    "Unable to read the selected image."
            );
        }

        Bitmap scaled =
                scaleBitmap(
                        bitmap,
                        MAX_SIZE
                );

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        scaled.compress(
                Bitmap.CompressFormat.JPEG,
                JPEG_QUALITY,
                output
        );

        byte[] bytes =
                output.toByteArray();

        if (scaled != bitmap) {
            scaled.recycle();
        }

        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }

        return Base64.encodeToString(
                bytes,
                Base64.NO_WRAP
        );
    }

    public static Bitmap decodeBitmap(
            Context context,
            Uri uri
    ) throws Exception {

        InputStream input =
                context.getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            return null;
        }

        try {

            BitmapFactory.Options options =
                    new BitmapFactory.Options();

            options.inPreferredConfig =
                    Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeStream(
                    input,
                    null,
                    options
            );

        } finally {

            input.close();
        }
    }

    public static Bitmap scaleBitmap(
            Bitmap bitmap,
            int maxSize
    ) {

        if (bitmap == null) {
            return null;
        }

        if (maxSize <= 0) {
            return bitmap;
        }

        int width =
                bitmap.getWidth();

        int height =
                bitmap.getHeight();

        if (width <= maxSize &&
                height <= maxSize) {

            return bitmap;
        }

        float scale =
                Math.min(
                        (float) maxSize / width,
                        (float) maxSize / height
                );

        int newWidth =
                Math.max(
                        1,
                        Math.round(width * scale)
                );

        int newHeight =
                Math.max(
                        1,
                        Math.round(height * scale)
                );

        return Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                newHeight,
                true
        );
    }

    public static String getDataUri(
            String base64
    ) {

        if (base64 == null ||
                base64.trim().isEmpty()) {

            return "";
        }

        return "data:image/jpeg;base64,"
                + base64;
    }

    public static long estimateBase64Bytes(
            String base64
    ) {

        if (base64 == null ||
                base64.isEmpty()) {

            return 0;
        }

        int padding = 0;

        if (base64.endsWith("==")) {
            padding = 2;
        } else if (base64.endsWith("=")) {
            padding = 1;
        }

        return Math.max(
                0,
                (base64.length() * 3L / 4L)
                        - padding
        );
    }
}
