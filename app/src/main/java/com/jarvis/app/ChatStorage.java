package com.jarvis.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatStorage {

    private static final String PREFS =
            "jarvis_chat_storage";

    private static final String KEY_MESSAGES =
            "messages";

    private static final int MAX_MESSAGES = 100;

    private final SharedPreferences prefs;

    public ChatStorage(Context context) {
        prefs = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );
    }

    public void save(
            List<ChatMessage> messages
    ) {
        if (messages == null) {
            return;
        }

        try {
            JSONArray array = new JSONArray();

            int start =
                    Math.max(
                            0,
                            messages.size()
                                    - MAX_MESSAGES
                    );

            for (int i = start;
                    i < messages.size();
                    i++) {

                ChatMessage message =
                        messages.get(i);

                JSONObject object =
                        new JSONObject();

                object.put(
                        "text",
                        message.getText()
                );

                object.put(
                        "type",
                        message.getType()
                );

                object.put(
                        "timestamp",
                        message.getTimestamp()
                );

                object.put(
                        "actionStatus",
                        message.getActionStatus()
                );

                array.put(object);
            }

            prefs.edit()
                    .putString(
                            KEY_MESSAGES,
                            array.toString()
                    )
                    .apply();

        } catch (Exception ignored) {
        }
    }

    public List<ChatMessage> load() {

        List<ChatMessage> result =
                new ArrayList<>();

        String raw =
                prefs.getString(
                        KEY_MESSAGES,
                        ""
                );

        if (raw == null
                || raw.trim().isEmpty()) {
            return result;
        }

        try {
            JSONArray array =
                    new JSONArray(raw);

            for (int i = 0;
                    i < array.length();
                    i++) {

                JSONObject object =
                        array.getJSONObject(i);

                String text =
                        object.optString(
                                "text",
                                ""
                        );

                int type =
                        object.optInt(
                                "type",
                                ChatMessage.TYPE_AI
                        );

                long timestamp =
                        object.optLong(
                                "timestamp",
                                System.currentTimeMillis()
                        );

                String actionStatus =
                        object.optString(
                                "actionStatus",
                                ""
                        );

                /*
                 * Typing indicators should never survive
                 * an app restart.
                 */
                if (type
                        == ChatMessage.TYPE_TYPING) {
                    continue;
                }

                result.add(
                        new ChatMessage(
                                text,
                                type,
                                timestamp,
                                actionStatus
                        )
                );
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    public void clear() {
        prefs.edit()
                .remove(KEY_MESSAGES)
                .apply();
    }

    public int size() {
        return load().size();
    }

    public boolean hasHistory() {
        return size() > 0;
    }

    public void add(
            ChatMessage message
    ) {
        if (message == null) {
            return;
        }

        List<ChatMessage> messages =
                load();

        messages.add(message);

        save(messages);
    }

    public void removeLast() {

        List<ChatMessage> messages =
                load();

        if (messages.isEmpty()) {
            return;
        }

        messages.remove(
                messages.size() - 1
        );

        save(messages);
    }
}
