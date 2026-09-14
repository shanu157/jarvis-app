package com.jarvis.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "jarvis_general";
    private static final int NOTIFICATION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();
        requestNotificationPermission();

        showHomeScreen();
    }

    private void showHomeScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 40, 32, 32);
        root.setBackgroundColor(Color.rgb(5, 11, 20));

        TextView title = new TextView(this);
        title.setText("JARVIS");
        title.setTextColor(Color.rgb(0, 191, 255));
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        root.addView(title);

        TextView date = new TextView(this);
        String today = new SimpleDateFormat(
                "EEEE, dd MMMM yyyy",
                Locale.getDefault()
        ).format(new Date());

        date.setText(today);
        date.setTextColor(Color.WHITE);
        date.setTextSize(18);
        date.setGravity(Gravity.CENTER);
        date.setPadding(0, 0, 0, 30);

        root.addView(date);

        TextView status = new TextView(this);
        status.setText(
                "Jarvis native Android mode\n\n" +
                "Your personal assistant is starting.\n" +
                "Calendar, timetable, reminders and timers will be added next."
        );
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(17);
        status.setPadding(0, 0, 0, 30);

        root.addView(status);

        Button timetableButton = new Button(this);
        timetableButton.setText("Today's Timetable");
        timetableButton.setOnClickListener(v -> showTimetable());

        root.addView(timetableButton);

        Button reminderButton = new Button(this);
        reminderButton.setText("Test Notification");
        reminderButton.setOnClickListener(v -> sendTestNotification());

        root.addView(reminderButton);

        Button settingsButton = new Button(this);
        settingsButton.setText("Android Notification Settings");
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    getPackageName()
            );
            startActivity(intent);
        });

        root.addView(settingsButton);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(root);

        setContentView(scrollView);
    }

    private void showTimetable() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Today's Timetable")
                .setMessage(
                        "Default Jarvis schedule:\n\n" +
                        "Morning: Check today's plan\n" +
                        "Football: Monday–Friday, 8:00 AM\n" +
                        "Monday: Political Science tuition, 6:00–7:00 PM\n" +
                        "Wednesday: English tuition, 6:00–8:00 PM\n" +
                        "Thursday: Bengali tuition, 7:00–8:00 AM\n" +
                        "Saturday/Sunday: Computer class, 5:30–7:30 PM\n\n" +
                        "The full editable timetable will be added next."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Jarvis Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("Jarvis reminders and alerts");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_REQUEST
                );
            }
        }
    }

    private void sendTestNotification() {
        NotificationManager manager =
                (NotificationManager) getSystemService(
                        NOTIFICATION_SERVICE
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestNotificationPermission();
                return;
            }
        }

        android.app.Notification notification =
                new android.app.Notification.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Jarvis")
                        .setContentText("Jarvis notification is working.")
                        .setAutoCancel(true)
                        .build();

        if (manager != null) {
            manager.notify(1001, notification);
        }
    }
}
