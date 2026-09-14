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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "jarvis_data";
    private static final String ENTRIES_KEY = "entries";
    private static final String BRAIN_URL_KEY = "brain_url";

    private final int BG = Color.rgb(5, 10, 19);
    private final int PANEL = Color.rgb(13, 23, 38);
    private final int BLUE = Color.rgb(0, 217, 255);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(180, 195, 210);

    private LinearLayout root;
    private SharedPreferences preferences;
    private ArrayList<Entry> entries = new ArrayList<>();

    private TextView pageTitle;
    private TextView pageSubtitle;

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

    // ---------------------------------------------------------
    // DATA MODEL
    // ---------------------------------------------------------

    private static class Entry {

        String id;
        String day;
        String title;
        String time;
        String description;
        boolean reminder;

        Entry(
                String id,
                String day,
                String title,
                String time,
                String description,
                boolean reminder
        ) {
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

                entries.add(
                        new Entry(
                                object.optString("id"),
                                object.optString("day"),
                                object.optString("title"),
                                object.optString("time"),
                                object.optString("description"),
                                object.optBoolean("reminder")
                        )
                );
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

    // ---------------------------------------------------------
    // MAIN UI
    // ---------------------------------------------------------

    private void prepareScreen(String title, String subtitle) {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 35);
        root.setBackgroundColor(BG);

        scrollView.addView(root);
        setContentView(scrollView);

        pageTitle = text(title, 32, BLUE);
        pageTitle.setGravity(Gravity.CENTER);

        pageSubtitle = text(subtitle, 16, MUTED);
        pageSubtitle.setGravity(Gravity.CENTER);

        root.addView(pageTitle);
        root.addView(space(5));
        root.addView(pageSubtitle);
        root.addView(space(25));
    }

    private TextView text(String value, float size, int color) {

        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, 6, 0, 6);

        return view;
    }

    private Button button(String label) {

        Button button = new Button(this);

        button.setText(label);
        button.setTextColor(WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackgroundColor(Color.rgb(55, 65, 80));
        button.setPadding(15, 8, 15, 8);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        58
                );

        params.setMargins(0, 7, 0, 7);
        button.setLayoutParams(params);

        return button;
    }

    private Space space(int height) {

        Space space = new Space(this);

        space.setLayoutParams(
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
        );

        return space;
    }

    private LinearLayout panel() {

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(20, 20, 20, 20);
        panel.setBackgroundColor(PANEL);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 8, 0, 8);
        panel.setLayoutParams(params);

        return panel;
    }

    // ---------------------------------------------------------
    // HOME
    // ---------------------------------------------------------

    private void showHome() {

        String date = new SimpleDateFormat(
                "EEEE, dd MMMM yyyy",
                Locale.getDefault()
        ).format(new Date());

        prepareScreen("J A R V I S", date);

        TextView status = text(
                "● SYSTEM ONLINE\n\nYour personal AI assistant is ready.",
                18,
                WHITE
        );

        status.setGravity(Gravity.CENTER);
        root.addView(status);
        root.addView(space(20));

        LinearLayout todayPanel = panel();

        todayPanel.addView(
                text(
                        "TODAY'S SCHEDULE",
                        19,
                        BLUE
                )
        );

        String today = new SimpleDateFormat(
                "EEEE",
                Locale.ENGLISH
        ).format(new Date());

        ArrayList<Entry> todayEntries = getEntriesForDay(today);

        if (todayEntries.isEmpty()) {

            todayPanel.addView(
                    text(
                            "No entries for today.",
                            16,
                            MUTED
                    )
            );

        } else {

            for (Entry entry : todayEntries) {

                todayPanel.addView(
                        text(
                                "• " + entry.time + "  " + entry.title,
                                16,
                                WHITE
                        )
                );
            }
        }

        root.addView(todayPanel);

        Button timetable = button("▣  Today's Timetable");
        timetable.setOnClickListener(v -> showToday());
        root.addView(timetable);

        Button edit = button("✎  Edit Timetable");
        edit.setOnClickListener(v -> showEditor());
        root.addView(edit);

        Button add = button("＋  Add New Entry");
        add.setOnClickListener(v -> showEntryDialog(null));
        root.addView(add);

        Button timer = button("◷  Countdown Timer");
        timer.setOnClickListener(v -> showTimerScreen());
        root.addView(timer);

        Button calendar = button("▦  Calendar");
        calendar.setOnClickListener(v -> showCalendar());
        root.addView(calendar);

        Button voice = button("◉  Voice Assistant");
        voice.setOnClickListener(v -> startVoiceAssistant());
        root.addView(voice);

        Button brain = button("⌁  Jarvis Brain");
        brain.setOnClickListener(v -> showBrainDialog());
        root.addView(brain);

        Button settings = button("⚙  Settings");
        settings.setOnClickListener(v -> showSettings());
        root.addView(settings);
    }

    // ---------------------------------------------------------
    // CURRENT DAY
    // ---------------------------------------------------------

    private void showToday() {

        String today = new SimpleDateFormat(
                "EEEE",
                Locale.ENGLISH
        ).format(new Date());

        prepareScreen(
                today.toUpperCase(Locale.ENGLISH),
                "Your schedule for today"
        );

        ArrayList<Entry> todayEntries = getEntriesForDay(today);

        if (todayEntries.isEmpty()) {

            root.addView(
                    text(
                            "Nothing scheduled today.",
                            18,
                            MUTED
                    )
            );

        } else {

            for (Entry entry : todayEntries) {

                LinearLayout card = panel();

                card.addView(
                        text(
                                entry.time + "  •  " + entry.title,
                                19,
                                BLUE
                        )
                );

                if (!entry.description.isEmpty()) {

                    card.addView(
                            text(
                                    entry.description,
                                    15,
                                    MUTED
                            )
                    );
                }

                root.addView(card);
            }
        }

        Button back = button("← Back Home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
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

    // ---------------------------------------------------------
    // EDITOR
    // ---------------------------------------------------------

    private void showEditor() {

        prepareScreen(
                "TIMETABLE EDITOR",
                "Create and manage your schedule"
        );

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

            LinearLayout dayPanel = panel();

            dayPanel.addView(
                    text(
                            day.toUpperCase(Locale.ENGLISH),
                            18,
                            BLUE
                    )
            );

            ArrayList<Entry> dayEntries = getEntriesForDay(day);

            if (dayEntries.isEmpty()) {

                dayPanel.addView(
                        text(
                                "No entries",
                                14,
                                MUTED
                        )
                );

            } else {

                for (Entry entry : dayEntries) {

                    Button entryButton = button(
                            entry.time + "  " + entry.title
                    );

                    entryButton.setOnClickListener(
                            v -> showEntryDialog(entry)
                    );

                    dayPanel.addView(entryButton);
                }
            }

            root.addView(dayPanel);
        }

        Button add = button("＋ Add Entry");
        add.setOnClickListener(v -> showEntryDialog(null));
        root.addView(add);

        Button back = button("← Back Home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showEntryDialog(Entry editing) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(25, 10, 25, 5);

        Spinner daySpinner = new Spinner(this);

        String[] days = {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        days
                );

        daySpinner.setAdapter(adapter);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Activity name");

        EditText timeInput = new EditText(this);
        timeInput.setHint("Time, example: 08:00 AM");

        EditText descriptionInput = new EditText(this);
        descriptionInput.setHint("Description");

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
                                        ? "Add Timetable Entry"
                                        : "Edit Timetable Entry"
                        )
                        .setView(layout)
                        .setPositiveButton(
                                "SAVE",
                                null
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String title = titleInput.getText()
                                    .toString()
                                    .trim();

                            String time = timeInput.getText()
                                    .toString()
                                    .trim();

                            String description = descriptionInput.getText()
                                    .toString()
                                    .trim();

                            if (title.isEmpty() || time.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "Activity and time are required.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            if (editing == null) {

                                Entry entry = new Entry(
                                        UUID.randomUUID().toString(),
                                        daySpinner.getSelectedItem().toString(),
                                        title,
                                        time,
                                        description,
                                        reminderCheck.isChecked()
                                );

                                entries.add(entry);

                                if (entry.reminder) {
                                    scheduleReminder(entry);
                                }

                            } else {

                                editing.day =
                                        daySpinner.getSelectedItem().toString();

                                editing.title = title;
                                editing.time = time;
                                editing.description = description;
                                editing.reminder = reminderCheck.isChecked();

                                if (editing.reminder) {
                                    scheduleReminder(editing);
                                }
                            }

                            saveEntries();

                            dialog.dismiss();
                            showEditor();
                        })
        );

        if (editing != null) {

            builder.setNeutralButton(
                    "DELETE",
                    (d, which) -> {

                        entries.remove(editing);
                        saveEntries();
                        showEditor();
                    }
            );
        }

        dialog.show();
    }

    // ---------------------------------------------------------
    // REMINDERS
    // ---------------------------------------------------------

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            ReminderReceiver.CHANNEL_ID,
                            "Jarvis Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleReminder(Entry entry) {

        Toast.makeText(
                this,
                "Reminder saved. Use Android alarm permissions if needed.",
                Toast.LENGTH_SHORT
        ).show();

        Calendar calendar = Calendar.getInstance();

        String[] timeParts = entry.time
                .replace(".", "")
                .split(" ");

        try {

            String clock = timeParts[0];
            String[] hm = clock.split(":");

            int hour = Integer.parseInt(hm[0]);
            int minute = Integer.parseInt(hm[1]);

            if (timeParts.length > 1) {

                String ampm = timeParts[1].toUpperCase(Locale.ENGLISH);

                if (ampm.equals("PM") && hour < 12) {
                    hour += 12;
                }

                if (ampm.equals("AM") && hour == 12) {
                    hour = 0;
                }
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("title", entry.title);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            this,
                            entry.id.hashCode(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );

                } else {

                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            }

        } catch (Exception ignored) {

            Toast.makeText(
                    this,
                    "Use time format like 08:30 AM",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ---------------------------------------------------------
    // COUNTDOWN TIMER
    // ---------------------------------------------------------

    private void showTimerScreen() {

        prepareScreen(
                "COUNTDOWN TIMER",
                "Focus mode"
        );

        EditText minutesInput = new EditText(this);
        minutesInput.setHint("Minutes");
        minutesInput.setInputType(2);

        root.addView(minutesInput);

        TextView countdown = text(
                "00:00",
                45,
                BLUE
        );

        countdown.setGravity(Gravity.CENTER);
        root.addView(countdown);

        Button start = button("▶ Start Timer");

        start.setOnClickListener(v -> {

            try {

                long minutes = Long.parseLong(
                        minutesInput.getText().toString()
                );

                if (activeTimer != null) {
                    activeTimer.cancel();
                }

                activeTimer = new CountDownTimer(
                        minutes * 60 * 1000,
                        1000
                ) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                        long totalSeconds =
                                millisUntilFinished / 1000;

                        long mins = totalSeconds / 60;
                        long secs = totalSeconds % 60;

                        countdown.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        mins,
                                        secs
                                )
                        );
                    }

                    @Override
                    public void onFinish() {

                        countdown.setText("TIME COMPLETE");

                        Toast.makeText(
                                MainActivity.this,
                                "Jarvis timer completed.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                }.start();

            } catch (Exception ignored) {

                Toast.makeText(
                        this,
                        "Enter a valid number of minutes.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        root.addView(start);

        Button stop = button("■ Stop Timer");

        stop.setOnClickListener(v -> {

            if (activeTimer != null) {
                activeTimer.cancel();
            }

            countdown.setText("00:00");
        });

        root.addView(stop);

        Button back = button("← Back Home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    // ---------------------------------------------------------
    // CALENDAR
    // ---------------------------------------------------------

    private void showCalendar() {

        prepareScreen(
                "CALENDAR",
                "Choose a date"
        );

        Button chooseDate = button("▦ Select Date");

        chooseDate.setOnClickListener(v -> {

            Calendar now = Calendar.getInstance();

            DatePickerDialog picker =
                    new DatePickerDialog(
                            this,
                            (view, year, month, dayOfMonth) -> {

                                Calendar selected = Calendar.getInstance();

                                selected.set(
                                        year,
                                        month,
                                        dayOfMonth
                                );

                                String formatted =
                                        new SimpleDateFormat(
                                                "EEEE, dd MMMM yyyy",
                                                Locale.getDefault()
                                        ).format(selected.getTime());

                                new AlertDialog.Builder(this)
                                        .setTitle(formatted)
                                        .setMessage(
                                                "Use the timetable editor to create recurring weekly activities."
                                        )
                                        .setPositiveButton("OK", null)
                                        .show();
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );

            picker.show();
        });

        root.addView(chooseDate);

        Button back = button("← Back Home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    // ---------------------------------------------------------
    // VOICE ASSISTANT
    // ---------------------------------------------------------

    private void startVoiceAssistant() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            Toast.makeText(
                    this,
                    "Voice recognition is not available on this phone.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Intent intent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak to Jarvis"
        );

        startActivityForResult(intent, 900);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 900 &&
                resultCode == RESULT_OK &&
                data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (results != null && !results.isEmpty()) {

                String command = results.get(0);

                new AlertDialog.Builder(this)
                        .setTitle("You said")
                        .setMessage(command)
                        .setPositiveButton(
                                "Send to Brain",
                                (dialog, which) -> sendToBrain(command)
                        )
                        .setNegativeButton("Close", null)
                        .show();
            }
        }
    }

    // ---------------------------------------------------------
    // BRAIN CONNECTION
    // ---------------------------------------------------------

    private void showBrainDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(25, 10, 25, 10);

        EditText urlInput = new EditText(this);

        urlInput.setHint(
                "Brain URL, example: https://your-server.com/chat"
        );

        urlInput.setText(
                preferences.getString(BRAIN_URL_KEY, "")
        );

        layout.addView(urlInput);

        new AlertDialog.Builder(this)
                .setTitle("Connect Jarvis Brain")
                .setMessage(
                        "Enter your backend API URL. Never place secret API keys inside the Android app."
                )
                .setView(layout)
                .setPositiveButton(
                        "SAVE",
                        (dialog, which) -> {

                            preferences.edit()
                                    .putString(
                                            BRAIN_URL_KEY,
                                            urlInput.getText().toString().trim()
                                    )
                                    .apply();

                            Toast.makeText(
                                    this,
                                    "Brain URL saved.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void sendToBrain(String message) {

        String brainUrl =
                preferences.getString(BRAIN_URL_KEY, "");

        if (brainUrl.isEmpty()) {

            Toast.makeText(
                    this,
                    "First configure your Jarvis Brain URL.",
                    Toast.LENGTH_LONG
            ).show();

            showBrainDialog();
            return;
        }

        Toast.makeText(
                this,
                "Sending command to Jarvis Brain...",
                Toast.LENGTH_SHORT
        ).show();

        new Thread(() -> {

            try {

                URL url = new URL(brainUrl);

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                JSONObject body = new JSONObject();
                body.put("message", message);

                OutputStream output =
                        connection.getOutputStream();

                output.write(body.toString().getBytes());
                output.flush();
                output.close();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream()
                                )
                        );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                runOnUiThread(() -> {

                    new AlertDialog.Builder(this)
                            .setTitle("JARVIS BRAIN")
                            .setMessage(response.toString())
                            .setPositiveButton("OK", null)
                            .show();
                });

                connection.disconnect();

            } catch (Exception error) {

                runOnUiThread(() -> {

                    new AlertDialog.Builder(this)
                            .setTitle("Brain Connection Failed")
                            .setMessage(error.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                });
            }

        }).start();
    }

    // ---------------------------------------------------------
    // SETTINGS
    // ---------------------------------------------------------

    private void showSettings() {

        prepareScreen(
                "SETTINGS",
                "Customize your Jarvis assistant"
        );

        Button notifications = button("Notification Settings");

        notifications.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    );

            intent.putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    getPackageName()
            );

            startActivity(intent);
        });

        root.addView(notifications);

        Button brain = button("Configure Brain URL");
        brain.setOnClickListener(v -> showBrainDialog());
        root.addView(brain);

        Button clear = button("Clear All Timetable Data");

        clear.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Clear timetable?")
                    .setMessage("All saved entries will be deleted.")
                    .setPositiveButton(
                            "DELETE",
                            (dialog, which) -> {

                                entries.clear();
                                saveEntries();
                                showHome();
                            }
                    )
                    .setNegativeButton("CANCEL", null)
                    .show();
        });

        root.addView(clear);

        Button back = button("← Back Home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    // ---------------------------------------------------------
    // PERMISSIONS
    // ---------------------------------------------------------

    private void requestPermissionsIfNeeded() {

        ArrayList<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            permissions.add(
                    Manifest.permission.RECORD_AUDIO
            );
        }

        if (!permissions.isEmpty()) {

            ActivityCompat.requestPermissions(
                    this,
                    permissions.toArray(new String[0]),
                    700
            );
        }
    }
}
