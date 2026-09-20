package com.jarvis.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AccessCenterActivity extends Activity {

    private static final int REQUEST_ACCESS = 5201;

    private TextView statusText;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_access_center
        );

        bindViews();
        setupButtons();
        refreshStatus();
    }

    private void bindViews() {

        statusText =
                findViewById(
                        R.id.accessStatus
                );
    }

    private void setupButtons() {

        View back =
                findViewById(
                        R.id.accessBackButton
                );

        back.setOnClickListener(
                v -> finish()
        );

        Button microphone =
                findViewById(
                        R.id.accessMicrophone
                );

        microphone.setOnClickListener(
                v -> requestPermission(
                        Manifest.permission.RECORD_AUDIO
                )
        );

        Button camera =
                findViewById(
                        R.id.accessCamera
                );

        camera.setOnClickListener(
                v -> requestPermission(
                        Manifest.permission.CAMERA
                )
        );

        Button phone =
                findViewById(
                        R.id.accessPhone
                );

        phone.setOnClickListener(
                v -> requestPermission(
                        Manifest.permission.CALL_PHONE
                )
        );

        Button sms =
                findViewById(
                        R.id.accessSms
                );

        sms.setOnClickListener(
                v -> requestPermission(
                        Manifest.permission.SEND_SMS
                )
        );

        Button contacts =
                findViewById(
                        R.id.accessContacts
                );

        contacts.setOnClickListener(
                v -> requestPermission(
                        Manifest.permission.READ_CONTACTS
                )
        );

        Button calendar =
                findViewById(
                        R.id.accessCalendar
                );

        calendar.setOnClickListener(
                v -> requestPermission(
                        Manifest.permission.READ_CALENDAR
                )
        );

        Button location =
                findViewById(
                        R.id.accessLocation
                );

        location.setOnClickListener(
                v -> requestLocation()
        );

        Button notifications =
                findViewById(
                        R.id.accessNotifications
                );

        notifications.setOnClickListener(
                v -> requestNotifications()
        );

        Button exactAlarm =
                findViewById(
                        R.id.accessExactAlarm
                );

        exactAlarm.setOnClickListener(
                v -> openExactAlarmSettings()
        );

        Button done =
                findViewById(
                        R.id.accessDoneButton
                );

        done.setOnClickListener(
                v -> finish()
        );
    }

    private void requestPermission(
            String permission
    ) {

        if (permission == null) {
            return;
        }

        if (Build.VERSION.SDK_INT < 23) {
            return;
        }

        if (checkSelfPermission(permission)
                == PackageManager.PERMISSION_GRANTED) {

            refreshStatus();
            return;
        }

        requestPermissions(
                new String[]{permission},
                REQUEST_ACCESS
        );
    }

    private void requestLocation() {

        if (Build.VERSION.SDK_INT < 23) {
            return;
        }

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            refreshStatus();
            return;
        }

        requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_ACCESS
        );
    }

    private void requestNotifications() {

        if (Build.VERSION.SDK_INT < 33) {
            refreshStatus();
            return;
        }

        requestPermission(
                Manifest.permission.POST_NOTIFICATIONS
        );
    }

    private void refreshStatus() {

        if (statusText == null) {
            return;
        }

        List<String> granted =
                new ArrayList<>();

        List<String> missing =
                new ArrayList<>();

        checkState(
                Manifest.permission.RECORD_AUDIO,
                "Microphone",
                granted,
                missing
        );

        checkState(
                Manifest.permission.CAMERA,
                "Camera / Vision",
                granted,
                missing
        );

        checkState(
                Manifest.permission.CALL_PHONE,
                "Phone",
                granted,
                missing
        );

        checkState(
                Manifest.permission.SEND_SMS,
                "SMS",
                granted,
                missing
        );

        checkState(
                Manifest.permission.READ_CONTACTS,
                "Contacts",
                granted,
                missing
        );

        checkState(
                Manifest.permission.READ_CALENDAR,
                "Calendar",
                granted,
                missing
        );

        checkState(
                Manifest.permission.ACCESS_FINE_LOCATION,
                "Location",
                granted,
                missing
        );

        if (Build.VERSION.SDK_INT >= 33) {

            checkState(
                    Manifest.permission.POST_NOTIFICATIONS,
                    "Notifications",
                    granted,
                    missing
            );
        }

        StringBuilder text =
                new StringBuilder();

        text.append("ACCESS STATUS\n\n");

        text.append("Granted: ");

        text.append(
                granted.isEmpty()
                        ? "None"
                        : join(granted)
        );

        text.append("\n\n");

        text.append("Not granted: ");

        text.append(
                missing.isEmpty()
                        ? "None"
                        : join(missing)
        );

        statusText.setText(
                text.toString()
        );
    }

    private void checkState(
            String permission,
            String name,
            List<String> granted,
            List<String> missing
    ) {

        if (Build.VERSION.SDK_INT < 23 ||
                checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED) {

            granted.add(name);

        } else {

            missing.add(name);
        }
    }

    private String join(
            List<String> values
    ) {

        StringBuilder result =
                new StringBuilder();

        for (int i = 0; i < values.size(); i++) {

            if (i > 0) {
                result.append(", ");
            }

            result.append(
                    values.get(i)
            );
        }

        return result.toString();
    }

    private void openExactAlarmSettings() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    );

            startActivity(intent);

        } catch (Exception e) {

            try {

                startActivity(
                        new Intent(
                                Settings.ACTION_SETTINGS
                        )
                );

            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        refreshStatus();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_ACCESS) {
            refreshStatus();
        }
    }
}
