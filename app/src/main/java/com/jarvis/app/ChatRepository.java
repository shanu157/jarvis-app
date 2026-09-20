package com.jarvis.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private final Context context;
    private final ChatStorage chatStorage;
    private final ConversationContext conversationContext;
    private final MemoryManager memoryManager;

    public ChatRepository(Context context) {

        this.context =
                context.getApplicationContext();

        this.chatStorage =
                new ChatStorage(this.context);

        this.conversationContext =
                new ConversationContext(this.context);

        this.memoryManager =
                new MemoryManager(this.context);
    }

    public ChatStorage getChatStorage() {
        return chatStorage;
    }

    public ConversationContext getConversationContext() {
        return conversationContext;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public void addUserMessage(
            String text
    ) {

        if (text == null ||
                text.trim().isEmpty()) {
            return;
        }

        String clean =
                text.trim();

        conversationContext.add(
                "user",
                clean
        );
    }

    public void addAssistantMessage(
            String text
    ) {

        if (text == null ||
                text.trim().isEmpty()) {
            return;
        }

        String clean =
                text.trim();

        conversationContext.add(
                "assistant",
                clean
        );
    }

    public List<ChatMessage> getConversationList() {
        List<ChatMessage> result = new java.util.ArrayList<>();
        JSONArray array = getConversationJson();

        for (int i = 0; i < array.length(); i++) {
            try {
                org.json.JSONObject item = array.optJSONObject(i);
                if (item == null) continue;

                String role = item.optString("role", "assistant");
                String content = item.optString("content", "");

                if (content.trim().isEmpty()) continue;

                int type =
                        "user".equalsIgnoreCase(role)
                                ? ChatMessage.TYPE_USER
                                : ChatMessage.TYPE_AI;

                result.add(
                        new ChatMessage(
                                content,
                                type,
                                System.currentTimeMillis()
                        )
                );
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public JSONArray getConversationJson() {

        try {

            return conversationContext.getJson();

        } catch (Exception e) {

            return new JSONArray();
        }
    }

    public JSONArray getMemoryJson() {

        JSONArray result =
                new JSONArray();

        try {

            List<String> memories =
                    memoryManager.getAll();

            if (memories == null) {
                return result;
            }

            for (String memory : memories) {

                if (memory == null ||
                        memory.trim().isEmpty()) {
                    continue;
                }

                result.put(
                        memory.trim()
                );
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    public String getMemoryText() {

        try {

            String text =
                    memoryManager.getAllText();

            if (text == null) {
                return "";
            }

            return text;

        } catch (Exception e) {

            return "";
        }
    }

    public void remember(
            String memory
    ) {

        if (memory == null ||
                memory.trim().isEmpty()) {
            return;
        }

        memoryManager.remember(
                memory.trim()
        );
    }

    public boolean forget(
            String memory
    ) {

        if (memory == null ||
                memory.trim().isEmpty()) {

            return false;
        }

        return memoryManager.forget(
                memory.trim()
        );
    }

    public void clearMemory() {
        memoryManager.clear();
    }

    public void clearChat() {

        chatStorage.clear();

        conversationContext.clear();
    }

    public int chatSize() {
        return chatStorage.size();
    }

    public int memorySize() {
        return memoryManager.size();
    }

    public String getLastUserMessage() {

        String last =
                conversationContext.lastUserText();

        if (last == null) {
            return "";
        }

        return last;
    }

    public JSONObject buildPlannerContext(
            String currentMessage,
            String timezone,
            String locale
    ) {

        JSONObject root =
                new JSONObject();

        try {

            root.put(
                    "message",
                    currentMessage == null
                            ? ""
                            : currentMessage
            );

            root.put(
                    "conversation",
                    getConversationJson()
            );

            root.put(
                    "memories",
                    getMemoryJson()
            );

            root.put(
                    "timezone",
                    timezone == null
                            ? ""
                            : timezone
            );

            root.put(
                    "locale",
                    locale == null
                            ? ""
                            : locale
            );

        } catch (Exception ignored) {
        }

        return root;
    }

    public List<String> getMemoryList() {

        List<String> result =
                new ArrayList<>();

        try {

            List<String> memories =
                    memoryManager.getAll();

            if (memories != null) {
                result.addAll(memories);
            }

        } catch (Exception ignored) {
        }

        return result;
    }
}
