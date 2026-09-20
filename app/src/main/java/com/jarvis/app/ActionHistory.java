package com.jarvis.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActionHistory {

    private static final String PREFS =
            "jarvis_action_history";

    private static final String KEY =
            "actions";

    private static final int MAX_ACTIONS = 50;

    private final SharedPreferences prefs;

    public ActionHistory(Context context) {

        prefs = context
                .getApplicationContext()
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    public synchronized void add(
            String type,
            String label,
            String id,
            long time
    ) {

        try {

            JSONArray array = load();

            JSONObject item = new JSONObject();

            item.put(
                    "type",
                    type == null ? "unknown" : type
            );

            item.put(
                    "label",
                    label == null ? "" : label
            );

            item.put(
                    "id",
                    id == null ? "" : id
            );

            item.put(
                    "time",
                    time
            );

            array.put(item);

            while (array.length() > MAX_ACTIONS) {
                array.remove(0);
            }

            save(array);

        } catch (Exception ignored) {
        }
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

    public synchronized void removeLast() {

        JSONArray array = load();

        if (array.length() == 0) {
            return;
        }

        array.remove(
                array.length() - 1
        );

        save(array);
    }

    public synchronized JSONArray getAll() {
        return load();
    }

    public synchronized String getJson() {
        return load().toString();
    }

    public synchronized int size() {
        return load().length();
    }

    public synchronized void clear() {
        prefs.edit()
                .remove(KEY)
                .apply();
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

    private void save(JSONArray array) {

        prefs.edit()
                .putString(
                        KEY,
                        array.toString()
                )
                .apply();
    }
}
