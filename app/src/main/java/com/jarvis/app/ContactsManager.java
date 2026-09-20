package com.jarvis.app;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactsManager {

    public static class ContactInfo {

        public long id;
        public String name;
        public String phone;

        public ContactInfo(
                long id,
                String name,
                String phone
        ) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }
    }

    private final Context context;

    public ContactsManager(Context context) {
        this.context =
                context.getApplicationContext();
    }

    public List<ContactInfo> getContacts() {

        List<ContactInfo> result =
                new ArrayList<>();

        Cursor cursor = null;

        try {

            String[] projection = {
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

            cursor =
                    context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection,
                            null,
                            null,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                    + " ASC"
                    );

            if (cursor == null) {
                return result;
            }

            int idIndex =
                    cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                    );

            int nameIndex =
                    cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    );

            int phoneIndex =
                    cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
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

                String phone =
                        phoneIndex >= 0
                                ? cursor.getString(phoneIndex)
                                : "";

                if (isEmpty(name) &&
                        isEmpty(phone)) {
                    continue;
                }

                result.add(
                        new ContactInfo(
                                id,
                                safe(name),
                                safe(phone)
                        )
                );
            }

        } catch (SecurityException ignored) {

            // Contacts permission has not been granted.

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    public ContactInfo findContact(
            String query
    ) {

        if (isEmpty(query)) {
            return null;
        }

        String clean =
                query.trim()
                        .toLowerCase(Locale.getDefault());

        List<ContactInfo> contacts =
                getContacts();

        ContactInfo partialMatch = null;

        for (ContactInfo contact : contacts) {

            String name =
                    safe(contact.name)
                            .toLowerCase(
                                    Locale.getDefault()
                            );

            String phone =
                    safe(contact.phone)
                            .toLowerCase(
                                    Locale.getDefault()
                            );

            if (name.equals(clean) ||
                    phone.equals(clean)) {

                return contact;
            }

            if (name.contains(clean) ||
                    phone.contains(clean)) {

                if (partialMatch == null) {
                    partialMatch = contact;
                }
            }
        }

        return partialMatch;
    }

    public String findPhoneNumber(
            String name
    ) {

        ContactInfo contact =
                findContact(name);

        if (contact == null) {
            return "";
        }

        return safe(contact.phone);
    }

    public boolean hasContactsAccess() {

        Cursor cursor = null;

        try {

            cursor =
                    context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Phone._ID
                            },
                            null,
                            null,
                            null
                    );

            return cursor != null;

        } catch (SecurityException e) {

            return false;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean isEmpty(
            String value
    ) {

        return value == null ||
                value.trim().isEmpty();
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
