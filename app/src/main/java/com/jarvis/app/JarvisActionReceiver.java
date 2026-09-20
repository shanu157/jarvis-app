package com.jarvis.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class JarvisActionReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID =
            "jarvis_reminders";

    private static final String CHANNEL_NAME =
            "Jarvis Reminders";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        if (context == null) {
            return;
        }

        String label =
                intent.getStringExtra("label");

        if (label == null ||
                label.trim().isEmpty()) {

            label = "Jarvis reminder";
        }

        String actionType =
                intent.getStringExtra(
                        "action_type"
                );

        if (actionType == null ||
                actionType.trim().isEmpty()) {

            actionType = "reminder";
        }

        showNotification(
                context,
                label,
                actionType
        );
    }

    private void showNotification(
            Context context,
            String label,
            String actionType
    ) {

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        createChannel(manager);

        int notificationId =
                (int) (System.currentTimeMillis()
                        & 0x7fffffff);

        String title;

        switch (actionType) {

            case "exam_reminder":
                title = "📚 Jarvis Exam Reminder";
                break;

            case "alarm":
                title = "⏰ Jarvis Alarm";
                break;

            case "reminder":
            default:
                title = "🔔 Jarvis Reminder";
                break;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable
                                        .ic_lock_idle_alarm
                        )
                        .setContentTitle(title)
                        .setContentText(label)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(label)
                        )
                        .setPriority(
                                NotificationCompat
                                        .PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .setDefaults(
                                NotificationCompat.DEFAULT_ALL
                        );

        manager.notify(
                notificationId,
                builder.build()
        );
    }

    private void createChannel(
            NotificationManager manager
    ) {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager
                                .IMPORTANCE_HIGH
                );

        channel.setDescription(
                "Jarvis alarms and reminders"
        );

        channel.enableVibration(true);

        channel.setLockscreenVisibility(
                android.app.Notification
                        .VISIBILITY_PUBLIC
        );

        manager.createNotificationChannel(
                channel
        );
    }
}
