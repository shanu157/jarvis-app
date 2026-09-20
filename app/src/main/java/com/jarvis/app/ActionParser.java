package com.jarvis.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ActionParser {

    private ActionParser() {
        // Utility class.
    }

    public static List<ParsedAction> parse(
            JSONArray actions
    ) {

        List<ParsedAction> result =
                new ArrayList<>();

        if (actions == null) {
            return result;
        }

        for (int i = 0; i < actions.length(); i++) {

            JSONObject object =
                    actions.optJSONObject(i);

            if (object == null) {
                continue;
            }

            ParsedAction action =
                    parseOne(object);

            if (action != null) {
                result.add(action);
            }
        }

        return result;
    }

    private static ParsedAction parseOne(
            JSONObject object
    ) {

        String type =
                object.optString(
                        "type",
                        ""
                ).trim().toLowerCase();

        if (type.isEmpty()) {
            return null;
        }

        String label =
                object.optString(
                        "label",
                        object.optString(
                                "title",
                                ""
                        )
                ).trim();

        String time =
                object.optString(
                        "time",
                        ""
                ).trim();

        String date =
                object.optString(
                        "date",
                        ""
                ).trim();

        String phone =
                object.optString(
                        "phone",
                        object.optString(
                                "number",
                                ""
                        )
                ).trim();

        String message =
                object.optString(
                        "message",
                        object.optString(
                                "text",
                                ""
                        )
                ).trim();

        String url =
                object.optString(
                        "url",
                        ""
                ).trim();

        String app =
                object.optString(
                        "app",
                        object.optString(
                                "package",
                                ""
                        )
                ).trim();

        long duration =
                object.optLong(
                        "duration_ms",
                        0L
                );

        boolean confirm =
                object.optBoolean(
                        "confirm",
                        false
                );

        return new ParsedAction(
                type,
                label,
                time,
                date,
                phone,
                message,
                url,
                app,
                duration,
                confirm
        );
    }

    public static boolean requiresConfirmation(
            ParsedAction action
    ) {

        if (action == null) {
            return false;
        }

        if (action.isConfirmationRequired()) {
            return true;
        }

        String type =
                action.getType();

        return "call".equals(type)
                ||
                "sms".equals(type)
                ||
                "forget_memory".equals(type)
                ||
                "external_send".equals(type);
    }

    public static final class ParsedAction {

        private final String type;
        private final String label;
        private final String time;
        private final String date;
        private final String phone;
        private final String message;
        private final String url;
        private final String app;
        private final long durationMs;
        private final boolean confirmationRequired;

        public ParsedAction(
                String type,
                String label,
                String time,
                String date,
                String phone,
                String message,
                String url,
                String app,
                long durationMs,
                boolean confirmationRequired
        ) {

            this.type = type;
            this.label = label;
            this.time = time;
            this.date = date;
            this.phone = phone;
            this.message = message;
            this.url = url;
            this.app = app;
            this.durationMs = durationMs;
            this.confirmationRequired =
                    confirmationRequired;
        }

        public String getType() {
            return type;
        }

        public String getLabel() {
            return label;
        }

        public String getTime() {
            return time;
        }

        public String getDate() {
            return date;
        }

        public String getPhone() {
            return phone;
        }

        public String getMessage() {
            return message;
        }

        public String getUrl() {
            return url;
        }

        public String getApp() {
            return app;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public boolean isConfirmationRequired() {
            return confirmationRequired;
        }

        public boolean requiresConfirmation() {
            return ActionParser.requiresConfirmation(this);
        }

        public org.json.JSONObject toJson() {
            org.json.JSONObject json = new org.json.JSONObject();

            try {
                json.put("type", type);
                json.put("label", label);
                json.put("time", time);
                json.put("date", date);
                json.put("phone", phone);
                json.put("message", message);
                json.put("url", url);
                json.put("app", app);
                json.put("duration_ms", durationMs);
                json.put("confirm", confirmationRequired);
            } catch (org.json.JSONException ignored) {
            }

            return json;
        }


        public boolean isAlarm() {
            return "alarm".equals(type);
        }

        public boolean isReminder() {
            return "reminder".equals(type);
        }

        public boolean isExamReminder() {
            return "exam_reminder".equals(type);
        }

        public boolean isTimer() {
            return "timer".equals(type);
        }

        public boolean isTorch() {
            return "torch".equals(type);
        }

        public boolean isCall() {
            return "call".equals(type);
        }

        public boolean isSms() {
            return "sms".equals(type);
        }

        public boolean isOpenUrl() {
            return "open_url".equals(type);
        }

        public boolean isOpenApp() {
            return "open_app".equals(type);
        }

        @Override
        public String toString() {

            return "ParsedAction{"
                    + "type='"
                    + type
                    + '\''
                    + ", label='"
                    + label
                    + '\''
                    + ", time='"
                    + time
                    + '\''
                    + ", date='"
                    + date
                    + '\''
                    + ", phone='"
                    + phone
                    + '\''
                    + ", message='"
                    + message
                    + '\''
                    + ", url='"
                    + url
                    + '\''
                    + ", app='"
                    + app
                    + '\''
                    + ", durationMs="
                    + durationMs
                    + ", confirmationRequired="
                    + confirmationRequired
                    + '}';
        }
    }
}
