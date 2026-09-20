package com.jarvis.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class JarvisPlanner {

    private final Context context;

    public JarvisPlanner(Context context) {
        this.context =
                context.getApplicationContext();
    }

    public JSONObject localPlan(
            String message
    ) {

        JSONObject plan =
                new JSONObject();

        JSONArray actions =
                new JSONArray();

        try {

            String text =
                    message == null
                            ? ""
                            : message.trim();

            plan.put(
                    "reply",
                    "I understood your request."
            );

            plan.put(
                    "actions",
                    actions
            );

            if (text.isEmpty()) {
                plan.put(
                        "reply",
                        "Tell me what you'd like me to do."
                );
                return plan;
            }

            String lower =
                    text.toLowerCase(
                            Locale.US
                    );

            /*
             * FLASHLIGHT
             */

            if (containsAny(
                    lower,
                    "flashlight on",
                    "torch on",
                    "turn on flashlight",
                    "turn flashlight on"
            )) {

                actions.put(
                        torchAction(true)
                );

                plan.put(
                        "reply",
                        "Turning the flashlight on."
                );

                return plan;
            }

            if (containsAny(
                    lower,
                    "flashlight off",
                    "torch off",
                    "turn off flashlight",
                    "turn flashlight off"
            )) {

                actions.put(
                        torchAction(false)
                );

                plan.put(
                        "reply",
                        "Turning the flashlight off."
                );

                return plan;
            }

            /*
             * UNDO
             */

            if (containsAny(
                    lower,
                    "undo",
                    "cancel last action",
                    "undo that"
            )) {

                plan.put(
                        "special",
                        "undo"
                );

                plan.put(
                        "reply",
                        "Cancelling the last Jarvis action."
                );

                return plan;
            }

            /*
             * SIMPLE TIMER
             */

            if (lower.contains("timer")) {

                int seconds =
                        extractTimerSeconds(lower);

                if (seconds > 0) {

                    JSONObject timer =
                            new JSONObject();

                    timer.put(
                            "type",
                            "timer"
                    );

                    timer.put(
                            "seconds",
                            seconds
                    );

                    timer.put(
                            "label",
                            "Jarvis timer"
                    );

                    actions.put(timer);

                    plan.put(
                            "reply",
                            "Setting the timer."
                    );

                    return plan;
                }
            }

            /*
             * OPEN URL
             */

            if (lower.startsWith("open http://")
                    || lower.startsWith("open https://")) {

                String url =
                        text.substring(
                                5
                        ).trim();

                JSONObject action =
                        new JSONObject();

                action.put(
                        "type",
                        "open_url"
                );

                action.put(
                        "url",
                        url
                );

                actions.put(action);

                plan.put(
                        "reply",
                        "Opening the website."
                );

                return plan;
            }

            /*
             * NORMAL CHAT
             *
             * Returning zero actions lets the
             * MainActivity send the request to
             * the cloud Brain instead of trying
             * to execute anything locally.
             */

            plan.put(
                    "route",
                    "cloud"
            );

        } catch (Exception e) {

            try {

                plan.put(
                        "error",
                        e.getMessage() == null
                                ? "Planner error"
                                : e.getMessage()
                );

            } catch (Exception ignored) {
            }
        }

        return plan;
    }

    private JSONObject torchAction(
            boolean enabled
    ) throws Exception {

        JSONObject action =
                new JSONObject();

        action.put(
                "type",
                "torch"
        );

        action.put(
                "enabled",
                enabled
        );

        action.put(
                "requires_confirmation",
                false
        );

        return action;
    }

    private int extractTimerSeconds(
            String text
    ) {

        String[] words =
                text.split("\\s+");

        for (int i = 0;
             i < words.length;
             i++) {

            try {

                int value =
                        Integer.parseInt(
                                words[i]
                                        .replaceAll(
                                                "[^0-9]",
                                                ""
                                        )
                        );

                if (value <= 0) {
                    continue;
                }

                if (i + 1 < words.length) {

                    String unit =
                            words[i + 1];

                    if (unit.startsWith("hour")
                            || unit.startsWith("hr")) {

                        return value * 60 * 60;
                    }

                    if (unit.startsWith("minute")
                            || unit.startsWith("min")) {

                        return value * 60;
                    }

                    if (unit.startsWith("second")
                            || unit.startsWith("sec")) {

                        return value;
                    }
                }

                /*
                 * "timer 30" means 30 seconds.
                 */

                if (text.contains("timer")) {
                    return value;
                }

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    private boolean containsAny(
            String text,
            String... values
    ) {

        for (String value : values) {

            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }

    public String getTodayDate() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                );

        format.setTimeZone(
                TimeZone.getDefault()
        );

        return format.format(
                new Date()
        );
    }

    public String getTomorrowDate() {

        Calendar calendar =
                Calendar.getInstance();

        calendar.add(
                Calendar.DAY_OF_YEAR,
                1
        );

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                );

        format.setTimeZone(
                TimeZone.getDefault()
        );

        return format.format(
                calendar.getTime()
        );
    }
}
