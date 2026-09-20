package com.jarvis.app;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "jarvis_data";
    private static final String ENTRIES_KEY = "entries";
    private static final String BRAIN_URL_KEY = "brain_url";

    private static final int BG = Color.rgb(4, 8, 15);
    private static final int PANEL = Color.rgb(11, 18, 30);
    private static final int PANEL_2 = Color.rgb(16, 26, 42);
    private static final int BLUE = Color.rgb(0, 217, 255);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(155, 171, 190);
    private static final int GREEN = Color.rgb(60, 220, 145);

    private LinearLayout root;
    private SharedPreferences preferences;
    private ArrayList<Entry> entries = new ArrayList<>();

    private CountDownTimer activeTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadEntries();

        createNotificationChannel();
        requestPermissionsIfNeeded();

        showHome();
    }

    // =========================================================
    // DATA
    // =========================================================

    private static class Entry {
        String id;
        String day;
        String title;
        String time;
        String description;
        boolean reminder;

        Entry(String id, String day, String title, String time,
              String description, boolean reminder) {
            this.id = id;
            this.day = day;
            this.title = title;
            this.time = time;
            this.description = description;
            this.reminder = reminder;
        }
    }

    private void loadEntries() {
        entries.clear();

        String saved = preferences.getString(ENTRIES_KEY, "[]");

        try {
            JSONArray array = new JSONArray(saved);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                entries.add(new Entry(
                        object.optString("id"),
                        object.optString("day"),
                        object.optString("title"),
                        object.optString("time"),
                        object.optString("description"),
                        object.optBoolean("reminder")
                ));
            }
        } catch (Exception ignored) {
        }
    }

    private void saveEntries() {
        JSONArray array = new JSONArray();

        try {
            for (Entry entry : entries) {
                JSONObject object = new JSONObject();

                object.put("id", entry.id);
                object.put("day", entry.day);
                object.put("title", entry.title);
                object.put("time", entry.time);
                object.put("description", entry.description);
                object.put("reminder", entry.reminder);

                array.put(object);
            }

            preferences.edit()
                    .putString(ENTRIES_KEY, array.toString())
                    .apply();

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // COMMON UI
    // =========================================================

    private void baseScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(35));
        root.setBackgroundColor(BG);

        scroll.addView(root);
        setContentView(scroll);
    }

    private TextView label(String value, float size, int color) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    private TextView centerLabel(String value, float size, int color) {
        TextView v = label(value, size, color);
        v.setGravity(Gravity.CENTER);
        return v;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp((int) radius));
        return d;
    }

    private LinearLayout vertical() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        return box;
    }

    private LinearLayout horizontal() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        return box;
    }

    private void addSpace(int height) {
        Space s = new Space(this);
        s.setLayoutParams(new LinearLayout.LayoutParams(1, dp(height)));
        root.addView(s);
    }

    private Button actionButton(String text) {
        Button b = new Button(this);

        b.setText(text);
        b.setTextColor(WHITE);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(8), 0, dp(8), 0);
        b.setBackground(rounded(PANEL_2, 18));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        p.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(p);

        return b;
    }

    private LinearLayout card() {
        LinearLayout c = vertical();
        c.setPadding(dp(16), dp(15), dp(16), dp(15));
        c.setBackground(rounded(PANEL, 18));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        p.setMargins(0, dp(6), 0, dp(6));
        c.setLayoutParams(p);

        return c;
    }

    private TextView topTitle() {
        TextView title = label("JARVIS", 28, WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        return title;
    }

    // =========================================================
    // HOME
    // =========================================================

    private void showHome() {
        baseScreen();

        LinearLayout header = horizontal();

        LinearLayout branding = vertical();

        TextView title = topTitle();
        branding.addView(title);

        TextView status = label("●  SYSTEM ONLINE", 12, GREEN);
        branding.addView(status);

        header.addView(
                branding,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView version = centerLabel("v4.0", 11, MUTED);
        version.setBackground(rounded(PANEL_2, 20));
        version.setPadding(dp(12), dp(7), dp(12), dp(7));
        header.addView(version);

        root.addView(header);

        addSpace(24);

        String greeting = getGreeting();

        TextView hello = label(greeting, 25, WHITE);
        hello.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(hello);

        TextView subtitle = label(
                "What can I help you with?",
                15,
                MUTED
        );
        root.addView(subtitle);

        addSpace(15);

        // Main ask box
        LinearLayout ask = vertical();
        ask.setPadding(dp(16), dp(14), dp(16), dp(14));
        ask.setBackground(rounded(PANEL_2, 20));

        EditText input = new EditText(this);
        input.setHint("Ask Jarvis anything...");
        input.setHintTextColor(Color.rgb(110, 128, 150));
        input.setTextColor(WHITE);
        input.setTextSize(16);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setPadding(0, 0, 0, dp(8));
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);

        ask.addView(input);

        LinearLayout sendRow = horizontal();

        Button attach = actionButton("＋ Attach");
        Button voice = actionButton("◉ Voice");
        Button send = actionButton("➤ Send");

        send.setTextColor(BLUE);

        sendRow.addView(attach);
        sendRow.addView(voice);
        sendRow.addView(send);

        ask.addView(sendRow);

        root.addView(ask);

        attach.setOnClickListener(v -> showFileInfo());
        voice.setOnClickListener(v -> startVoiceAssistant());

        View.OnClickListener sendListener = v -> {
            String text = input.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(
                        this,
                        "Type something for Jarvis.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            showBrainDialog(text);
        };

        send.setOnClickListener(sendListener);

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendListener.onClick(v);
                return true;
            }
            return false;
        });

        addSpace(18);

        TextView quickTitle = label("QUICK ACTIONS", 13, MUTED);
        quickTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(quickTitle);

        LinearLayout row1 = horizontal();

        Button schedule = actionButton("▣\nSchedule");
        Button timer = actionButton("◷\nTimer");

        row1.addView(schedule);
        row1.addView(timer);
        root.addView(row1);

        LinearLayout row2 = horizontal();

        Button calendar = actionButton("▦\nCalendar");
        Button brain = actionButton("⌁\nBrain");

        row2.addView(calendar);
        row2.addView(brain);
        root.addView(row2);

        schedule.setOnClickListener(v -> showToday());
        timer.setOnClickListener(v -> showTimerScreen());
        calendar.setOnClickListener(v -> showCalendar());
        brain.setOnClickListener(v -> showBrainDialog());

        addSpace(20);

        TextView recentTitle = label("RECENT / SCHEDULE", 13, MUTED);
        recentTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(recentTitle);

        String today = new SimpleDateFormat(
                "EEEE",
                Locale.ENGLISH
        ).format(new Date());

        ArrayList<Entry> todayEntries = getEntriesForDay(today);

        if (todayEntries.isEmpty()) {
            LinearLayout empty = card();

            empty.addView(
                    label(
                            "No schedule entries for today.",
                            14,
                            MUTED
                    )
            );

            root.addView(empty);
        } else {
            for (Entry entry : todayEntries) {
                LinearLayout c = card();

                TextView time = label(
                        entry.time,
                        13,
                        BLUE
                );

                TextView name = label(
                        entry.title,
                        17,
                        WHITE
                );

                c.addView(time);
                c.addView(name);

                if (!entry.description.isEmpty()) {
                    c.addView(
                            label(
                                    entry.description,
                                    13,
                                    MUTED
                            )
                    );
                }

                root.addView(c);
            }
        }

        addSpace(12);

        LinearLayout bottom = horizontal();

        Button files = actionButton("Files");
        Button settings = actionButton("⚙ Settings");

        bottom.addView(files);
        bottom.addView(settings);

        root.addView(bottom);

        files.setOnClickListener(v -> showFiles());
        settings.setOnClickListener(v -> showSettings());
    }

    private String getGreeting() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) return "Good morning.";
        if (hour < 18) return "Good afternoon.";
        return "Good evening.";
    }

    // =========================================================
    // FILES
    // =========================================================

    private void showFiles() {
        baseScreen();

        TextView title = label("FILES", 28, WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        root.addView(
                label(
                        "Access files and connected sources",
                        14,
                        MUTED
                )
        );

        addSpace(20);

        addFileCard(
                "PHONE",
                "Images, PDF, documents, downloads and local files",
                "Open Android file picker"
        );

        addFileCard(
                "GOOGLE DRIVE",
                "Connect Drive for cloud documents and spreadsheets",
                "Drive connection"
        );

        addFileCard(
                "WEB",
                "Give Jarvis a URL to read a web page or online file",
                "Web access"
        );

        addSpace(12);

        Button back = actionButton("← Home");
        root.addView(back);

        back.setOnClickListener(v -> showHome());
    }

    private void addFileCard(
            String title,
            String description,
            String buttonText
    ) {
        LinearLayout c = card();

        TextView t = label(title, 17, BLUE);
        t.setTypeface(null, android.graphics.Typeface.BOLD);

        c.addView(t);
        c.addView(label(description, 14, MUTED));

        Button b = actionButton(buttonText);
        c.addView(b);

        b.setOnClickListener(v -> showFileInfo());

        root.addView(c);
    }

    private void showFileInfo() {
        new AlertDialog.Builder(this)
                .setTitle("Jarvis Files")
                .setMessage(
                        "File understanding is being connected to Jarvis Brain.\n\n" +
                        "Planned support:\n" +
                        "• PDF\n" +
                        "• DOC / DOCX\n" +
                        "• TXT / MD\n" +
                        "• XLS / XLSX / CSV\n" +
                        "• PPT / PPTX\n" +
                        "• Images\n" +
                        "• Audio / video\n" +
                        "• Web pages\n" +
                        "• Google Drive"
                )
                .setPositiveButton("OK", null)
                .show();
    }

    // =========================================================
    // BRAIN
    // =========================================================

    private void showBrainDialog() {
        showBrainDialog("");
    }

    private void showBrainDialog(String initialText) {
        LinearLayout layout = vertical();
        layout.setPadding(dp(20), dp(5), dp(20), 0);

        EditText input = new EditText(this);
        input.setText(initialText);
        input.setHint("Ask Jarvis...");
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);

        layout.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("JARVIS BRAIN")
                .setMessage(
                        "Connect this screen to your Jarvis Brain server.\n\n" +
                        "Current architecture:\n" +
                        "Android → Jarvis Brain → AI"
                )
                .setView(layout)
                .setPositiveButton("SEND", (dialog, which) -> {
                    String text = input.getText().toString().trim();

                    if (!text.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Sending to Jarvis Brain...",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("CLOSE", null)
                .show();
    }

    // =========================================================
    // BRAIN STATUS
    // =========================================================

    private void showBrainScreen() {
        baseScreen();

        TextView title = label("JARVIS BRAIN", 28, WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        root.addView(
                label(
                        "System capabilities",
                        14,
                        MUTED
                )
        );

        addSpace(18);

        addStatus("AI", "Ready");
        addStatus("VISION", "Ready");
        addStatus("MEMORY", "Ready");
        addStatus("FILES", "Ready");
        addStatus("WEB", "Ready");
        addStatus("TERMUX", "On demand");

        addSpace(15);

        Button ask = actionButton("Ask Jarvis");
        root.addView(ask);

        ask.setOnClickListener(v -> showBrainDialog());

        Button back = actionButton("← Home");
        root.addView(back);

        back.setOnClickListener(v -> showHome());
    }

    private void addStatus(String name, String status) {
        LinearLayout c = card();

        LinearLayout row = horizontal();

        TextView n = label(name, 15, WHITE);
        n.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView s = label(status, 14, GREEN);
        s.setGravity(Gravity.RIGHT);

        row.addView(
                n,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        row.addView(s);

        c.addView(row);
        root.addView(c);
    }

    // =========================================================
    // TODAY
    // =========================================================

    private void showToday() {
        baseScreen();

        String today = new SimpleDateFormat(
                "EEEE",
                Locale.ENGLISH
        ).format(new Date());

        TextView title = label(
                today.toUpperCase(Locale.ENGLISH),
                28,
                WHITE
        );

        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        root.addView(
                label(
                        "Today's schedule",
                        14,
                        MUTED
                )
        );

        addSpace(15);

        ArrayList<Entry> todayEntries = getEntriesForDay(today);

        if (todayEntries.isEmpty()) {
            LinearLayout c = card();

            c.addView(
                    centerLabel(
                            "Nothing scheduled today.",
                            16,
                            MUTED
                    )
            );

            root.addView(c);
        } else {
            for (Entry entry : todayEntries) {
                LinearLayout c = card();

                c.addView(
                        label(
                                entry.time,
                                13,
                                BLUE
                        )
                );

                c.addView(
                        label(
                                entry.title,
                                19,
                                WHITE
                        )
                );

                if (!entry.description.isEmpty()) {
                    c.addView(
                            label(
                                    entry.description,
                                    14,
                                    MUTED
                            )
                    );
                }

                root.addView(c);
            }
        }

        addSpace(12);

        Button edit = actionButton("✎ Edit Timetable");
        root.addView(edit);

        edit.setOnClickListener(v -> showEditor());

        Button back = actionButton("← Home");
        root.addView(back);

        back.setOnClickListener(v -> showHome());
    }

    private ArrayList<Entry> getEntriesForDay(String day) {
        ArrayList<Entry> result = new ArrayList<>();

        for (Entry entry : entries) {
            if (entry.day.equalsIgnoreCase(day)) {
                result.add(entry);
            }
        }

        Collections.sort(
                result,
                Comparator.comparing(entry -> entry.time)
        );

        return result;
    }

    // =========================================================
    // EDITOR
    // =========================================================

    private void showEditor() {
        baseScreen();

        TextView title = label(
                "TIMETABLE",
                28,
                WHITE
        );

        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        root.addView(
                label(
                        "Create and manage your schedule",
                        14,
                        MUTED
                )
        );

        addSpace(15);

        String[] days = {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
        };

        for (String day : days) {
            ArrayList<Entry> dayEntries = getEntriesForDay(day);

            if (dayEntries.isEmpty()) continue;

            LinearLayout c = card();

            c.addView(
                    label(
                            day.toUpperCase(Locale.ENGLISH),
                            14,
                            BLUE
                    )
            );

            for (Entry entry : dayEntries) {
                Button b = actionButton(
                        entry.time + "  •  " + entry.title
                );

                c.addView(b);

                b.setOnClickListener(
                        v -> showEntryDialog(entry)
                );
            }

            root.addView(c);
        }

        Button add = actionButton("＋ Add New Entry");
        root.addView(add);

        add.setOnClickListener(v -> showEntryDialog(null));

        Button back = actionButton("← Home");
        root.addView(back);

        back.setOnClickListener(v -> showHome());
    }

    private void showEntryDialog(Entry editing) {
        LinearLayout layout = vertical();
        layout.setPadding(dp(20), dp(5), dp(20), 0);

        String[] days = {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
        };

        Spinner daySpinner = new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        days
                );

        daySpinner.setAdapter(adapter);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Activity name");
        titleInput.setTextColor(WHITE);

        EditText timeInput = new EditText(this);
        timeInput.setHint("Time, example: 08:00 AM");
        timeInput.setTextColor(WHITE);

        EditText descriptionInput = new EditText(this);
        descriptionInput.setHint("Description");
        descriptionInput.setTextColor(WHITE);

        CheckBox reminderCheck = new CheckBox(this);
        reminderCheck.setText("Create reminder notification");
        reminderCheck.setTextColor(WHITE);

        layout.addView(daySpinner);
        layout.addView(titleInput);
        layout.addView(timeInput);
        layout.addView(descriptionInput);
        layout.addView(reminderCheck);

        if (editing != null) {
            titleInput.setText(editing.title);
            timeInput.setText(editing.time);
            descriptionInput.setText(editing.description);
            reminderCheck.setChecked(editing.reminder);

            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(editing.day)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                editing == null
                                        ? "Add Entry"
                                        : "Edit Entry"
                        )
                        .setView(layout)
                        .setPositiveButton("SAVE", null)
                        .setNegativeButton("CANCEL", null)
                        .create();

        dialog.setOnShowListener(
                ignored ->
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setOnClickListener(v -> {

                                    String title =
                                            titleInput.getText()
                                                    .toString()
                                                    .trim();

                                    String time =
                                            timeInput.getText()
                                                    .toString()
                                                    .trim();

                                    String description =
                                            descriptionInput.getText()
                                                    .toString()
                                                    .trim();

                                    if (title.isEmpty() ||
                                            time.isEmpty()) {

                                        Toast.makeText(
                                                this,
                                                "Activity and time are required.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }

                                    if (editing == null) {

                                        Entry entry =
                                                new Entry(
                                                        UUID.randomUUID()
                                                                .toString(),
                                                        daySpinner
                                                                .getSelectedItem()
                                                                .toString(),
                                                        title,
                                                        time,
                                                        description,
                                                        reminderCheck
                                                                .isChecked()
                                                );

                                        entries.add(entry);

                                        if (entry.reminder) {
                                            scheduleReminder(entry);
                                        }

                                    } else {

                                        editing.day =
                                                daySpinner
                                                        .getSelectedItem()
                                                        .toString();

                                        editing.title = title;
                                        editing.time = time;
                                        editing.description =
                                                description;
                                        editing.reminder =
                                                reminderCheck.isChecked();

                                        if (editing.reminder) {
                                            scheduleReminder(editing);
                                        }
                                    }

                                    saveEntries();

                                    dialog.dismiss();
                                    showEditor();
                                })
        );

        dialog.show();
    }

    // =========================================================
    // TIMER
    // =========================================================

    private void showTimerScreen() {
        baseScreen();

        TextView title = label(
                "COUNTDOWN",
                28,
                WHITE
        );

        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        root.addView(
                label(
                        "Simple Jarvis countdown timer",
                        14,
                        MUTED
                )
        );

        addSpace(20);

        EditText minutes = new EditText(this);
        minutes.setHint("Minutes");
        minutes.setTextColor(WHITE);
        minutes.setHintTextColor(MUTED);
        minutes.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );
        minutes.setGravity(Gravity.CENTER);
        minutes.setTextSize(18);
        minutes.setBackground(rounded(PANEL_2, 18));

        root.addView(
                minutes,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(58)
                )
        );

        addSpace(15);

        TextView display = centerLabel(
                "00:00",
                48,
                BLUE
        );

        display.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        root.addView(display);

        addSpace(15);

        Button start = actionButton("▶ Start");
        root.addView(start);

        start.setOnClickListener(v -> {
            String value =
                    minutes.getText().toString().trim();

            if (value.isEmpty()) {
                Toast.makeText(
                        this,
                        "Enter minutes first.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            try {
                long mins = Long.parseLong(value);

                if (mins <= 0) {
                    throw new NumberFormatException();
                }

                if (activeTimer != null) {
                    activeTimer.cancel();
                }

                activeTimer =
                        new CountDownTimer(
                                mins * 60_000L,
                                1000
                        ) {
                            @Override
                            public void onTick(long millis) {
                                long total = millis / 1000;
                                long m = total / 60;
                                long s = total % 60;

                                display.setText(
                                        String.format(
                                                Locale.getDefault(),
                                                "%02d:%02d",
                                                m,
                                                s
                                        )
                                );
                            }

                            @Override
                            public void onFinish() {
                                display.setText("00:00");

                                Toast.makeText(
                                        MainActivity.this,
                                        "Jarvis timer finished.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }.start();

            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Invalid minutes.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Button stop = actionButton("■ Stop");
        root.addView(stop);

        stop.setOnClickListener(v -> {
            if (activeTimer != null) {
                activeTimer.cancel();
                activeTimer = null;
            }

            display.setText("00:00");
        });

        Button back = actionButton("← Home");
        root.addView(back);

        back.setOnClickListener(v -> showHome());
    }

    // =========================================================
    // CALENDAR
    // =========================================================

    private void showCalendar() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog picker =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {

                            String date =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            day,
                                            month + 1,
                                            year
                                    );

                            Toast.makeText(
                                    this,
                                    "Selected " + date,
                                    Toast.LENGTH_SHORT
                            ).show();
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );

        picker.show();
    }

    // =========================================================
    // VOICE
    // =========================================================

    private void startVoiceAssistant() {
        try {
            Intent intent =
                    new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                    );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Speak to Jarvis..."
            );

            startActivityForResult(intent, 1001);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Voice recognition is not available.",
                    Toast.LENGTH_SHORT
            ).show();
        }
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

        if (requestCode == 1001 &&
                resultCode == RESULT_OK &&
                data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (results != null &&
                    !results.isEmpty()) {

                showBrainDialog(results.get(0));
            }
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void showSettings() {
        baseScreen();

        TextView title = label(
                "SETTINGS",
                28,
                WHITE
        );

        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        root.addView(
                label(
                        "Configure your Jarvis app",
                        14,
                        MUTED
                )
        );

        addSpace(18);

        addSetting(
                "Jarvis Brain",
                "Configure the backend URL",
                v -> showBrainUrlDialog()
        );

        addSetting(
                "Notifications",
                "Reminder notification permissions",
                v -> requestPermissionsIfNeeded()
        );

        addSetting(
                "Voice",
                "Android speech recognition",
                v -> startVoiceAssistant()
        );

        addSetting(
                "System",
                "Android alarm and app settings",
                v -> openSystemSettings()
        );

        addSpace(15);

        Button back = actionButton("← Home");
        root.addView(back);

        back.setOnClickListener(v -> showHome());
    }

    private void addSetting(
            String title,
            String description,
            View.OnClickListener listener
    ) {
        LinearLayout c = card();

        TextView t = label(title, 17, WHITE);
        t.setTypeface(null, android.graphics.Typeface.BOLD);

        c.addView(t);
        c.addView(
                label(
                        description,
                        13,
                        MUTED
                )
        );

        Button b = actionButton("Open");
        c.addView(b);

        b.setOnClickListener(listener);

        root.addView(c);
    }

    private void showBrainUrlDialog() {
        EditText input = new EditText(this);

        input.setHint(
                "http://127.0.0.1:5000"
        );

        input.setText(
                preferences.getString(
                        BRAIN_URL_KEY,
                        "http://127.0.0.1:5000"
                )
        );

        input.setTextColor(WHITE);

        new AlertDialog.Builder(this)
                .setTitle("Jarvis Brain URL")
                .setMessage(
                        "Enter the address of your Jarvis Brain server."
                )
                .setView(input)
                .setPositiveButton(
                        "SAVE",
                        (dialog, which) -> {

                            String url =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (!url.isEmpty()) {
                                preferences.edit()
                                        .putString(
                                                BRAIN_URL_KEY,
                                                url
                                        )
                                        .apply();
                            }
                        }
                )
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void openSystemSettings() {
        try {
            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            startActivity(intent);

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // REMINDERS
    // =========================================================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            "jarvis_reminders",
                            "Jarvis Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Jarvis timetable reminders"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void requestPermissionsIfNeeded() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        2001
                );
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    2002
            );
        }
    }

    private void scheduleReminder(Entry entry) {
        try {
            String[] parts =
                    entry.time.replaceAll("[^0-9:]", "")
                            .split(":");

            if (parts.length < 2) return;

            int hour =
                    Integer.parseInt(parts[0]);

            int minute =
                    Integer.parseInt(parts[1]);

            if (entry.time.toUpperCase(Locale.ENGLISH)
                    .contains("PM") &&
                    hour < 12) {
                hour += 12;
            }

            if (entry.time.toUpperCase(Locale.ENGLISH)
                    .contains("AM") &&
                    hour == 12) {
                hour = 0;
            }

            Calendar calendar =
                    Calendar.getInstance();

            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    hour
            );

            calendar.set(
                    Calendar.MINUTE,
                    minute
            );

            calendar.set(
                    Calendar.SECOND,
                    0
            );

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent =
                    new Intent(
                            this,
                            ReminderReceiver.class
                    );

            intent.putExtra(
                    "title",
                    entry.title
            );

            intent.putExtra(
                    "description",
                    entry.description
            );

            int requestCode =
                    Math.abs(entry.id.hashCode());

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            this,
                            requestCode,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    (Build.VERSION.SDK_INT >= 23
                                            ? PendingIntent.FLAG_IMMUTABLE
                                            : 0)
                    );

            AlarmManager alarm =
                    (AlarmManager)
                            getSystemService(
                                    Context.ALARM_SERVICE
                            );

            if (alarm == null) return;

            if (Build.VERSION.SDK_INT >= 23) {

                if (Build.VERSION.SDK_INT >= 31 &&
                        !alarm.canScheduleExactAlarms()) {

                    Toast.makeText(
                            this,
                            "Enable exact alarms for reminders.",
                            Toast.LENGTH_LONG
                    ).show();

                    try {
                        startActivity(
                                new Intent(
                                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                )
                        );
                    } catch (Exception ignored) {
                    }

                    return;
                }

                alarm.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );

            } else {

                alarm.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

        } catch (Exception ignored) {
        }
    }
}
