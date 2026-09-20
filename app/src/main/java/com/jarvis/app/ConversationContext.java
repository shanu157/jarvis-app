package com.jarvis.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConversationContext {

    private static final String PREFS =
            "jarvis_conversation";

    private static final String KEY =
            "messages";

    private static final int MAX_MESSAGES = 20;

    private final SharedPreferences prefs;

    public ConversationContext(Context context) {

        prefs = context
                .getApplicationContext()
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    public synchronized void add(
            String role,
            String message
    ) {

        if (message == null ||
                message.trim().isEmpty()) {
            return;
        }

        try {

            JSONArray array = load();

            JSONObject item = new JSONObject();

            item.put(
                    "role",
                    role == null ? "user" : role
            );

            item.put(
                    "message",
                    message.trim()
            );

            item.put(
                    "time",
                    System.currentTimeMillis()
            );

            array.put(item);

            while (array.length() > MAX_MESSAGES) {
                array.remove(0);
            }

            prefs.edit()
                    .putString(
                            KEY,
                            array.toString()
                    )
                    .apply();

        } catch (Exception ignored) {
        }
    }

    public synchronized JSONArray get() {
        return load();
    }

    public synchronized JSONArray getJson() {
        return load();
    }

    public synchronized void clear() {

        prefs.edit()
                .remove(KEY)
                .apply();
    }

    public synchronized int size() {
        return load().length();
    }

    public synchronized JSONObject last() {

        JSONArray array = load();

        if (array.length() == 0) {
            return null;
        }

        return array.optJSONObject(
                array.length() - 1
        );
    }

    public synchronized JSONObject lastUserMessage() {

        JSONArray array = load();

        for (int i = array.length() - 1;
             i >= 0;
             i--) {

            JSONObject item =
                    array.optJSONObject(i);

            if (item == null) {
                continue;
            }

            if ("user".equals(
                    item.optString("role")
            )) {
                return item;
            }
        }

        return null;
    }

    public synchronized String lastUserText() {

        JSONObject item =
                lastUserMessage();

        if (item == null) {
            return "";
        }

        return item.optString(
                "message",
                ""
        );
    }

    private JSONArray load() {

        try {

            String saved =
                    prefs.getString(
                            KEY,
                            "[]"
                    );

            if (saved == null ||
                    saved.trim().isEmpty()) {

                return new JSONArray();
            }

            return new JSONArray(saved);

        } catch (Exception e) {

            return new JSONArray();
        }
    }
}
