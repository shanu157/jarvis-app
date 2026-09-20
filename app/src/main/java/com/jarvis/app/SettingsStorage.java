package com.jarvis.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStorage {

    private static final String PREFS =
            "jarvis_settings";

    private static final String KEY_BRAIN_URL =
            "brain_url";

    private static final String KEY_VOICE_OUTPUT =
            "voice_output";

    private static final String KEY_AUTO_SPEAK =
            "auto_speak";

    private static final String KEY_SAVE_CHAT =
            "save_chat";

    private static final String KEY_VIBRATION =
            "vibration";

    private static final String KEY_ENTER_SEND =
            "enter_send";

    private static final String KEY_SHOW_ACTIONS =
            "show_actions";

    private static final String KEY_FIRST_RUN =
            "first_run";

    private final SharedPreferences prefs;

    public SettingsStorage(Context context) {

        prefs =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREFS,
                                Context.MODE_PRIVATE
                        );
    }

    public String getBrainUrl() {

        return prefs.getString(
                KEY_BRAIN_URL,
                "https://shanu11.pythonanywhere.com"
        );
    }

    public void setBrainUrl(
            String url
    ) {

        if (url == null) {
            return;
        }

        String clean =
                url.trim();

        if (clean.isEmpty()) {
            return;
        }

        prefs.edit()
                .putString(
                        KEY_BRAIN_URL,
                        clean
                )
                .apply();
    }

    public boolean isVoiceOutputEnabled() {

        return prefs.getBoolean(
                KEY_VOICE_OUTPUT,
                true
        );
    }

    public void setVoiceOutputEnabled(
            boolean enabled
    ) {

        prefs.edit()
                .putBoolean(
                        KEY_VOICE_OUTPUT,
                        enabled
                )
                .apply();
    }

    public boolean isAutoSpeakEnabled() {

        return prefs.getBoolean(
                KEY_AUTO_SPEAK,
                false
        );
    }

    public void setAutoSpeakEnabled(
            boolean enabled
    ) {

        prefs.edit()
                .putBoolean(
                        KEY_AUTO_SPEAK,
                        enabled
                )
                .apply();
    }

    public boolean isChatSavingEnabled() {

        return prefs.getBoolean(
                KEY_SAVE_CHAT,
                true
        );
    }

    public void setChatSavingEnabled(
            boolean enabled
    ) {

        prefs.edit()
                .putBoolean(
                        KEY_SAVE_CHAT,
                        enabled
                )
                .apply();
    }

    public boolean isVibrationEnabled() {

        return prefs.getBoolean(
                KEY_VIBRATION,
                true
        );
    }

    public void setVibrationEnabled(
            boolean enabled
    ) {

        prefs.edit()
                .putBoolean(
                        KEY_VIBRATION,
                        enabled
                )
                .apply();
    }

    public boolean isEnterToSendEnabled() {

        return prefs.getBoolean(
                KEY_ENTER_SEND,
                true
        );
    }

    public void setEnterToSendEnabled(
            boolean enabled
    ) {

        prefs.edit()
                .putBoolean(
                        KEY_ENTER_SEND,
                        enabled
                )
                .apply();
    }

    public boolean isShowActionsEnabled() {

        return prefs.getBoolean(
                KEY_SHOW_ACTIONS,
                true
        );
    }

    public void setShowActionsEnabled(
            boolean enabled
    ) {

        prefs.edit()
                .putBoolean(
                        KEY_SHOW_ACTIONS,
                        enabled
                )
                .apply();
    }

    public boolean isFirstRun() {

        return prefs.getBoolean(
                KEY_FIRST_RUN,
                true
        );
    }

    public void setFirstRunComplete() {

        prefs.edit()
                .putBoolean(
                        KEY_FIRST_RUN,
                        false
                )
                .apply();
    }

    public void resetSettings() {

        prefs.edit()
                .clear()
                .apply();
    }
}
