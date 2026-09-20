package com.jarvis.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ChatUtils {

    private ChatUtils() {
        // Utility class.
    }

    public static String currentTime() {

        return new SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
        ).format(
                new Date()
        );
    }

    public static String formatTime(
            long timestamp
    ) {

        if (timestamp <= 0) {
            timestamp =
                    System.currentTimeMillis();
        }

        return new SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
        ).format(
                new Date(timestamp)
        );
    }

    public static String getGreeting() {

        int hour =
                java.util.Calendar
                        .getInstance()
                        .get(
                                java.util.Calendar.HOUR_OF_DAY
                        );

        if (hour >= 5 && hour < 12) {
            return "Good morning";
        }

        if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        }

        if (hour >= 17 && hour < 22) {
            return "Good evening";
        }

        return "Good night";
    }

    public static String cleanText(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
    }

    public static boolean isEmpty(
            String text
    ) {

        return text == null
                ||
                text.trim().isEmpty();
    }

    public static String limitText(
            String text,
            int maxLength
    ) {

        text = cleanText(text);

        if (maxLength <= 0) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        if (maxLength <= 3) {
            return text.substring(
                    0,
                    maxLength
            );
        }

        return text.substring(
                0,
                maxLength - 3
        ) + "...";
    }

    public static String fallbackReply() {

        return "I'm ready. What would you like me to do?";
    }
}
