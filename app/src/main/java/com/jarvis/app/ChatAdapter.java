package com.jarvis.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter {

    public interface Callbacks {
        void onRegenerate(ChatMessage message);
    }

    private final Context context;
    private final ViewGroup container;
    private final Callbacks callbacks;

    private final List<ChatMessage> messages =
            new ArrayList<>();

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat(
                    "h:mm a",
                    Locale.getDefault()
            );

    public ChatAdapter(
            Context context,
            ViewGroup container,
            Callbacks callbacks
    ) {
        this.context = context;
        this.container = container;
        this.callbacks = callbacks;
    }

    public void addMessage(ChatMessage message) {
        if (message == null) {
            return;
        }

        messages.add(message);
        render();
    }

    public void addMessageWithoutRender(ChatMessage message) {
        if (message == null) return;
        messages.add(message);
    }

    public void addUserMessage(String text) {
        addMessage(
                new ChatMessage(
                        text,
                        ChatMessage.TYPE_USER,
                        System.currentTimeMillis()
                )
        );
    }

    public void addAiMessage(String text) {
        addMessage(
                new ChatMessage(
                        text,
                        ChatMessage.TYPE_AI,
                        System.currentTimeMillis()
                )
        );
    }

    public void addAiMessage(
            String text,
            String actionStatus
    ) {
        addMessage(
                new ChatMessage(
                        text,
                        ChatMessage.TYPE_AI,
                        System.currentTimeMillis(),
                        actionStatus
                )
        );
    }

    public void showTyping() {
        removeTyping();

        messages.add(
                new ChatMessage(
                        "",
                        ChatMessage.TYPE_TYPING,
                        System.currentTimeMillis()
                )
        );

        render();
    }

    public void removeTyping() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).isTyping()) {
                messages.remove(i);
            }
        }
    }

    public void replaceTypingWithAi(
            String text
    ) {
        removeTyping();

        messages.add(
                new ChatMessage(
                        text,
                        ChatMessage.TYPE_AI,
                        System.currentTimeMillis()
                )
        );

        render();
    }

    public void renderMessages() {
        container.removeAllViews();

        for (ChatMessage message : messages) {
            if (message == null) continue;

            if (message.isUser()) {
                renderUser(message);
            } else if (message.isAi()) {
                renderAi(message);
            } else if (message.isTyping()) {
                renderTyping(message);
            } else if (message.isSystem()) {
                renderSystem(message);
            }
        }
    }

    public void clear() {
        messages.clear();
        container.removeAllViews();
    }

    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    public ChatMessage getLastUserMessage() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);

            if (message.isUser()) {
                return message;
            }
        }

        return null;
    }

    private void render() {
        container.removeAllViews();

        LayoutInflater inflater =
                LayoutInflater.from(context);

        for (ChatMessage message : messages) {

            if (message.isUser()) {
                renderUser(inflater, message);

            } else if (message.isAi()) {
                renderAi(inflater, message);

            } else if (message.isTyping()) {
                renderTyping(inflater);

            } else {
                renderSystem(inflater, message);
            }
        }
    }

    private void renderUser(
            LayoutInflater inflater,
            ChatMessage message
    ) {
        View view = inflater.inflate(
                R.layout.item_message_user,
                container,
                false
        );

        TextView text =
                view.findViewById(
                        R.id.userMessageText
                );

        TextView time =
                view.findViewById(
                        R.id.userTimeText
                );

        View bubble =
                view.findViewById(
                        R.id.userBubble
                );

        text.setText(message.getText());

        time.setText(
                timeFormat.format(
                        new Date(message.getTimestamp())
                )
        );

        bubble.setBackgroundResource(
                R.drawable.bg_user_message
        );

        container.addView(view);
    }

    private void renderAi(
            LayoutInflater inflater,
            ChatMessage message
    ) {
        View view = inflater.inflate(
                R.layout.item_message_ai,
                container,
                false
        );

        TextView text =
                view.findViewById(
                        R.id.aiMessageText
                );

        TextView time =
                view.findViewById(
                        R.id.aiTimeText
                );

        TextView action =
                view.findViewById(
                        R.id.aiActionStatus
                );

        TextView copy =
                view.findViewById(
                        R.id.copyButton
                );

        TextView regenerate =
                view.findViewById(
                        R.id.regenerateButton
                );

        text.setText(message.getText());

        time.setText(
                timeFormat.format(
                        new Date(message.getTimestamp())
                )
        );

        String actionStatus =
                message.getActionStatus();

        if (actionStatus != null
                && !actionStatus.trim().isEmpty()) {

            action.setText(actionStatus);
            action.setVisibility(View.VISIBLE);

        } else {
            action.setVisibility(View.GONE);
        }

        copy.setOnClickListener(
                v -> copyToClipboard(
                        message.getText()
                )
        );

        regenerate.setOnClickListener(
                v -> {
                    if (callbacks != null) {
                        callbacks.onRegenerate(message);
                    }
                }
        );

        container.addView(view);
    }

    private void renderTyping(
            LayoutInflater inflater
    ) {
        View view = inflater.inflate(
                R.layout.item_typing,
                container,
                false
        );

        TextView typing =
                view.findViewById(
                        R.id.typingText
                );

        animateTyping(typing);

        container.addView(view);
    }

    private void renderSystem(
            LayoutInflater inflater,
            ChatMessage message
    ) {
        TextView view = new TextView(context);

        view.setText(message.getText());

        view.setTextColor(
                0xFF647286
        );

        view.setTextSize(12);

        view.setPadding(
                16,
                12,
                16,
                12
        );

        container.addView(view);
    }

    private void animateTyping(
            TextView textView
    ) {
        final String[] states = {
                "Thinking  •",
                "Thinking  •  •",
                "Thinking  •  •  •"
        };

        final int[] index = {0};

        textView.post(
                new Runnable() {
                    @Override
                    public void run() {

                        if (textView.getWindowToken()
                                == null) {
                            return;
                        }

                        textView.setText(
                                states[index[0]]
                        );

                        index[0] =
                                (index[0] + 1)
                                        % states.length;

                        textView.postDelayed(
                                this,
                                450
                        );
                    }
                }
        );
    }

    private void copyToClipboard(
            String value
    ) {
        ClipboardManager clipboard =
                (ClipboardManager)
                        context.getSystemService(
                                Context.CLIPBOARD_SERVICE
                        );

        if (clipboard == null) {
            return;
        }

        clipboard.setPrimaryClip(
                ClipData.newPlainText(
                        "JARVIS response",
                        value
                )
        );
    }
}
