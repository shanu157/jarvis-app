package com.jarvis.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "jarvis_timetable";
    private static final String TIMETABLE_KEY = "entries";

    private SharedPreferences preferences;
    private LinearLayout timetableContainer;

    private final String[] days = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        showHomeScreen();
    }

    private void showHomeScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 35, 28, 30);
        root.setBackgroundColor(Color.rgb(5, 11, 20));

        TextView title = new TextView(this);
        title.setText("JARVIS");
        title.setTextColor(Color.rgb(0, 191, 255));
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 18);
        root.addView(title);

        TextView date = new TextView(this);
        String today = new SimpleDateFormat(
                "EEEE, dd MMMM yyyy",
                Locale.getDefault()
        ).format(new Date());

        date.setText(today);
        date.setTextColor(Color.WHITE);
        date.setTextSize(18);
        date.setGravity(Gravity.CENTER);
        date.setPadding(0, 0, 0, 25);
        root.addView(date);

        TextView status = new TextView(this);
        status.setText(
                "Jarvis native Android mode\n\n" +
                "Your personal assistant is starting."
        );
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(17);
        status.setPadding(0, 0, 0, 25);
        root.addView(status);

        Button todayButton = new Button(this);
        todayButton.setText("TODAY'S TIMETABLE");
        todayButton.setOnClickListener(v -> showTodayTimetable());
        root.addView(todayButton);

        Button manageButton = new Button(this);
        manageButton.setText("EDIT TIMETABLE");
        manageButton.setOnClickListener(v -> showTimetableManager());
        root.addView(manageButton);

        Button addButton = new Button(this);
        addButton.setText("ADD NEW ENTRY");
        addButton.setOnClickListener(v -> showEntryDialog(-1));
        root.addView(addButton);

        Button notificationButton = new Button(this);
        notificationButton.setText("TEST NOTIFICATION");
        notificationButton.setOnClickListener(
                v -> Toast.makeText(
                        this,
                        "Notification test can be added next.",
                        Toast.LENGTH_SHORT
                ).show()
        );
        root.addView(notificationButton);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(root);

        setContentView(scrollView);
    }

    private void showTodayTimetable() {
        String today = new SimpleDateFormat(
                "EEEE",
                Locale.getDefault()
        ).format(new Date());

        ArrayList<TimetableEntry> entries = loadEntries();

        StringBuilder message = new StringBuilder();

        message.append(today).append("'s Timetable\n\n");

        boolean found = false;

        for (TimetableEntry entry : entries) {
            if (entry.day.equals(today)) {
                message.append("• ")
                        .append(entry.time)
                        .append(" - ")
                        .append(entry.title)
                        .append("\n");

                if (!entry.description.isEmpty()) {
                    message.append("  ")
                            .append(entry.description)
                            .append("\n");
                }

                message.append("\n");
                found = true;
            }
        }

        if (!found) {
            message.append("No timetable entry for today.");
        }

        new AlertDialog.Builder(this)
                .setTitle("Today's Timetable")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton(
                        "Edit Timetable",
                        (dialog, which) -> showTimetableManager()
                )
                .show();
    }

    private void showTimetableManager() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(root);

        TextView heading = new TextView(this);
        heading.setText("Your Timetable");
        heading.setTextSize(24);
        heading.setTextColor(Color.WHITE);
        heading.setPadding(0, 0, 0, 20);
        root.addView(heading);

        ArrayList<TimetableEntry> entries = loadEntries();

        if (entries.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No entries yet. Add your first timetable entry.");
            empty.setTextSize(17);
            empty.setPadding(0, 0, 0, 20);
            root.addView(empty);
        }

        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            TimetableEntry entry = entries.get(i);

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(0, 12, 0, 12);

            TextView text = new TextView(this);
            text.setText(
                    entry.day + "\n" +
                    entry.time + " - " + entry.title +
                    (entry.description.isEmpty()
                            ? ""
                            : "\n" + entry.description)
            );
            text.setTextSize(17);
            text.setTextColor(Color.WHITE);
            item.addView(text);

            LinearLayout buttons = new LinearLayout(this);
            buttons.setOrientation(LinearLayout.HORIZONTAL);

            Button editButton = new Button(this);
            editButton.setText("EDIT");
            editButton.setOnClickListener(
                    v -> showEntryDialog(index)
            );

            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE");
            deleteButton.setOnClickListener(
                    v -> confirmDelete(index)
            );

            buttons.addView(editButton);
            buttons.addView(deleteButton);
            item.addView(buttons);

            root.addView(item);
        }

        Button addButton = new Button(this);
        addButton.setText("ADD ENTRY");
        addButton.setOnClickListener(v -> showEntryDialog(-1));
        root.addView(addButton);

        new AlertDialog.Builder(this)
                .setView(scrollView)
                .setPositiveButton("CLOSE", null)
                .show();
    }

    private void showEntryDialog(int editIndex) {
        ArrayList<TimetableEntry> entries = loadEntries();

        TimetableEntry oldEntry = null;

        if (editIndex >= 0 && editIndex < entries.size()) {
            oldEntry = entries.get(editIndex);
        }

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(35, 15, 35, 10);

        Spinner daySpinner = new Spinner(this);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                days
        );

        daySpinner.setAdapter(dayAdapter);

        if (oldEntry != null) {
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(oldEntry.day)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
        }

        form.addView(daySpinner);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Activity name");
        titleInput.setSingleLine(true);

        if (oldEntry != null) {
            titleInput.setText(oldEntry.title);
        }

        form.addView(titleInput);

        EditText timeInput = new EditText(this);
        timeInput.setHint("Time, e.g. 8:00 AM - 10:00 AM");
        timeInput.setSingleLine(true);

        if (oldEntry != null) {
            timeInput.setText(oldEntry.time);
        }

        form.addView(timeInput);

        EditText descriptionInput = new EditText(this);
        descriptionInput.setHint("Description, optional");
        descriptionInput.setSingleLine(false);

        if (oldEntry != null) {
            descriptionInput.setText(oldEntry.description);
        }

        form.addView(descriptionInput);

        String dialogTitle =
                editIndex >= 0
                        ? "Edit Timetable Entry"
                        : "Add Timetable Entry";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(dialogTitle)
                .setView(form)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton(
                        "SAVE",
                        null
                )
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            );

            saveButton.setOnClickListener(v -> {
                String day = daySpinner.getSelectedItem().toString();
                String title = titleInput.getText().toString().trim();
                String time = timeInput.getText().toString().trim();
                String description =
                        descriptionInput.getText().toString().trim();

                if (title.isEmpty() || time.isEmpty()) {
                    Toast.makeText(
                            this,
                            "Please enter activity name and time.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                TimetableEntry newEntry = new TimetableEntry(
                        day,
                        title,
                        time,
                        description
                );

                if (editIndex >= 0 && editIndex < entries.size()) {
                    entries.set(editIndex, newEntry);
                } else {
                    entries.add(newEntry);
                }

                saveEntries(entries);

                dialog.dismiss();

                Toast.makeText(
                        this,
                        "Timetable saved.",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });

        dialog.show();
    }

    private void confirmDelete(int index) {
        ArrayList<TimetableEntry> entries = loadEntries();

        if (index < 0 || index >= entries.size()) {
            return;
        }

        TimetableEntry entry = entries.get(index);

        new AlertDialog.Builder(this)
                .setTitle("Delete Entry?")
                .setMessage(
                        entry.day + "\n" +
                        entry.time + " - " +
                        entry.title
                )
                .setNegativeButton("CANCEL", null)
                .setPositiveButton(
                        "DELETE",
                        (dialog, which) -> {
                            entries.remove(index);
                            saveEntries(entries);
                            showTimetableManager();
                        }
                )
                .show();
    }

    private ArrayList<TimetableEntry> loadEntries() {
        ArrayList<TimetableEntry> entries = new ArrayList<>();

        String saved = preferences.getString(
                TIMETABLE_KEY,
                "[]"
        );

        try {
            JSONArray array = new JSONArray(saved);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                entries.add(
                        new TimetableEntry(
                                object.getString("day"),
                                object.getString("title"),
                                object.getString("time"),
                                object.optString("description", "")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return entries;
    }

    private void saveEntries(ArrayList<TimetableEntry> entries) {
        JSONArray array = new JSONArray();

        try {
            for (TimetableEntry entry : entries) {
                JSONObject object = new JSONObject();

                object.put("day", entry.day);
                object.put("title", entry.title);
                object.put("time", entry.time);
                object.put("description", entry.description);

                array.put(object);
            }

            preferences.edit()
                    .putString(TIMETABLE_KEY, array.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TimetableEntry {
        String day;
        String title;
        String time;
        String description;

        TimetableEntry(
                String day,
                String title,
                String time,
                String description
        ) {
            this.day = day;
            this.title = title;
            this.time = time;
            this.description = description;
        }
    }
}
