package com.jarvis.app;

public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;
    public static final int TYPE_TYPING = 2;
    public static final int TYPE_SYSTEM = 3;

    private final String text;
    private final int type;
    private final long timestamp;
    private String actionStatus;

    public ChatMessage(
            String text,
            int type,
            long timestamp
    ) {
        this.text = text == null ? "" : text;
        this.type = type;
        this.timestamp = timestamp;
        this.actionStatus = "";
    }

    public ChatMessage(
            String text,
            int type,
            long timestamp,
            String actionStatus
    ) {
        this.text = text == null ? "" : text;
        this.type = type;
        this.timestamp = timestamp;
        this.actionStatus =
                actionStatus == null ? "" : actionStatus;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus =
                actionStatus == null ? "" : actionStatus;
    }

    public boolean isUser() {
        return type == TYPE_USER;
    }

    public boolean isAi() {
        return type == TYPE_AI;
    }

    public boolean isTyping() {
        return type == TYPE_TYPING;
    }

    public boolean isSystem() {
        return type == TYPE_SYSTEM;
    }
}
