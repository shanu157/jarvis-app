package com.jarvis.app;

public interface VoiceInputCallback {

    void onVoiceResult(
            String text
    );

    void onVoiceError(
            String message
    );

    default void onVoiceStarted() {
        // Optional.
    }

    default void onVoiceStopped() {
        // Optional.
    }
}
