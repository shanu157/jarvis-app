package com.jarvis.app;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

public class TextToSpeechManager {

    public interface Callback {
        void onReady();

        void onStarted();

        void onFinished();

        void onError(String message);
    }

    private final Context context;
    private final Callback callback;

    private TextToSpeech textToSpeech;
    private boolean ready = false;
    private boolean speaking = false;

    public TextToSpeechManager(
            Context context,
            Callback callback
    ) {
        this.context = context.getApplicationContext();
        this.callback = callback;

        initialize();
    }

    private void initialize() {

        textToSpeech =
                new TextToSpeech(
                        context,
                        status -> {

                            if (
                                    status
                                            == TextToSpeech.SUCCESS
                            ) {

                                int result =
                                        textToSpeech.setLanguage(
                                                Locale.getDefault()
                                        );

                                ready =
                                        result
                                                != TextToSpeech.LANG_MISSING_DATA
                                                &&
                                                result
                                                        != TextToSpeech.LANG_NOT_SUPPORTED;

                                textToSpeech.setSpeechRate(
                                        0.95f
                                );

                                textToSpeech.setPitch(
                                        0.95f
                                );

                                textToSpeech.setOnUtteranceProgressListener(
                                        new UtteranceProgressListener() {

                                            @Override
                                            public void onStart(
                                                    String utteranceId
                                            ) {

                                                speaking = true;

                                                if (callback != null) {
                                                    callback.onStarted();
                                                }
                                            }

                                            @Override
                                            public void onDone(
                                                    String utteranceId
                                            ) {

                                                speaking = false;

                                                if (callback != null) {
                                                    callback.onFinished();
                                                }
                                            }

                                            @Override
                                            public void onError(
                                                    String utteranceId
                                            ) {

                                                speaking = false;

                                                if (callback != null) {
                                                    callback.onError(
                                                            "Text-to-speech failed."
                                                    );
                                                }
                                            }

                                            @Override
                                            public void onError(
                                                    String utteranceId,
                                                    int errorCode
                                            ) {

                                                speaking = false;

                                                if (callback != null) {
                                                    callback.onError(
                                                            "Text-to-speech failed."
                                                    );
                                                }
                                            }
                                        }
                                );

                                if (callback != null) {
                                    callback.onReady();
                                }

                            } else {

                                ready = false;

                                if (callback != null) {
                                    callback.onError(
                                            "Text-to-speech is unavailable."
                                    );
                                }
                            }
                        }
                );
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public void speak(
            String text
    ) {

        if (!ready) {

            if (callback != null) {
                callback.onError(
                        "Text-to-speech is not ready."
                );
            }

            return;
        }

        if (
                text == null
                        ||
                text.trim().isEmpty()
        ) {
            return;
        }

        String clean =
                cleanForSpeech(text);

        if (clean.isEmpty()) {
            return;
        }

        stop();

        String utteranceId =
                "jarvis_"
                        +
                        System.currentTimeMillis();

        textToSpeech.speak(
                clean,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
        );
    }

    public void stop() {

        if (
                textToSpeech != null
                        &&
                textToSpeech.isSpeaking()
        ) {

            textToSpeech.stop();
        }

        speaking = false;
    }

    public void setSpeechRate(
            float rate
    ) {

        if (textToSpeech == null) {
            return;
        }

        if (rate < 0.1f) {
            rate = 0.1f;
        }

        if (rate > 3.0f) {
            rate = 3.0f;
        }

        textToSpeech.setSpeechRate(
                rate
        );
    }

    public void setPitch(
            float pitch
    ) {

        if (textToSpeech == null) {
            return;
        }

        if (pitch < 0.1f) {
            pitch = 0.1f;
        }

        if (pitch > 3.0f) {
            pitch = 3.0f;
        }

        textToSpeech.setPitch(
                pitch
        );
    }

    private String cleanForSpeech(
            String text
    ) {

        return text
                .replaceAll(
                        "```[\\s\\S]*?```",
                        " "
                )
                .replaceAll(
                        "`([^`]*)`",
                        "$1"
                )
                .replaceAll(
                        "\\*\\*(.*?)\\*\\*",
                        "$1"
                )
                .replaceAll(
                        "\\*(.*?)\\*",
                        "$1"
                )
                .replaceAll(
                        "#{1,6}\\s*",
                        ""
                )
                .replaceAll(
                        "\\[([^\\]]+)\\]\\([^\\)]+\\)",
                        "$1"
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    public void destroy() {

        stop();

        if (textToSpeech != null) {

            textToSpeech.shutdown();

            textToSpeech = null;
        }

        ready = false;
    }
}
