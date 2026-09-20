package com.jarvis.app;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.UUID;

public class ActionExecutor {

    public interface Callback {
        void onComplete(String message);
        void onError(String error);
        void onConfirmationRequired(JSONObject action);
    }

    private final Context context;
    private final ActionHistory history;

    public ActionExecutor(Context context) {
        this.context = context.getApplicationContext();
        this.history = new ActionHistory(context);
    }

    public void executeAction(JSONObject action) {
        if (action == null) {
            return;
        }

        JSONObject plan = new JSONObject();
        try {
            org.json.JSONArray actions = new org.json.JSONArray();
            actions.put(action);
            plan.put("actions", actions);
            executePlan(plan, null);
        } catch (org.json.JSONException e) {
            android.util.Log.e("JARVIS", "Unable to execute action", e);
        }
    }

    public void executePlan(JSONObject plan, Callback callback) {
        try {
            JSONArray actions = plan.optJSONArray("actions");

            if (actions == null || actions.length() == 0) {
                callback.onComplete(
                        plan.optString("reply", "Done.")
                );
                return;
            }

            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);

                if (action.optBoolean(
                        "requires_confirmation",
                        false
                )) {
                    callback.onConfirmationRequired(action);
                    return;
                }
            }

            StringBuilder output = new StringBuilder();

            for (int i = 0; i < actions.length(); i++) {
                executeSingle(
                        actions.getJSONObject(i),
                        output
                );
            }

            String reply = output.length() > 0
                    ? output.toString().trim()
                    : plan.optString("reply", "Done.");

            callback.onComplete(reply);

        } catch (Exception e) {
            callback.onError(
                    e.getMessage() == null
                            ? "Action failed."
                            : e.getMessage()
            );
        }
    }

    private void executeSingle(
            JSONObject action,
            StringBuilder output
    ) throws Exception {

        String type = action.optString("type", "");

        switch (type) {

            case "alarm":
            case "reminder":
            case "exam_reminder":
                scheduleAlarm(action, output);
                break;

            case "timer":
                scheduleTimer(action, output);
                break;

            case "torch":
                boolean enabled = action.optBoolean("enabled", true);

                setTorch(enabled);

                output.append(
                        enabled
                                ? "Flashlight turned on.\n"
                                : "Flashlight turned off.\n"
                );
                break;

            case "call":
                makeCall(action.optString("number"));
                output.append(
                        "Calling "
                                + action.optString(
                                        "contact",
                                        action.optString("number")
                                )
                                + ".\n"
                );
                break;

            case "sms":
                sendSms(
                        action.optString("number"),
                        action.optString("message")
                );
                output.append("Message sent.\n");
                break;

            case "open_url":
                openUrl(action.optString("url"));
                output.append("Opening it.\n");
                break;

            case "open_app":
                openApp(action.optString("package"));
                output.append("Opening the app.\n");
                break;

            default:
                output.append(
                        "I understood the request, but that action "
                                + "is not available yet.\n"
                );
                break;
        }
    }

    private void scheduleAlarm(
            JSONObject action,
            StringBuilder output
    ) throws Exception {

        String date = action.optString("date", "");
        String time = action.optString("time", "");

        String label = action.optString(
                "label",
                "Jarvis reminder"
        );

        if (date.isEmpty() || time.isEmpty()) {
            throw new Exception("Missing date or time.");
        }

        String[] d = date.split("-");
        String[] t = time.split(":");

        if (d.length != 3 || t.length < 2) {
            throw new Exception("Invalid date or time.");
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(
                Integer.parseInt(d[0]),
                Integer.parseInt(d[1]) - 1,
                Integer.parseInt(d[2]),
                Integer.parseInt(t[0]),
                Integer.parseInt(t[1]),
                0
        );

        calendar.set(Calendar.MILLISECOND, 0);

        long trigger = calendar.getTimeInMillis();

        if (trigger <= System.currentTimeMillis()) {
            throw new Exception(
                    "That time has already passed."
            );
        }

        String id = UUID.randomUUID().toString();

        Intent intent = new Intent(
                context,
                JarvisActionReceiver.class
        );

        intent.setAction(
                "com.jarvis.app.JARVIS_ACTION"
        );

        intent.putExtra("id", id);
        intent.putExtra("label", label);
        intent.putExtra(
                "action_type",
                action.optString("type", "alarm")
        );

        PendingIntent pending =
                PendingIntent.getBroadcast(
                        context,
                        id.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager manager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (manager == null) {
            throw new Exception(
                    "Alarm service unavailable."
            );
        }

        if (Build.VERSION.SDK_INT >= 31
                && !manager.canScheduleExactAlarms()) {

            Intent settingsIntent = new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            );

            settingsIntent.setData(
                    Uri.parse(
                            "package:"
                                    + context.getPackageName()
                    )
            );

            settingsIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(settingsIntent);

            throw new Exception(
                    "Please allow exact alarms, then try again."
            );
        }

        if (Build.VERSION.SDK_INT >= 23) {
            manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pending
            );
        } else {
            manager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pending
            );
        }

        history.add(
                action.optString("type", "alarm"),
                label,
                id,
                System.currentTimeMillis()
        );

        output.append(
                "Scheduled "
                        + label
                        + " at "
                        + time
                        + ".\n"
        );
    }

    private void scheduleTimer(
            JSONObject action,
            StringBuilder output
    ) {

        int seconds = action.optInt(
                "seconds",
                60
        );

        String label = action.optString(
                "label",
                "Jarvis timer"
        );

        Intent intent = new Intent(
                AlarmClock.ACTION_SET_TIMER
        );

        intent.putExtra(
                AlarmClock.EXTRA_LENGTH,
                seconds
        );

        intent.putExtra(
                AlarmClock.EXTRA_MESSAGE,
                label
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        context.startActivity(intent);

        output.append(
                "Timer set for "
                        + seconds
                        + " seconds.\n"
        );
    }

    private void setTorch(boolean enabled)
            throws Exception {

        if (Build.VERSION.SDK_INT < 23) {
            throw new Exception(
                    "Flashlight requires Android 6 or newer."
            );
        }

        CameraManager manager =
                (CameraManager) context.getSystemService(
                        Context.CAMERA_SERVICE
                );

        if (manager == null) {
            throw new Exception(
                    "Camera service unavailable."
            );
        }

        String cameraId = null;

        try {
            for (String id : manager.getCameraIdList()) {

                CameraCharacteristics characteristics =
                        manager.getCameraCharacteristics(id);

                Boolean flash =
                        characteristics.get(
                                CameraCharacteristics
                                        .FLASH_INFO_AVAILABLE
                        );

                Integer facing =
                        characteristics.get(
                                CameraCharacteristics
                                        .LENS_FACING
                        );

                if (Boolean.TRUE.equals(flash)
                        && (facing == null
                        || facing
                        == CameraCharacteristics
                        .LENS_FACING_BACK)) {

                    cameraId = id;
                    break;
                }
            }

        } catch (CameraAccessException e) {
            throw new Exception(
                    "Cannot access camera."
            );
        }

        if (cameraId == null) {
            throw new Exception(
                    "No flashlight found."
            );
        }

        try {
            manager.setTorchMode(
                    cameraId,
                    enabled
            );
        } catch (CameraAccessException e) {
            throw new Exception(
                    "Could not control flashlight."
            );
        }
    }

    private void makeCall(String number)
            throws Exception {

        if (number == null
                || number.trim().isEmpty()) {

            throw new Exception(
                    "No phone number found."
            );
        }

        Intent intent;

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED) {

            intent = new Intent(
                    Intent.ACTION_CALL,
                    Uri.parse(
                            "tel:"
                                    + Uri.encode(number)
                    )
            );

        } else {

            intent = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse(
                            "tel:"
                                    + Uri.encode(number)
                    )
            );
        }

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        context.startActivity(intent);
    }

    private void sendSms(
            String number,
            String message
    ) throws Exception {

        if (number == null
                || number.trim().isEmpty()) {

            throw new Exception(
                    "No phone number found."
            );
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            throw new Exception(
                    "SMS permission is not granted."
            );
        }

        if (message == null
                || message.trim().isEmpty()) {

            throw new Exception(
                    "Message is empty."
            );
        }

        SmsManager.getDefault()
                .sendTextMessage(
                        number,
                        null,
                        message,
                        null,
                        null
                );
    }

    private void openUrl(String url)
            throws Exception {

        if (url == null
                || url.trim().isEmpty()) {

            throw new Exception("No URL.");
        }

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        context.startActivity(intent);
    }

    private void openApp(String packageName)
            throws Exception {

        if (packageName == null
                || packageName.trim().isEmpty()) {

            throw new Exception(
                    "No application package."
            );
        }

        Intent launch =
                context.getPackageManager()
                        .getLaunchIntentForPackage(
                                packageName
                        );

        if (launch == null) {
            throw new Exception(
                    "Application not installed."
            );
        }

        launch.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        context.startActivity(launch);
    }

    public void undoLast() {

        JSONObject last = history.last();

        if (last == null) {
            Toast.makeText(
                    context,
                    "Nothing to undo.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {

            String id = last.optString("id", "");

            if (!id.isEmpty()) {

                Intent intent = new Intent(
                        context,
                        JarvisActionReceiver.class
                );

                intent.setAction(
                        "com.jarvis.app.JARVIS_ACTION"
                );

                PendingIntent pending =
                        PendingIntent.getBroadcast(
                                context,
                                id.hashCode(),
                                intent,
                                PendingIntent.FLAG_NO_CREATE
                                        | PendingIntent.FLAG_IMMUTABLE
                        );

                AlarmManager manager =
                        (AlarmManager) context.getSystemService(
                                Context.ALARM_SERVICE
                        );

                if (pending != null
                        && manager != null) {

                    manager.cancel(pending);
                    pending.cancel();
                }
            }

            history.removeLast();

            Toast.makeText(
                    context,
                    "Last Jarvis action cancelled.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    context,
                    "Could not undo last action.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
