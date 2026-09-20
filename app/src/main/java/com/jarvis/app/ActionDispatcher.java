package com.jarvis.app;

import android.content.Context;

import org.json.JSONObject;

import java.util.List;

public class ActionDispatcher {

    public interface Callback {
        void onActionStarted(ActionParser.ParsedAction action);

        void onConfirmationRequired(
                ActionParser.ParsedAction action,
                Runnable onConfirm,
                Runnable onCancel
        );

        void onActionSuccess(
                ActionParser.ParsedAction action,
                String message
        );

        void onActionError(
                ActionParser.ParsedAction action,
                String message
        );

        void onUndoSuccess(String message);

        void onUndoError(String message);
    }

    private final Context context;
    private final ActionExecutor executor;
    private final ActionHistory history;
    private final Callback callback;

    public ActionDispatcher(
            Context context,
            ActionExecutor executor,
            ActionHistory history,
            Callback callback
    ) {
        this.context = context.getApplicationContext();
        this.executor = executor;
        this.history = history;
        this.callback = callback;
    }

    public void dispatchAll(
            List<ActionParser.ParsedAction> actions
    ) {

        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (ActionParser.ParsedAction action : actions) {

            if (action == null) {
                continue;
            }

            dispatch(action);
        }
    }

    public void dispatch(
            ActionParser.ParsedAction action
    ) {

        if (action == null) {
            return;
        }

        if ("undo".equalsIgnoreCase(action.getType())) {
            undoLast();
            return;
        }

        if (callback != null) {
            callback.onActionStarted(action);
        }

        if (action.requiresConfirmation()) {

            if (callback != null) {

                callback.onConfirmationRequired(
                        action,
                        () -> execute(action),
                        () -> {

                            if (callback != null) {
                                callback.onActionError(
                                        action,
                                        "Action cancelled."
                                );
                            }
                        }
                );
            }

            return;
        }

        execute(action);
    }

    private void execute(
            ActionParser.ParsedAction action
    ) {

        try {

            JSONObject json =
                    action.toJson();

            String result =
                    executor.executeAction(
                            json
                    );

            if (result == null ||
                    result.trim().isEmpty()) {

                result =
                        successMessage(action);
            }

            if (callback != null) {

                callback.onActionSuccess(
                        action,
                        result
                );
            }

        } catch (Exception e) {

            String message =
                    e.getMessage();

            if (message == null ||
                    message.trim().isEmpty()) {

                message =
                        "Unable to execute this action.";
            }

            if (callback != null) {

                callback.onActionError(
                        action,
                        message
                );
            }
        }
    }

    public void undoLast() {

        try {

            boolean success =
                    executor.undoLast();

            if (success) {

                if (history != null) {
                    history.removeLast();
                }

                if (callback != null) {
                    callback.onUndoSuccess(
                            "Last action undone."
                    );
                }

            } else {

                if (callback != null) {
                    callback.onUndoError(
                            "There is no recent action to undo."
                    );
                }
            }

        } catch (Exception e) {

            String message =
                    e.getMessage();

            if (message == null ||
                    message.trim().isEmpty()) {

                message =
                        "Unable to undo the last action.";
            }

            if (callback != null) {
                callback.onUndoError(message);
            }
        }
    }

    private String successMessage(
            ActionParser.ParsedAction action
    ) {

        if (action == null ||
                action.getType() == null) {

            return "Action completed.";
        }

        String type =
                action.getType().toLowerCase();

        switch (type) {

            case "alarm":
                return "Alarm scheduled.";

            case "reminder":
                return "Reminder scheduled.";

            case "exam_reminder":
                return "Exam reminder scheduled.";

            case "timer":
                return "Timer started.";

            case "torch":
                return "Flashlight updated.";

            case "call":
                return "Call action completed.";

            case "sms":
                return "Message action completed.";

            case "open_url":
                return "Opening the requested page.";

            case "open_app":
                return "Opening the requested app.";

            default:
                return "Action completed.";
        }
    }
}
