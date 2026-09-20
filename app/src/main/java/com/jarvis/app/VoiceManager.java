package com.jarvis.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceManager {

    private final Activity activity;
    private final VoiceInputCallback callback;

    private SpeechRecognizer speechRecognizer;
    private boolean listening = false;

    public VoiceManager(
            Activity activity,
            VoiceInputCallback callback
    ) {
        this.activity = activity;
        this.callback = callback;
    }

    public boolean isAvailable() {

        return SpeechRecognizer.isRecognitionAvailable(
                activity
        );
    }

    public boolean isListening() {
        return listening;
    }

    public void start() {
        startListening();
    }

    public void stop() {
        stopListening();
    }

    public void startListening() {

        if (!isAvailable()) {

            if (callback != null) {
                callback.onVoiceError(
                        "Speech recognition is not available on this device."
                );
            }

            return;
        }

        if (
                ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            PermissionManager.requestMicrophone(
                    activity
            );

            if (callback != null) {
                callback.onVoiceError(
                        "Microphone permission is required."
                );
            }

            return;
        }

        stopListening();

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(
                        activity
                );

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params
                    ) {

                        listening = true;

                        if (callback != null) {
                            callback.onVoiceStarted();
                        }
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        // Speech has started.
                    }

                    @Override
                    public void onRmsChanged(
                            float rmsdB
                    ) {
                        // Reserved for future waveform UI.
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer
                    ) {
                        // Not required.
                    }

                    @Override
                    public void onEndOfSpeech() {

                        listening = false;

                        if (callback != null) {
                            callback.onVoiceStopped();
                        }
                    }

                    @Override
                    public void onError(
                            int error
                    ) {

                        listening = false;

                        if (callback != null) {
                            callback.onVoiceStopped();
                            callback.onVoiceError(
                                    getErrorMessage(error)
                            );
                        }
                    }

                    @Override
                    public void onResults(
                            Bundle results
                    ) {

                        listening = false;

                        if (callback != null) {
                            callback.onVoiceStopped();
                        }

                        if (results == null) {
                            return;
                        }

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (
                                matches != null
                                        &&
                                !matches.isEmpty()
                        ) {

                            String text =
                                    matches.get(0);

                            if (
                                    text != null
                                            &&
                                    !text.trim().isEmpty()
                            ) {

                                if (callback != null) {
                                    callback.onVoiceResult(
                                            text.trim()
                                    );
                                }
                            }
                        }
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults
                    ) {
                        // Final result is used for commands.
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params
                    ) {
                        // Reserved for future voice features.
                    }
                }
        );

        Intent intent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                Locale.getDefault().toLanguageTag()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        speechRecognizer.startListening(
                intent
        );
    }

    public void stopListening() {

        listening = false;

        if (speechRecognizer != null) {

            try {
                speechRecognizer.stopListening();
            } catch (Exception ignored) {
            }

            try {
                speechRecognizer.cancel();
            } catch (Exception ignored) {
            }

            try {
                speechRecognizer.destroy();
            } catch (Exception ignored) {
            }

            speechRecognizer = null;
        }

        if (callback != null) {
            callback.onVoiceStopped();
        }
    }

    public void destroy() {
        stopListening();
    }

    private String getErrorMessage(
            int error
    ) {

        switch (error) {

            case SpeechRecognizer.ERROR_AUDIO:
                return "Microphone audio error.";

            case SpeechRecognizer.ERROR_CLIENT:
                return "Speech recognition client error.";

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission was denied.";

            case SpeechRecognizer.ERROR_NETWORK:
                return "Speech recognition network error.";

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Speech recognition timed out.";

            case SpeechRecognizer.ERROR_NO_MATCH:
                return "I couldn't understand that.";

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Speech recognition is busy.";

            case SpeechRecognizer.ERROR_SERVER:
                return "Speech recognition server error.";

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "I didn't hear anything.";

            default:
                return "Voice recognition failed.";
        }
    }
}
