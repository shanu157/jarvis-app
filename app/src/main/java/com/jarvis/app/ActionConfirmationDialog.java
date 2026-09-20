package com.jarvis.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class ActionConfirmationDialog {

    private ActionConfirmationDialog() {
    }

    public static void show(
            Context context,
            ActionParser.ParsedAction action,
            Runnable onConfirm,
            Runnable onCancel
    ) {

        if (context == null) {
            if (onCancel != null) {
                onCancel.run();
            }
            return;
        }

        String title =
                getTitle(action);

        String description =
                getDescription(action);

        LinearLayout layout =
                new LinearLayout(context);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                48,
                28,
                48,
                12
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(15, 24, 31)
        );

        background.setCornerRadius(
                28
        );

        layout.setBackground(
                background
        );

        TextView titleView =
                new TextView(context);

        titleView.setText(
                title
        );

        titleView.setTextColor(
                Color.WHITE
        );

        titleView.setTextSize(
                20
        );

        titleView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        layout.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        TextView descriptionView =
                new TextView(context);

        descriptionView.setText(
                description
        );

        descriptionView.setTextColor(
                Color.rgb(190, 205, 212)
        );

        descriptionView.setTextSize(
                15
        );

        descriptionView.setPadding(
                0,
                16,
                0,
                12
        );

        layout.addView(
                descriptionView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(context)
                        .setView(layout)
                        .setNegativeButton(
                                "Cancel",
                                (d, which) -> {
                                    if (onCancel != null) {
                                        onCancel.run();
                                    }
                                }
                        )
                        .setPositiveButton(
                                "Confirm",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    if (dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ) != null) {

                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setOnClickListener(
                                v -> {

                                    dialog.dismiss();

                                    if (onConfirm != null) {
                                        onConfirm.run();
                                    }
                                }
                        );
                    }
                }
        );

        dialog.setOnCancelListener(
                d -> {

                    if (onCancel != null) {
                        onCancel.run();
                    }
                }
        );

        dialog.show();
    }

    private static String getTitle(
            ActionParser.ParsedAction action
    ) {

        if (action == null ||
                action.type == null) {

            return "Confirm action";
        }

        String type =
                action.type.toLowerCase();

        switch (type) {

            case "call":
                return "Confirm call";

            case "sms":
                return "Confirm message";

            case "forget_memory":
                return "Confirm memory deletion";

            case "external_send":
                return "Confirm external action";

            default:
                return "Confirm action";
        }
    }

    private static String getDescription(
            ActionParser.ParsedAction action
    ) {

        if (action == null) {
            return "JARVIS wants to perform an action.";
        }

        String type =
                action.type == null
                        ? ""
                        : action.type.toLowerCase();

        if ("call".equals(type)) {

            String phone =
                    safe(action.phone);

            if (phone.isEmpty()) {
                return "JARVIS wants to start a phone call.";
            }

            return "JARVIS wants to call:\n" + phone;
        }

        if ("sms".equals(type)) {

            String phone =
                    safe(action.phone);

            String message =
                    safe(action.message);

            if (phone.isEmpty()) {
                return "JARVIS wants to send an SMS.";
            }

            if (message.isEmpty()) {
                return "JARVIS wants to send an SMS to:\n"
                        + phone;
            }

            return "JARVIS wants to send an SMS to:\n"
                    + phone
                    + "\n\n"
                    + message;
        }

        if ("forget_memory".equals(type)) {

            String label =
                    safe(action.label);

            if (label.isEmpty()) {
                return "JARVIS wants to forget stored memory.";
            }

            return "JARVIS wants to forget:\n"
                    + label;
        }

        if ("external_send".equals(type)) {

            return "JARVIS wants to perform an external action. "
                    + "Please confirm that you want to continue.";
        }

        String label =
                safe(action.label);

        if (!label.isEmpty()) {
            return "JARVIS wants to perform:\n"
                    + label;
        }

        return "JARVIS wants to perform an action.";
    }

    private static String safe(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}
