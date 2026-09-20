package com.jarvis.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppLauncher {

    private final Context context;
    private final PackageManager packageManager;

    private final Map<String, String> aliases =
            new HashMap<>();

    public AppLauncher(Context context) {

        this.context =
                context.getApplicationContext();

        this.packageManager =
                this.context.getPackageManager();

        setupAliases();
    }

    private void setupAliases() {

        aliases.put(
                "chrome",
                "com.android.chrome"
        );

        aliases.put(
                "google chrome",
                "com.android.chrome"
        );

        aliases.put(
                "youtube",
                "com.google.android.youtube"
        );

        aliases.put(
                "whatsapp",
                "com.whatsapp"
        );

        aliases.put(
                "telegram",
                "org.telegram.messenger"
        );

        aliases.put(
                "instagram",
                "com.instagram.android"
        );

        aliases.put(
                "facebook",
                "com.facebook.katana"
        );

        aliases.put(
                "gmail",
                "com.google.android.gm"
        );

        aliases.put(
                "maps",
                "com.google.android.apps.maps"
        );

        aliases.put(
                "google maps",
                "com.google.android.apps.maps"
        );

        aliases.put(
                "settings",
                "com.android.settings"
        );

        aliases.put(
                "calculator",
                "com.google.android.calculator"
        );

        aliases.put(
                "clock",
                "com.google.android.deskclock"
        );

        aliases.put(
                "phone",
                "com.google.android.dialer"
        );

        aliases.put(
                "contacts",
                "com.google.android.contacts"
        );
    }

    public boolean open(
            String app
    ) {

        if (app == null ||
                app.trim().isEmpty()) {

            return false;
        }

        String clean =
                app.trim();

        String packageName =
                resolvePackage(clean);

        if (packageName == null ||
                packageName.isEmpty()) {

            packageName =
                    clean;
        }

        Intent launchIntent =
                packageManager.getLaunchIntentForPackage(
                        packageName
                );

        if (launchIntent == null) {
            return false;
        }

        launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        try {

            context.startActivity(
                    launchIntent
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    public boolean isInstalled(
            String app
    ) {

        if (app == null ||
                app.trim().isEmpty()) {

            return false;
        }

        String packageName =
                resolvePackage(app);

        if (packageName == null) {
            packageName =
                    app.trim();
        }

        try {

            packageManager.getPackageInfo(
                    packageName,
                    0
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    public String resolvePackage(
            String app
    ) {

        if (app == null) {
            return "";
        }

        String clean =
                app.trim();

        if (clean.isEmpty()) {
            return "";
        }

        if (clean.contains(".")) {

            try {

                packageManager.getPackageInfo(
                        clean,
                        0
                );

                return clean;

            } catch (Exception ignored) {
            }
        }

        String key =
                clean.toLowerCase(
                        Locale.getDefault()
                );

        String packageName =
                aliases.get(key);

        if (packageName != null) {
            return packageName;
        }

        return "";
    }

    public boolean openSettings() {

        try {

            Intent intent =
                    new Intent(
                            android.provider.Settings.ACTION_SETTINGS
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(intent);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    public boolean openAppInfo(
            String app
    ) {

        String packageName =
                resolvePackage(app);

        if (packageName == null ||
                packageName.isEmpty()) {

            return false;
        }

        try {

            Intent intent =
                    new Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    );

            intent.setData(
                    android.net.Uri.parse(
                            "package:" + packageName
                    )
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(
                    intent
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}
