package com.jarvis.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class MemoryManager {

    private static final String PREFS =
            "jarvis_memory";

    private static final String KEY =
            "memories";

    private static final int MAX_MEMORIES = 100;

    private final SharedPreferences prefs;

    public MemoryManager(Context context) {

        prefs = context
                .getApplicationContext()
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    public synchronized void remember(
            String text
    ) {

        if (text == null ||
                text.trim().isEmpty()) {
            return;
        }

        try {

            JSONArray memories = load();

            String value = text.trim();

            /*
             * Do not store the exact same memory twice.
             */
            for (int i = 0;
                 i < memories.length();
                 i++) {

                JSONObject item =
                        memories.optJSONObject(i);

                if (item == null) {
                    continue;
                }

                if (value.equalsIgnoreCase(
                        item.optString("text", "")
                )) {
                    return;
                }
            }

            JSONObject item =
                    new JSONObject();

            item.put(
                    "text",
                    value
            );

            item.put(
                    "time",
                    System.currentTimeMillis()
            );

            memories.put(item);

            while (memories.length()
                    > MAX_MEMORIES) {

                memories.remove(0);
            }

            save(memories);

        } catch (Exception ignored) {
        }
    }

    public synchronized JSONArray getAll() {
        return load();
    }

    public synchronized String getAllText() {

        JSONArray memories = load();

        if (memories.length() == 0) {
            return "I don't have any saved memories yet.";
        }

        StringBuilder result =
                new StringBuilder();

        result.append(
                "Here is what I remember:\n"
        );

        for (int i = 0;
             i < memories.length();
             i++) {

            JSONObject item =
                    memories.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String text =
                    item.optString(
                            "text",
                            ""
                    );

            if (!text.isEmpty()) {

                result.append(
                        "• "
                );

                result.append(text);

                result.append("\n");
            }
        }

        return result.toString().trim();
    }

    public synchronized boolean forget(
            String query
    ) {

        if (query == null ||
                query.trim().isEmpty()) {

            return false;
        }

        String target =
                query.trim();

        JSONArray memories = load();

        boolean removed = false;

        for (int i = memories.length() - 1;
             i >= 0;
             i--) {

            JSONObject item =
                    memories.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String text =
                    item.optString(
                            "text",
                            ""
                    );

            /*
             * Exact match or partial match.
             */
            if (text.equalsIgnoreCase(target)
                    || text.toLowerCase()
                    .contains(
                            target.toLowerCase()
                    )) {

                memories.remove(i);
                removed = true;
            }
        }

        if (removed) {
            save(memories);
        }

        return removed;
    }

    public synchronized void clear() {
        prefs.edit()
                .remove(KEY)
                .apply();
    }

    public synchronized int size() {
        return load().length();
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

    private void save(
            JSONArray memories
    ) {

        prefs.edit()
                .putString(
                        KEY,
                        memories.toString()
                )
                .apply();
    }
}
