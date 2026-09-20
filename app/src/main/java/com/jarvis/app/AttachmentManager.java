package com.jarvis.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class AttachmentManager {

    public static final int REQUEST_FILE =
            4101;

    public static final int REQUEST_IMAGE =
            4102;

    public interface Callback {
        void onFileSelected(
                Uri uri,
                String mimeType
        );

        void onAttachmentCancelled();
    }

    private final Activity activity;
    private final Callback callback;

    public AttachmentManager(
            Activity activity,
            Callback callback
    ) {
        this.activity = activity;
        this.callback = callback;
    }

    public void openFilePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "*/*"
        );

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                false
        );

        activity.startActivityForResult(
                intent,
                REQUEST_FILE
        );
    }

    public void openImagePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "image/*"
        );

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                false
        );

        activity.startActivityForResult(
                intent,
                REQUEST_IMAGE
        );
    }

    public boolean handleActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        if (requestCode != REQUEST_FILE &&
                requestCode != REQUEST_IMAGE) {

            return false;
        }

        if (resultCode != Activity.RESULT_OK ||
                data == null) {

            if (callback != null) {
                callback.onAttachmentCancelled();
            }

            return true;
        }

        Uri uri =
                data.getData();

        if (uri == null) {

            if (callback != null) {
                callback.onAttachmentCancelled();
            }

            return true;
        }

        persistReadPermission(
                data,
                uri
        );

        String mimeType =
                activity.getContentResolver()
                        .getType(uri);

        if (mimeType == null) {

            if (requestCode == REQUEST_IMAGE) {
                mimeType = "image/*";
            } else {
                mimeType = "application/octet-stream";
            }
        }

        if (callback != null) {

            callback.onFileSelected(
                    uri,
                    mimeType
            );
        }

        return true;
    }

    private void persistReadPermission(
            Intent data,
            Uri uri
    ) {

        int flags =
                data.getFlags()
                        &
                        (
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        );

        if ((flags &
                Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {

            flags |=
                    Intent.FLAG_GRANT_READ_URI_PERMISSION;
        }

        try {

            activity.getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            flags
                    );

        } catch (Exception ignored) {
            // Some providers do not support
            // persistable URI permissions.
        }
    }

    public static boolean isImage(
            String mimeType
    ) {

        return mimeType != null &&
                mimeType.toLowerCase()
                        .startsWith("image/");
    }

    public static boolean isSupportedImage(
            String mimeType
    ) {

        if (!isImage(mimeType)) {
            return false;
        }

        return "image/jpeg".equalsIgnoreCase(mimeType)
                ||
                "image/jpg".equalsIgnoreCase(mimeType)
                ||
                "image/png".equalsIgnoreCase(mimeType)
                ||
                "image/webp".equalsIgnoreCase(mimeType)
                ||
                "image/heic".equalsIgnoreCase(mimeType)
                ||
                "image/heif".equalsIgnoreCase(mimeType);
    }
}
