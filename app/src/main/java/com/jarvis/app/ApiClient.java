package com.jarvis.app;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    public interface Callback {

        void onSuccess(
                String reply,
                JSONArray actions
        );

        void onError(
                String message
        );
    }

    private static final String DEFAULT_URL =
            "https://shanu11.pythonanywhere.com";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(
                    Looper.getMainLooper()
            );

    private String baseUrl;

    public ApiClient() {
        this(DEFAULT_URL);
    }

    public ApiClient(
            String baseUrl
    ) {

        if (
                baseUrl == null
                        ||
                baseUrl.trim().isEmpty()
        ) {

            this.baseUrl =
                    DEFAULT_URL;

        } else {

            this.baseUrl =
                    baseUrl
                            .trim()
                            .replaceAll(
                                    "/+$",
                                    ""
                            );
        }
    }

    public void sendChat(
            String message,
            List<ChatMessage> conversation,
            String memories,
            Callback callback
    ) {

        sendChat(
                message,
                conversation,
                memories,
                null,
                null,
                callback
        );
    }

    public void sendChat(
            String message,
            List<ChatMessage> conversation,
            String memories,
            String timezone,
            String locale,
            Callback callback
    ) {

        if (
                message == null
                        ||
                message.trim().isEmpty()
        ) {

            deliverError(
                    callback,
                    "Message is empty."
            );

            return;
        }

        executor.execute(
                () -> {

                    HttpURLConnection connection =
                            null;

                    try {

                        URL url =
                                new URL(
                                        baseUrl
                                                +
                                                "/api/plan"
                                );

                        connection =
                                (HttpURLConnection)
                                        url.openConnection();

                        connection.setRequestMethod(
                                "POST"
                        );

                        connection.setConnectTimeout(
                                15000
                        );

                        connection.setReadTimeout(
                                60000
                        );

                        connection.setDoOutput(
                                true
                        );

                        connection.setRequestProperty(
                                "Content-Type",
                                "application/json; charset=UTF-8"
                        );

                        connection.setRequestProperty(
                                "Accept",
                                "application/json"
                        );

                        JSONObject body =
                                new JSONObject();

                        body.put(
                                "message",
                                message.trim()
                        );

                        body.put(
                                "text",
                                message.trim()
                        );

                        JSONArray conversationJson =
                                new JSONArray();

                        if (conversation != null) {

                            for (
                                    ChatMessage item
                                    : conversation
                            ) {

                                if (item == null) {
                                    continue;
                                }

                                if (item.isTyping()) {
                                    continue;
                                }

                                JSONObject itemJson =
                                        new JSONObject();

                                itemJson.put(
                                        "role",
                                        item.isUser()
                                                ? "user"
                                                : "assistant"
                                );

                                itemJson.put(
                                        "content",
                                        safe(
                                                item.getText()
                                        )
                                );

                                conversationJson.put(
                                        itemJson
                                );
                            }
                        }

                        body.put(
                                "conversation",
                                conversationJson
                        );

                        JSONArray memoryJson =
                                new JSONArray();

                        if (
                                memories != null
                                        &&
                                !memories.trim().isEmpty()
                        ) {

                            String[] lines =
                                    memories.split(
                                            "\\n"
                                    );

                            for (String line : lines) {

                                if (
                                        line != null
                                                &&
                                        !line.trim().isEmpty()
                                ) {

                                    memoryJson.put(
                                            line.trim()
                                    );
                                }
                            }
                        }

                        body.put(
                                "memories",
                                memoryJson
                        );

                        if (
                                timezone != null
                                        &&
                                !timezone.trim().isEmpty()
                        ) {

                            body.put(
                                    "timezone",
                                    timezone
                            );
                        }

                        if (
                                locale != null
                                        &&
                                !locale.trim().isEmpty()
                        ) {

                            body.put(
                                    "locale",
                                    locale
                            );
                        }

                        byte[] payload =
                                body.toString()
                                        .getBytes(
                                                StandardCharsets.UTF_8
                                        );

                        try (
                                OutputStream output =
                                        connection.getOutputStream()
                        ) {

                            output.write(
                                    payload
                            );

                            output.flush();
                        }

                        int responseCode =
                                connection.getResponseCode();

                        InputStream inputStream;

                        if (
                                responseCode >= 200
                                        &&
                                responseCode < 300
                        ) {

                            inputStream =
                                    connection.getInputStream();

                        } else {

                            inputStream =
                                    connection.getErrorStream();

                            if (inputStream == null) {

                                throw new Exception(
                                        "Server returned HTTP "
                                                +
                                                responseCode
                                );
                            }
                        }

                        String response =
                                readStream(
                                        inputStream
                                );

                        if (
                                responseCode < 200
                                        ||
                                responseCode >= 300
                        ) {

                            throw new Exception(
                                    extractError(
                                            response,
                                            responseCode
                                    )
                            );
                        }

                        JSONObject json =
                                new JSONObject(
                                        response
                                );

                        String reply =
                                json.optString(
                                        "reply",
                                        ""
                                );

                        JSONArray actions =
                                json.optJSONArray(
                                        "actions"
                                );

                        if (actions == null) {
                            actions =
                                    new JSONArray();
                        }

                        if (
                                reply.trim().isEmpty()
                        ) {

                            reply =
                                    ChatUtils.fallbackReply();
                        }

                        deliverSuccess(
                                callback,
                                reply,
                                actions
                        );

                    } catch (Exception error) {

                        String messageText =
                                error.getMessage();

                        if (
                                messageText == null
                                        ||
                                messageText.trim().isEmpty()
                        ) {

                            messageText =
                                    "Unable to contact JARVIS.";
                        }

                        deliverError(
                                callback,
                                messageText
                        );

                    } finally {

                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
        );
    }

    public void sendVisionChat(
            String message,
            String imageBase64,
            List<ChatMessage> conversation,
            String memories,
            Callback callback
    ) {

        if (
                message == null
                        ||
                message.trim().isEmpty()
        ) {

            message =
                    "Analyze this image.";
        }

        if (
                imageBase64 == null
                        ||
                imageBase64.trim().isEmpty()
        ) {

            sendChat(
                    message,
                    conversation,
                    memories,
                    callback
            );

            return;
        }

        final String finalMessage =
                message.trim();

        final String finalImage =
                imageBase64.trim();

        executor.execute(
                () -> {

                    HttpURLConnection connection =
                            null;

                    try {

                        URL url =
                                new URL(
                                        baseUrl
                                                +
                                                "/api/chat"
                                );

                        connection =
                                (HttpURLConnection)
                                        url.openConnection();

                        connection.setRequestMethod(
                                "POST"
                        );

                        connection.setConnectTimeout(
                                15000
                        );

                        connection.setReadTimeout(
                                90000
                        );

                        connection.setDoOutput(
                                true
                        );

                        connection.setRequestProperty(
                                "Content-Type",
                                "application/json; charset=UTF-8"
                        );

                        connection.setRequestProperty(
                                "Accept",
                                "application/json"
                        );

                        JSONObject body =
                                new JSONObject();

                        body.put(
                                "message",
                                finalMessage
                        );

                        body.put(
                                "image",
                                finalImage
                        );

                        JSONArray conversationJson =
                                new JSONArray();

                        if (conversation != null) {

                            for (
                                    ChatMessage item
                                    : conversation
                            ) {

                                if (
                                        item == null
                                                ||
                                        item.isTyping()
                                ) {
                                    continue;
                                }

                                JSONObject itemJson =
                                        new JSONObject();

                                itemJson.put(
                                        "role",
                                        item.isUser()
                                                ? "user"
                                                : "assistant"
                                );

                                itemJson.put(
                                        "content",
                                        safe(
                                                item.getText()
                                        )
                                );

                                conversationJson.put(
                                        itemJson
                                );
                            }
                        }

                        body.put(
                                "conversation",
                                conversationJson
                        );

                        byte[] payload =
                                body.toString()
                                        .getBytes(
                                                StandardCharsets.UTF_8
                                        );

                        try (
                                OutputStream output =
                                        connection.getOutputStream()
                        ) {

                            output.write(
                                    payload
                            );

                            output.flush();
                        }

                        int responseCode =
                                connection.getResponseCode();

                        InputStream inputStream =
                                responseCode >= 200
                                        &&
                                        responseCode < 300
                                        ?
                                        connection.getInputStream()
                                        :
                                        connection.getErrorStream();

                        if (inputStream == null) {

                            throw new Exception(
                                    "Vision server returned HTTP "
                                            +
                                            responseCode
                            );
                        }

                        String response =
                                readStream(
                                        inputStream
                                );

                        if (
                                responseCode < 200
                                        ||
                                responseCode >= 300
                        ) {

                            throw new Exception(
                                    extractError(
                                            response,
                                            responseCode
                                    )
                            );
                        }

                        JSONObject json =
                                new JSONObject(
                                        response
                                );

                        String reply =
                                json.optString(
                                        "reply",
                                        ""
                                );

                        if (
                                reply.trim().isEmpty()
                        ) {

                            reply =
                                    "I couldn't analyze that image.";
                        }

                        deliverSuccess(
                                callback,
                                reply,
                                new JSONArray()
                        );

                    } catch (Exception error) {

                        String messageText =
                                error.getMessage();

                        if (
                                messageText == null
                                        ||
                                messageText.trim().isEmpty()
                        ) {

                            messageText =
                                    "Vision request failed.";
                        }

                        deliverError(
                                callback,
                                messageText
                        );

                    } finally {

                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
        );
    }

    public void shutdown() {

        executor.shutdownNow();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(
            String baseUrl
    ) {

        if (
                baseUrl == null
                        ||
                baseUrl.trim().isEmpty()
        ) {
            return;
        }

        this.baseUrl =
                baseUrl
                        .trim()
                        .replaceAll(
                                "/+$",
                                ""
                        );
    }

    private void deliverSuccess(
            Callback callback,
            String reply,
            JSONArray actions
    ) {

        if (callback == null) {
            return;
        }

        mainHandler.post(
                () -> callback.onSuccess(
                        reply,
                        actions
                )
        );
    }

    private void deliverError(
            Callback callback,
            String message
    ) {

        if (callback == null) {
            return;
        }

        mainHandler.post(
                () -> callback.onError(
                        message
                )
        );
    }

    private String readStream(
            InputStream input
    ) throws Exception {

        StringBuilder result =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        input,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                result.append(
                        line
                );
            }
        }

        return result.toString();
    }

    private String extractError(
            String response,
            int code
    ) {

        try {

            JSONObject json =
                    new JSONObject(
                            response
                    );

            String error =
                    json.optString(
                            "error",
                            ""
                    );

            if (
                    !error.trim().isEmpty()
            ) {
                return error;
            }

            String detail =
                    json.optString(
                            "detail",
                            ""
                    );

            if (
                    !detail.trim().isEmpty()
            ) {
                return detail;
            }

        } catch (Exception ignored) {
        }

        if (response != null
                &&
                !response.trim().isEmpty()) {

            return "Server error: "
                    +
                    response;
        }

        return "Server returned HTTP "
                +
                code;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}
