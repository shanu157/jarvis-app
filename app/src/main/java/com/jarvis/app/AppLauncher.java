package com.jarvis.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.HashMap;
import java.util.List;
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

        aliases.put("chrome", "com.android.chrome");
        aliases.put("google chrome", "com.android.chrome");

        aliases.put("youtube", "com.google.android.youtube");
        aliases.put("whatsapp", "com.whatsapp");

        aliases.put("telegram", "org.telegram.messenger");
        aliases.put("instagram", "com.instagram.android");
        aliases.put("facebook", "com.facebook.katana");
        aliases.put("gmail", "com.google.android.gm");

        aliases.put(
                "maps",
                "com.google.android.apps.maps"
        );

        aliases.put(
                "google maps",
                "com.google.android.apps.maps"
        );

        aliases.put("settings", "com.android.settings");

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

    public boolean open(String app) {

        if (app == null ||
                app.trim().isEmpty()) {

            return false;
        }

        String clean =
                app.trim();

        /*
         * First try aliases and explicit package names.
         */
        String packageName =
                resolvePackage(clean);

        if (packageName != null &&
                !packageName.isEmpty()) {

            if (openPackage(packageName)) {
                return true;
            }
        }

        /*
         * If the package was not found, search all visible
         * launcher applications by their displayed name.
         *
         * This handles third-party apps such as:
         *
         *   Minecraft
         *   Spotify
         *   Discord
         *   VLC
         *   etc.
         */
        packageName =
                findPackageByAppName(clean);

        if (packageName != null &&
                !packageName.isEmpty()) {

            return openPackage(packageName);
        }

        /*
         * Last chance: the supplied string itself may be
         * an actual package name.
         */
        if (!clean.contains(".")) {
            return false;
        }

        return openPackage(clean);
    }

    private boolean openPackage(
            String packageName
    ) {

        if (packageName == null ||
                packageName.trim().isEmpty()) {

            return false;
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

    /**
     * Find an installed launcher application by its
     * human-readable application label.
     */
    private String findPackageByAppName(
            String requestedName
    ) {

        if (requestedName == null ||
                requestedName.trim().isEmpty()) {

            return "";
        }

        String requested =
                normalizeName(requestedName);

        Intent launcherIntent =
                new Intent(
                        Intent.ACTION_MAIN
                );

        launcherIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> apps =
                packageManager.queryIntentActivities(
                        launcherIntent,
                        PackageManager.MATCH_ALL
                );

        /*
         * Exact label match first.
         */
        for (ResolveInfo info : apps) {

            if (info == null ||
                    info.activityInfo == null) {

                continue;
            }

            CharSequence label =
                    info.loadLabel(packageManager);

            if (label == null) {
                continue;
            }

            String labelName =
                    normalizeName(
                            label.toString()
                    );

            if (requested.equals(labelName)) {

                return info.activityInfo.packageName;
            }
        }

        /*
         * Then allow a simple partial match.
         *
         * Example:
         *
         *   "open Minecraft game"
         *
         * can still find an app labelled "Minecraft".
         */
        for (ResolveInfo info : apps) {

            if (info == null ||
                    info.activityInfo == null) {

                continue;
            }

            CharSequence label =
                    info.loadLabel(packageManager);

            if (label == null) {
                continue;
            }

            String labelName =
                    normalizeName(
                            label.toString()
                    );

            if (labelName.contains(requested) ||
                    requested.contains(labelName)) {

                return info.activityInfo.packageName;
            }
        }

        return "";
    }

    private String normalizeName(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.getDefault())
                .replaceAll("\\s+", " ");
    }

    public boolean isInstalled(String app) {

        if (app == null ||
                app.trim().isEmpty()) {

            return false;
        }

        String packageName =
                resolvePackage(app);

        if (packageName == null ||
                packageName.isEmpty()) {

            packageName =
                    findPackageByAppName(app);
        }

        if (packageName == null ||
                packageName.isEmpty()) {

            return false;
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

    public String resolvePackage(String app) {

        if (app == null) {
            return "";
        }

        String clean =
                app.trim();

        if (clean.isEmpty()) {
            return "";
        }

        /*
         * Explicit package name.
         */
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
                normalizeName(clean);

        String packageName =
                aliases.get(key);

        if (packageName != null) {
            return packageName;
        }

        /*
         * Dynamic application-name lookup.
         */
        return findPackageByAppName(clean);
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

    public boolean openAppInfo(String app) {

        String packageName =
                resolvePackage(app);

        if (packageName == null ||
                packageName.isEmpty()) {

            return false;
        }

        try {

            Intent intent =
                    new Intent(
                            android.provider.Settings
                                    .ACTION_APPLICATION_DETAILS_SETTINGS
                    );

            intent.setData(
                    android.net.Uri.parse(
                            "package:" + packageName
                    )
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
}
