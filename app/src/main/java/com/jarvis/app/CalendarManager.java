package com.jarvis.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalendarManager {

    public static class CalendarInfo {

        public long id;
        public String name;
        public String accountName;
        public String accountType;

        public CalendarInfo(
                long id,
                String name,
                String accountName,
                String accountType
        ) {
            this.id = id;
            this.name = name;
            this.accountName = accountName;
            this.accountType = accountType;
        }
    }

    private final Context context;
    private final ContentResolver resolver;

    public CalendarManager(Context context) {

        this.context =
                context.getApplicationContext();

        this.resolver =
                this.context.getContentResolver();
    }

    public List<CalendarInfo> getCalendars() {

        List<CalendarInfo> result =
                new ArrayList<>();

        Cursor cursor = null;

        try {

            String[] projection = {
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.ACCOUNT_NAME,
                    CalendarContract.Calendars.ACCOUNT_TYPE
            };

            cursor =
                    resolver.query(
                            CalendarContract.Calendars.CONTENT_URI,
                            projection,
                            CalendarContract.Calendars.VISIBLE + "=?",
                            new String[]{"1"},
                            CalendarContract.Calendars._ID + " ASC"
                    );

            if (cursor == null) {
                return result;
            }

            int idIndex =
                    cursor.getColumnIndex(
                            CalendarContract.Calendars._ID
                    );

            int nameIndex =
                    cursor.getColumnIndex(
                            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
                    );

            int accountNameIndex =
                    cursor.getColumnIndex(
                            CalendarContract.Calendars.ACCOUNT_NAME
                    );

            int accountTypeIndex =
                    cursor.getColumnIndex(
                            CalendarContract.Calendars.ACCOUNT_TYPE
                    );

            while (cursor.moveToNext()) {

                long id =
                        idIndex >= 0
                                ? cursor.getLong(idIndex)
                                : -1;

                String name =
                        nameIndex >= 0
                                ? cursor.getString(nameIndex)
                                : "";

                String accountName =
                        accountNameIndex >= 0
                                ? cursor.getString(accountNameIndex)
                                : "";

                String accountType =
                        accountTypeIndex >= 0
                                ? cursor.getString(accountTypeIndex)
                                : "";

                result.add(
                        new CalendarInfo(
                                id,
                                safe(name),
                                safe(accountName),
                                safe(accountType)
                        )
                );
            }

        } catch (SecurityException ignored) {

            // Calendar permission has not been granted.

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    public long getDefaultCalendarId() {

        List<CalendarInfo> calendars =
                getCalendars();

        if (calendars.isEmpty()) {
            return -1;
        }

        return calendars.get(0).id;
    }

    public long addEvent(
            String title,
            String description,
            long startMillis,
            long endMillis
    ) {

        return addEvent(
                getDefaultCalendarId(),
                title,
                description,
                startMillis,
                endMillis
        );
    }

    public long addEvent(
            long calendarId,
            String title,
            String description,
            long startMillis,
            long endMillis
    ) {

        if (calendarId < 0) {
            throw new IllegalStateException(
                    "No calendar is available."
            );
        }

        if (endMillis <= startMillis) {
            endMillis =
                    startMillis + 60 * 60 * 1000L;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                CalendarContract.Events.CALENDAR_ID,
                calendarId
        );

        values.put(
                CalendarContract.Events.TITLE,
                safe(title)
        );

        values.put(
                CalendarContract.Events.DESCRIPTION,
                safe(description)
        );

        values.put(
                CalendarContract.Events.DTSTART,
                startMillis
        );

        values.put(
                CalendarContract.Events.DTEND,
                endMillis
        );

        values.put(
                CalendarContract.Events.EVENT_TIMEZONE,
                TimeZone.getDefault()
                        .getID()
        );

        values.put(
                CalendarContract.Events.ALL_DAY,
                0
        );

        Uri result =
                resolver.insert(
                        CalendarContract.Events.CONTENT_URI,
                        values
                );

        if (result == null) {
            throw new IllegalStateException(
                    "Unable to create calendar event."
            );
        }

        String id =
                result.getLastPathSegment();

        if (id == null) {
            return -1;
        }

        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean deleteEvent(
            long eventId
    ) {

        if (eventId < 0) {
            return false;
        }

        try {

            Uri uri =
                    CalendarContract.Events.CONTENT_URI
                            .buildUpon()
                            .appendPath(
                                    String.valueOf(eventId)
                            )
                            .build();

            int deleted =
                    resolver.delete(
                            uri,
                            null,
                            null
                    );

            return deleted > 0;

        } catch (SecurityException e) {

            return false;
        }
    }

    public boolean hasCalendarAccess() {

        try {

            return !getCalendars().isEmpty();

        } catch (Exception e) {

            return false;
        }
    }

    private String safe(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}
