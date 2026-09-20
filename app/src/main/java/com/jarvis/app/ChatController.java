package com.jarvis.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.List;

public class ChatController {

    public interface Listener {
        void onUserMessage(String message);

        void onRegenerate(String message);
    }

    private final Context context;
    private final ScrollView scrollView;
    private final ChatAdapter adapter;
    private final ChatStorage storage;
    private final Listener listener;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public ChatController(
            Context context,
            ScrollView scrollView,
            ChatAdapter adapter,
            ChatStorage storage,
            Listener listener
    ) {
        this.context = context;
        this.scrollView = scrollView;
        this.adapter = adapter;
        this.storage = storage;
        this.listener = listener;

        restore();
    }

    public void send(String message) {

        if (message == null) {
            return;
        }

        message = message.trim();

        if (message.isEmpty()) {
            return;
        }

        adapter.addUserMessage(message);

        persist();

        scrollToBottom();

        if (listener != null) {
            listener.onUserMessage(message);
        }
    }

    public void addAiResponse(
            String response
    ) {

        if (response == null) {
            response = "";
        }

        adapter.replaceTypingWithAi(
                response
        );

        persist();

        scrollToBottom();
    }

    public void addAiResponse(
            String response,
            String actionStatus
    ) {

        adapter.removeTyping();

        adapter.addAiMessage(
                response,
                actionStatus
        );

        persist();

        scrollToBottom();
    }

    public void showThinking() {
        adapter.showTyping();

        scrollToBottom();
    }

    public void hideThinking() {
        adapter.removeTyping();

        scrollToBottom();
    }

    public void clearChat() {

        adapter.clear();

        storage.clear();

        Toast.makeText(
                context,
                "Chat cleared",
                Toast.LENGTH_SHORT
        ).show();
    }

    public void restore() {

        List<ChatMessage> saved =
                storage.load();

        for (ChatMessage message : saved) {
            adapter.addMessageWithoutRender(
                    message
            );
        }

        adapter.renderMessages();

        scrollToBottom();
    }

    public void persist() {
        storage.save(
                adapter.getMessages()
        );
    }

    public ChatMessage getLastUserMessage() {
        return adapter.getLastUserMessage();
    }

    private void scrollToBottom() {

        mainHandler.postDelayed(
                () -> {

                    scrollView.fullScroll(
                            View.FOCUS_DOWN
                    );

                },
                120
        );
    }
}
