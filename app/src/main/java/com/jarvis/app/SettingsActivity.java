package com.jarvis.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private SettingsStorage settings;

    private EditText brainUrlInput;

    private Switch voiceOutputSwitch;
    private Switch autoSpeakSwitch;
    private Switch saveChatSwitch;
    private Switch enterSendSwitch;
    private Switch vibrationSwitch;
    private Switch showActionsSwitch;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.settings_activity
        );

        settings =
                new SettingsStorage(this);

        bindViews();

        loadSettings();

        setupListeners();
    }

    private void bindViews() {

        brainUrlInput =
                findViewById(
                        R.id.brainUrlInput
                );

        voiceOutputSwitch =
                findViewById(
                        R.id.voiceOutputSwitch
                );

        autoSpeakSwitch =
                findViewById(
                        R.id.autoSpeakSwitch
                );

        saveChatSwitch =
                findViewById(
                        R.id.saveChatSwitch
                );

        enterSendSwitch =
                findViewById(
                        R.id.enterSendSwitch
                );

        vibrationSwitch =
                findViewById(
                        R.id.vibrationSwitch
                );

        showActionsSwitch =
                findViewById(
                        R.id.showActionsSwitch
                );
    }

    private void loadSettings() {

        brainUrlInput.setText(
                settings.getBrainUrl()
        );

        voiceOutputSwitch.setChecked(
                settings.isVoiceOutputEnabled()
        );

        autoSpeakSwitch.setChecked(
                settings.isAutoSpeakEnabled()
        );

        saveChatSwitch.setChecked(
                settings.isChatSavingEnabled()
        );

        enterSendSwitch.setChecked(
                settings.isEnterToSendEnabled()
        );

        vibrationSwitch.setChecked(
                settings.isVibrationEnabled()
        );

        showActionsSwitch.setChecked(
                settings.isShowActionsEnabled()
        );
    }

    private void setupListeners() {

        Button back =
                findViewById(
                        R.id.settingsBackButton
                );

        Button testBrain =
                findViewById(
                        R.id.testBrainButton
                );

        Button accessCenter =
                findViewById(
                        R.id.accessCenterButton
                );

        Button reset =
                findViewById(
                        R.id.resetSettingsButton
                );

        back.setOnClickListener(
                v -> finish()
        );

        testBrain.setOnClickListener(
                v -> saveAndTestBrain()
        );

        voiceOutputSwitch.setOnCheckedChangeListener(
                (buttonView, checked) ->
                        settings.setVoiceOutputEnabled(
                                checked
                        )
        );

        autoSpeakSwitch.setOnCheckedChangeListener(
                (buttonView, checked) ->
                        settings.setAutoSpeakEnabled(
                                checked
                        )
        );

        saveChatSwitch.setOnCheckedChangeListener(
                (buttonView, checked) ->
                        settings.setChatSavingEnabled(
                                checked
                        )
        );

        enterSendSwitch.setOnCheckedChangeListener(
                (buttonView, checked) ->
                        settings.setEnterToSendEnabled(
                                checked
                        )
        );

        vibrationSwitch.setOnCheckedChangeListener(
                (buttonView, checked) ->
                        settings.setVibrationEnabled(
                                checked
                        )
        );

        showActionsSwitch.setOnCheckedChangeListener(
                (buttonView, checked) ->
                        settings.setShowActionsEnabled(
                                checked
                        )
        );

        accessCenter.setOnClickListener(
                v -> {

                    try {

                        startActivity(
                                new Intent(
                                        this,
                                        AccessCenterActivity.class
                                )
                        );

                    } catch (Exception e) {

                        Toast.makeText(
                                this,
                                "Access Center unavailable.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        reset.setOnClickListener(
                v -> confirmReset()
        );
    }

    private void saveAndTestBrain() {

        String url =
                brainUrlInput
                        .getText()
                        .toString()
                        .trim();

        if (url.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter a Brain URL first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (url.endsWith("/")) {
            url =
                    url.substring(
                            0,
                            url.length() - 1
                    );
        }

        settings.setBrainUrl(
                url
        );

        Toast.makeText(
                this,
                "Brain URL saved.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void confirmReset() {

        new android.app.AlertDialog.Builder(this)
                .setTitle(
                        "Reset settings?"
                )
                .setMessage(
                        "This restores the JARVIS settings to their defaults. Chat and memory are not cleared."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Reset",
                        (dialog, which) -> {

                            settings.reset();

                            loadSettings();

                            Toast.makeText(
                                    this,
                                    "Settings restored.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    @Override
    protected void onPause() {

        saveCurrentValues();

        super.onPause();
    }

    private void saveCurrentValues() {

        if (brainUrlInput == null) {
            return;
        }

        String url =
                brainUrlInput
                        .getText()
                        .toString()
                        .trim();

        if (!url.isEmpty()) {

            if (url.endsWith("/")) {

                url =
                        url.substring(
                                0,
                                url.length() - 1
                        );
            }

            settings.setBrainUrl(
                    url
            );
        }

        settings.setVoiceOutputEnabled(
                voiceOutputSwitch.isChecked()
        );

        settings.setAutoSpeakEnabled(
                autoSpeakSwitch.isChecked()
        );

        settings.setChatSavingEnabled(
                saveChatSwitch.isChecked()
        );

        settings.setEnterToSendEnabled(
                enterSendSwitch.isChecked()
        );

        settings.setVibrationEnabled(
                vibrationSwitch.isChecked()
        );

        settings.setShowActionsEnabled(
                showActionsSwitch.isChecked()
        );
    }
}
