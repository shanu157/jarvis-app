package com.jarvis.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity
        implements VoiceInputCallback,
        AttachmentManager.Callback,
        ActionDispatcher.Callback {

    private EditText messageInput;
    private Button sendButton;
    private Button clearInputButton;
    private Button voiceButton;
    private Button attachButton;
    private Button clearChatButton;

    private View quickSchedule;
    private View quickTimer;
    private View quickCalendar;
    private View quickVision;
    private View quickMemory;

    private ScrollView chatScroll;
    private TextView statusText;

    private ChatAdapter chatAdapter;
    private ChatController chatController;
    private ChatRepository repository;
    private SettingsStorage settings;

    private VoiceManager voiceManager;
    private TextToSpeechManager ttsManager;
    private ApiClient apiClient;
    private ActionExecutor actionExecutor;
    private ActionHistory actionHistory;
    private ActionDispatcher actionDispatcher;
    private AttachmentManager attachmentManager;
    private NotificationHelper notificationHelper;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean thinking = false;

    private Uri selectedAttachmentUri;
    private String selectedAttachmentMime;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );

        initialize();

        setupUi();

        restoreChat();

        checkFirstRun();
    }

    private void initialize() {

        repository =
                new ChatRepository(this);

        settings =
                new SettingsStorage(this);

        actionHistory =
                new ActionHistory(this);

        actionExecutor =
                new ActionExecutor(
                        this
                );

        notificationHelper =
                new NotificationHelper(
                        this
                );

        chatScroll =
                findViewById(
                        R.id.chatScroll
                );

        View chatContainer =
                findViewById(
                        R.id.chatContainer
                );

        chatAdapter =
                new ChatAdapter(
                        this,
                        (android.view.ViewGroup)
                                chatContainer,
                        this::regenerateMessage
                );

        chatController =
                new ChatController(
                        this,
                        chatScroll,
                        chatAdapter,
                        repository.getChatStorage(),
                        null
                );

        apiClient =
                new ApiClient(
                        settings.getBrainUrl()
                );

        voiceManager =
                new VoiceManager(
                        this,
                        this
                );

        ttsManager =
                new TextToSpeechManager(
                        this,
                        new TextToSpeechManager.Callback() {
                            @Override
                            public void onReady() {
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onDone() {
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {
                            }
                        }
                );

        attachmentManager =
                new AttachmentManager(
                        this,
                        this
                );

        actionDispatcher =
                new ActionDispatcher(
                        this,
                        actionExecutor,
                        actionHistory,
                        this
                );
    }

    private void setupUi() {

        messageInput =
                findViewById(
                        R.id.messageInput
                );

        sendButton =
                findViewById(
                        R.id.sendButton
                );

        clearInputButton =
                findViewById(
                        R.id.clearInputButton
                );

        voiceButton =
                findViewById(
                        R.id.voiceButton
                );

        attachButton =
                findViewById(
                        R.id.attachButton
                );

        clearChatButton =
                findViewById(
                        R.id.clearChatButton
                );

        statusText =
                findViewById(
                        R.id.statusText
                );

        quickSchedule =
                findViewById(
                        R.id.quickSchedule
                );

        quickTimer =
                findViewById(
                        R.id.quickTimer
                );

        quickCalendar =
                findViewById(
                        R.id.quickCalendar
                );

        quickVision =
                findViewById(
                        R.id.quickVision
                );

        quickMemory =
                findViewById(
                        R.id.quickMemory
                );

        sendButton.setOnClickListener(
                v -> sendCurrentMessage()
        );

        clearInputButton.setOnClickListener(
                v -> clearInput()
        );

        clearChatButton.setOnClickListener(
                v -> clearChat()
        );

        voiceButton.setOnClickListener(
                v -> toggleVoice()
        );

        attachButton.setOnClickListener(
                v -> openAttachmentMenu()
        );

        setupQuickActions();

        messageInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (settings.isEnterToSendEnabled()) {

                        sendCurrentMessage();

                        return true;
                    }

                    return false;
                }
        );
    }

    private void setupQuickActions() {

        if (quickSchedule != null) {

            quickSchedule.setOnClickListener(
                    v -> setInput(
                            "Schedule something for me"
                    )
            );
        }

        if (quickTimer != null) {

            quickTimer.setOnClickListener(
                    v -> setInput(
                            "Set a timer for 10 minutes"
                    )
            );
        }

        if (quickCalendar != null) {

            quickCalendar.setOnClickListener(
                    v -> openAccessCenter()
            );
        }

        if (quickVision != null) {

            quickVision.setOnClickListener(
                    v -> openAttachmentMenu()
            );
        }

        if (quickMemory != null) {

            quickMemory.setOnClickListener(
                    v -> setInput(
                            "What do you remember about me?"
                    )
            );
        }
    }

    private void setInput(
            String text
    ) {

        messageInput.setText(
                text
        );

        messageInput.setSelection(
                messageInput.length()
        );

        messageInput.requestFocus();

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.showSoftInput(
                    messageInput,
                    InputMethodManager.SHOW_IMPLICIT
            );
        }
    }

    private void sendCurrentMessage() {

        String text =
                messageInput.getText()
                        .toString()
                        .trim();

        if (text.isEmpty() &&
                selectedAttachmentUri == null) {
            return;
        }

        if (thinking) {
            return;
        }

        messageInput.setText("");

        hideKeyboard();

        if (selectedAttachmentUri != null) {

            sendVisionMessage(
                    text
            );

            return;
        }

        sendTextMessage(
                text
        );
    }

    private void sendTextMessage(
            String text
    ) {

        chatAdapter.addUserMessage(
                text
        );

        repository.addUserMessage(
                text
        );

        persistChat();

        showThinking();

        setStatus(
                "JARVIS • THINKING"
        );

        String timezone =
                java.util.TimeZone
                        .getDefault()
                        .getID();

        String locale =
                Locale.getDefault()
                        .toLanguageTag();

        apiClient.sendChat(
                text,
                repository.getConversationJson(),
                repository.getMemoryJson(),
                timezone,
                locale,
                new ApiClient.Callback() {

                    @Override
                    public void onSuccess(
                            String reply,
                            JSONArray actions
                    ) {

                        runOnUiThread(
                                () -> {

                                    hideThinking();

                                    String answer =
                                            safeReply(
                                                    reply
                                            );

                                    chatAdapter.addAiMessage(
                                            answer
                                    );

                                    repository.addAssistantMessage(
                                            answer
                                    );

                                    persistChat();

                                    setStatus(
                                            "JARVIS • ONLINE"
                                    );

                                    if (settings
                                            .isAutoSpeakEnabled()) {

                                        speak(
                                                answer
                                        );
                                    }

                                    List<ActionParser.ParsedAction>
                                            parsed =
                                            ActionParser.parse(
                                                    actions
                                            );

                                    if (!parsed.isEmpty()) {

                                        actionDispatcher
                                                .dispatchAll(
                                                        parsed
                                                );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        runOnUiThread(
                                () -> {

                                    hideThinking();

                                    String error =
                                            "I couldn't reach the JARVIS brain.\n\n"
                                                    + safeReply(
                                                    message
                                            );

                                    chatAdapter.addAiMessage(
                                            error
                                    );

                                    repository
                                            .addAssistantMessage(
                                                    error
                                            );

                                    persistChat();

                                    setStatus(
                                            "JARVIS • OFFLINE"
                                    );
                                }
                        );
                    }
                }
        );
    }

    private void sendVisionMessage(
            String text
    ) {

        Uri imageUri =
                selectedAttachmentUri;

        String mime =
                selectedAttachmentMime;

        selectedAttachmentUri =
                null;

        selectedAttachmentMime =
                null;

        chatAdapter.addUserMessage(
                text.isEmpty()
                        ? "Analyze this image."
                        : text
        );

        repository.addUserMessage(
                text.isEmpty()
                        ? "Analyze this image."
                        : text
        );

        persistChat();

        showThinking();

        setStatus(
                "JARVIS • VISION"
        );

        new Thread(
                () -> {

                    try {

                        String base64 =
                                ImageUtils.uriToBase64(
                                        this,
                                        imageUri
                                );

                        apiClient.sendVisionChat(
                                text.isEmpty()
                                        ? "Analyze this image."
                                        : text,
                                base64,
                                repository.getConversationJson(),
                                new ApiClient.Callback() {

                                    @Override
                                    public void onSuccess(
                                            String reply,
                                            JSONArray actions
                                    ) {

                                        runOnUiThread(
                                                () -> {

                                                    hideThinking();

                                                    String answer =
                                                            safeReply(
                                                                    reply
                                                            );

                                                    chatAdapter
                                                            .addAiMessage(
                                                                    answer
                                                            );

                                                    repository
                                                            .addAssistantMessage(
                                                                    answer
                                                            );

                                                    persistChat();

                                                    setStatus(
                                                            "JARVIS • ONLINE"
                                                    );
                                                }
                                        );
                                    }

                                    @Override
                                    public void onError(
                                            String message
                                    ) {

                                        runOnUiThread(
                                                () -> {

                                                    hideThinking();

                                                    String error =
                                                            "Vision request failed.\n\n"
                                                                    + safeReply(
                                                                    message
                                                            );

                                                    chatAdapter
                                                            .addAiMessage(
                                                                    error
                                                            );

                                                    repository
                                                            .addAssistantMessage(
                                                                    error
                                                            );

                                                    persistChat();

                                                    setStatus(
                                                            "JARVIS • ONLINE"
                                                    );
                                                }
                                        );
                                    }
                                }
                        );

                    } catch (Exception e) {

                        runOnUiThread(
                                () -> {

                                    hideThinking();

                                    chatAdapter.addAiMessage(
                                            "I couldn't read that image.\n\n"
                                                    + safeReply(
                                                    e.getMessage()
                                            )
                                    );

                                    setStatus(
                                            "JARVIS • ONLINE"
                                    );
                                }
                        );
                    }

                }
        ).start();
    }

    private void showThinking() {

        thinking = true;

        sendButton.setEnabled(
                false
        );

        chatAdapter.showTyping();

        scrollToBottom();
    }

    private void hideThinking() {

        thinking = false;

        sendButton.setEnabled(
                true
        );

        chatAdapter.removeTyping();

        scrollToBottom();
    }

    private void clearInput() {

        messageInput.setText("");

        selectedAttachmentUri =
                null;

        selectedAttachmentMime =
                null;

        setStatus(
                "JARVIS • INPUT CLEARED"
        );
    }

    private void clearChat() {

        new android.app.AlertDialog.Builder(this)
                .setTitle(
                        "Clear chat?"
                )
                .setMessage(
                        "This removes the visible conversation history from JARVIS."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Clear",
                        (dialog, which) -> {

                            chatAdapter.clear();

                            repository.clearChat();

                            setStatus(
                                    "JARVIS • CHAT CLEARED"
                            );
                        }
                )
                .show();
    }

    private void toggleVoice() {

        if (voiceManager.isListening()) {

            voiceManager.stop();

            return;
        }

        setStatus(
                "JARVIS • LISTENING"
        );

        voiceManager.start();
    }

    private void speak(
            String text
    ) {

        if (!settings.isVoiceOutputEnabled()) {
            return;
        }

        if (ttsManager != null) {
            ttsManager.speak(
                    text
            );
        }
    }

    private void openAttachmentMenu() {

        new android.app.AlertDialog.Builder(this)
                .setTitle(
                        "Attach to JARVIS"
                )
                .setItems(
                        new String[]{
                                "Image / Vision",
                                "File"
                        },
                        (dialog, which) -> {

                            if (which == 0) {

                                attachmentManager
                                        .openImagePicker();

                            } else {

                                attachmentManager
                                        .openFilePicker();
                            }
                        }
                )
                .show();
    }

    private void openAccessCenter() {

        try {

            startActivity(
                    new Intent(
                            this,
                            AccessCenterActivity.class
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Access Center unavailable.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void restoreChat() {

        try {

            List<ChatMessage> saved =
                    repository.getChatStorage()
                            .load();

            if (saved != null) {

                for (ChatMessage message : saved) {

                    if (message != null &&
                            !message.isTyping()) {

                        chatAdapter
                                .addMessageWithoutRender(
                                        message
                                );
                    }
                }
            }

            chatAdapter.renderMessages();

            scrollToBottom();

        } catch (Exception ignored) {
        }
    }

    private void persistChat() {

        if (!settings.isChatSavingEnabled()) {
            return;
        }

        try {

            repository.getChatStorage()
                    .save(
                            chatAdapter.getMessages()
                    );

        } catch (Exception ignored) {
        }
    }

    private void regenerateMessage(
            ChatMessage message
    ) {

        ChatMessage last =
                repository.getChatStorage()
                        .getLastUserMessage();

        if (last == null) {

            ChatMessage local =
                    chatAdapter.getLastUserMessage();

            if (local != null) {
                sendTextMessage(
                        local.getText()
                );
            }

            return;
        }

        sendTextMessage(
                last.getText()
        );
    }

    private void checkFirstRun() {

        if (!settings.isFirstRun()) {
            return;
        }

        settings.setFirstRunComplete();

        handler.postDelayed(
                () -> {

                    chatAdapter.addAiMessage(
                            "Systems online. I'm JARVIS.\n\n"
                                    + "You can type, speak, attach an image, "
                                    + "set reminders, manage memory, or ask me anything."
                    );

                    persistChat();

                },
                400
        );
    }

    private void setStatus(
            String text
    ) {

        if (statusText != null) {

            statusText.setText(
                    text
            );
        }
    }

    private String safeReply(
            String text
    ) {

        if (text == null ||
                text.trim().isEmpty()) {

            return "I’m ready.";
        }

        return text.trim();
    }

    private void scrollToBottom() {

        if (chatScroll == null) {
            return;
        }

        chatScroll.post(
                () -> chatScroll.fullScroll(
                        View.FOCUS_DOWN
                )
        );
    }

    private void hideKeyboard() {

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    messageInput.getWindowToken(),
                    0
            );
        }
    }

    @Override
    public void onVoiceResult(
            String text
    ) {

        runOnUiThread(
                () -> {

                    setStatus(
                            "JARVIS • PROCESSING VOICE"
                    );

                    if (text == null ||
                            text.trim().isEmpty()) {

                        setStatus(
                                "JARVIS • ONLINE"
                        );

                        return;
                    }

                    setInput(
                            text.trim()
                    );

                    sendCurrentMessage();
                }
        );
    }

    @Override
    public void onVoiceError(
            String message
    ) {

        runOnUiThread(
                () -> setStatus(
                        "JARVIS • VOICE ERROR"
                )
        );
    }

    @Override
    public void onVoiceStarted() {

        runOnUiThread(
                () -> setStatus(
                        "JARVIS • LISTENING"
                )
        );
    }

    @Override
    public void onVoiceStopped() {

        runOnUiThread(
                () -> {

                    if (!thinking) {
                        setStatus(
                                "JARVIS • ONLINE"
                        );
                    }
                }
        );
    }

    @Override
    public void onFileSelected(
            Uri uri,
            String mimeType
    ) {

        selectedAttachmentUri =
                uri;

        selectedAttachmentMime =
                mimeType;

        String label =
                AttachmentManager.isImage(
                        mimeType
                )
                        ? "IMAGE ATTACHED"
                        : "FILE ATTACHED";

        setStatus(
                "JARVIS • " + label
        );

        Toast.makeText(
                this,
                label + " — add a message and press Send.",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onAttachmentCancelled() {

        selectedAttachmentUri =
                null;

        selectedAttachmentMime =
                null;

        setStatus(
                "JARVIS • ONLINE"
        );
    }

    @Override
    public void onActionStarted(
            ActionParser.ParsedAction action
    ) {

        if (action == null) {
            return;
        }

        setStatus(
                "JARVIS • EXECUTING"
        );
    }

    @Override
    public void onConfirmationRequired(
            ActionParser.ParsedAction action,
            Runnable onConfirm,
            Runnable onCancel
    ) {

        ActionConfirmationDialog.show(
                this,
                action,
                onConfirm,
                onCancel
        );
    }

    @Override
    public void onActionSuccess(
            ActionParser.ParsedAction action,
            String message
    ) {

        runOnUiThread(
                () -> {

                    String result =
                            safeReply(
                                    message
                            );

                    chatAdapter.addAiMessage(
                            result,
                            "ACTION • COMPLETED"
                    );

                    persistChat();

                    notificationHelper.showAction(
                            (int)
                                    (System.currentTimeMillis()
                                            % Integer.MAX_VALUE),
                            result
                    );

                    setStatus(
                            "JARVIS • ONLINE"
                    );
                }
        );
    }

    @Override
    public void onActionError(
            ActionParser.ParsedAction action,
            String message
    ) {

        runOnUiThread(
                () -> {

                    chatAdapter.addAiMessage(
                            safeReply(
                                    message
                            ),
                            "ACTION • NOT COMPLETED"
                    );

                    persistChat();

                    setStatus(
                            "JARVIS • ONLINE"
                    );
                }
        );
    }

    @Override
    public void onUndoSuccess(
            String message
    ) {

        runOnUiThread(
                () -> {

                    chatAdapter.addAiMessage(
                            message,
                            "ACTION • UNDONE"
                    );

                    persistChat();

                    setStatus(
                            "JARVIS • ONLINE"
                    );
                }
        );
    }

    @Override
    public void onUndoError(
            String message
    ) {

        runOnUiThread(
                () -> {

                    chatAdapter.addAiMessage(
                            message,
                            "ACTION • UNDO FAILED"
                    );

                    persistChat();

                    setStatus(
                            "JARVIS • ONLINE"
                    );
                }
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (attachmentManager != null &&
                attachmentManager.handleActivityResult(
                        requestCode,
                        resultCode,
                        data
                )) {

            return;
        }
    }

    @Override
    protected void onDestroy() {

        if (voiceManager != null) {
            voiceManager.destroy();
        }

        if (ttsManager != null) {
            ttsManager.destroy();
        }

        super.onDestroy();
    }
}
