package com.jarvis.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID =
            "jarvis_main";

    private static final String CHANNEL_NAME =
            "JARVIS";

    private static final String CHANNEL_DESCRIPTION =
            "JARVIS reminders, alarms and assistant updates";

    private final Context context;

    public NotificationHelper(Context context) {

        this.context =
                context.getApplicationContext();

        createChannel();
    }

    private void createChannel() {

        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );

        channel.setDescription(
                CHANNEL_DESCRIPTION
        );

        channel.enableVibration(
                true
        );

        manager.createNotificationChannel(
                channel
        );
    }

    public void show(
            int notificationId,
            String title,
            String message
    ) {

        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(
                        android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent =
                new Intent(
                        context,
                        MainActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                );

        builder.setSmallIcon(
                android.R.drawable.ic_lock_idle_alarm
        );

        builder.setContentTitle(
                safe(title, "JARVIS")
        );

        builder.setContentText(
                safe(message, "JARVIS update")
        );

        builder.setStyle(
                new NotificationCompat.BigTextStyle()
                        .bigText(
                                safe(
                                        message,
                                        "JARVIS update"
                                )
                        )
        );

        builder.setContentIntent(
                pendingIntent
        );

        builder.setAutoCancel(
                true
        );

        builder.setPriority(
                NotificationCompat.PRIORITY_HIGH
        );

        builder.setCategory(
                NotificationCompat.CATEGORY_REMINDER
        );

        NotificationManagerCompat.from(
                context
        ).notify(
                notificationId,
                builder.build()
        );
    }

    public void showReminder(
            int notificationId,
            String title,
            String message
    ) {

        show(
                notificationId,
                title,
                message
        );
    }

    public void showAction(
            int notificationId,
            String message
    ) {

        show(
                notificationId,
                "JARVIS",
                message
        );
    }

    public void cancel(
            int notificationId
    ) {

        NotificationManagerCompat
                .from(context)
                .cancel(notificationId);
    }

    public void cancelAll() {

        NotificationManagerCompat
                .from(context)
                .cancelAll();
    }

    private String safe(
            String value,
            String fallback
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return fallback;
        }

        return value.trim();
    }
}
