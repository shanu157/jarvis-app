package com.jarvis.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public final class PermissionManager {

    public static final int REQUEST_MICROPHONE = 1101;
    public static final int REQUEST_CAMERA = 1102;
    public static final int REQUEST_PHONE = 1103;
    public static final int REQUEST_SMS = 1104;
    public static final int REQUEST_NOTIFICATIONS = 1105;
    public static final int REQUEST_CALENDAR = 1106;
    public static final int REQUEST_CONTACTS = 1107;
    public static final int REQUEST_LOCATION = 1108;

    private PermissionManager() {
        // Utility class.
    }

    public static boolean hasPermission(
            Context context,
            String permission
    ) {

        return ContextCompat.checkSelfPermission(
                context,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasMicrophone(
            Context context
    ) {

        return hasPermission(
                context,
                Manifest.permission.RECORD_AUDIO
        );
    }

    public static boolean hasCamera(
            Context context
    ) {

        return hasPermission(
                context,
                Manifest.permission.CAMERA
        );
    }

    public static boolean hasPhone(
            Context context
    ) {

        return hasPermission(
                context,
                Manifest.permission.CALL_PHONE
        );
    }

    public static boolean hasSms(
            Context context
    ) {

        return hasPermission(
                context,
                Manifest.permission.SEND_SMS
        );
    }

    public static boolean hasNotifications(
            Context context
    ) {

        if (android.os.Build.VERSION.SDK_INT < 33) {
            return true;
        }

        return hasPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        );
    }

    public static boolean hasCalendar(
            Context context
    ) {

        return hasPermission(
                context,
                Manifest.permission.READ_CALENDAR
        )
                &&
                hasPermission(
                        context,
                        Manifest.permission.WRITE_CALENDAR
                );
    }

    public static boolean hasContacts(
            Context context
    ) {

        return hasPermission(
                context,
                Manifest.permission.READ_CONTACTS
        )
                &&
                hasPermission(
                        context,
                        Manifest.permission.WRITE_CONTACTS
                );
    }

    public static boolean hasLocation(
            Context context
    ) {

        boolean fine =
                hasPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                );

        boolean coarse =
                hasPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                );

        return fine || coarse;
    }

    public static void requestMicrophone(
            Activity activity
    ) {

        request(
                activity,
                new String[]{
                        Manifest.permission.RECORD_AUDIO
                },
                REQUEST_MICROPHONE
        );
    }

    public static void requestCamera(
            Activity activity
    ) {

        request(
                activity,
                new String[]{
                        Manifest.permission.CAMERA
                },
                REQUEST_CAMERA
        );
    }

    public static void requestPhone(
            Activity activity
    ) {

        request(
                activity,
                new String[]{
                        Manifest.permission.CALL_PHONE
                },
                REQUEST_PHONE
        );
    }

    public static void requestSms(
            Activity activity
    ) {

        request(
                activity,
                new String[]{
                        Manifest.permission.SEND_SMS
                },
                REQUEST_SMS
        );
    }

    public static void requestNotifications(
            Activity activity
    ) {

        if (android.os.Build.VERSION.SDK_INT < 33) {
            return;
        }

        request(
                activity,
                new String[]{
                        Manifest.permission.POST_NOTIFICATIONS
                },
                REQUEST_NOTIFICATIONS
        );
    }

    public static void requestCalendar(
            Activity activity
    ) {

        request(
                activity,
                new String[]{
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                },
                REQUEST_CALENDAR
        );
    }

    public static void requestContacts(
            Activity activity
    ) {

        request(
                activity,
                new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS
                },
                REQUEST_CONTACTS
        );
    }

    public static void requestLocation(
            Activity activity
    ) {

        request(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_LOCATION
        );
    }

    public static void request(
            Activity activity,
            String[] permissions,
            int requestCode
    ) {

        if (activity == null
                || permissions == null
                || permissions.length == 0) {
            return;
        }

        List<String> missing =
                new ArrayList<>();

        for (String permission : permissions) {

            if (!hasPermission(
                    activity,
                    permission
            )) {

                missing.add(permission);
            }
        }

        if (missing.isEmpty()) {
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                missing.toArray(
                        new String[0]
                ),
                requestCode
        );
    }

    public static String getPermissionName(
            String permission
    ) {

        if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
            return "Microphone";
        }

        if (Manifest.permission.CAMERA.equals(permission)) {
            return "Camera";
        }

        if (Manifest.permission.CALL_PHONE.equals(permission)) {
            return "Phone";
        }

        if (Manifest.permission.SEND_SMS.equals(permission)) {
            return "SMS";
        }

        if (Manifest.permission.POST_NOTIFICATIONS.equals(permission)) {
            return "Notifications";
        }

        if (Manifest.permission.READ_CALENDAR.equals(permission)
                ||
                Manifest.permission.WRITE_CALENDAR.equals(permission)) {
            return "Calendar";
        }

        if (Manifest.permission.READ_CONTACTS.equals(permission)
                ||
                Manifest.permission.WRITE_CONTACTS.equals(permission)) {
            return "Contacts";
        }

        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)
                ||
                Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
            return "Location";
        }

        return "Android permission";
    }
}
