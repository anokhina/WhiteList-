/*
 * Copyright 2019 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ru.org.sevn.whitelist;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class CursorUtil {
    public static abstract class Callback<T> {
        public abstract void accept(T t);
        public void finished() {};
    }

    public static void getContactData(final ContentResolver contentResolver, final Integer id, final Callback<Cursor> callback) {
        if (callback != null) {
            final String whereClause;
            if (id != null) {
                final StringBuffer whereClauseBuf = new StringBuffer();
                whereClauseBuf.append(ContactsContract.Data._ID);
                whereClauseBuf.append("=");
                whereClauseBuf.append(id);
                whereClause = whereClauseBuf.toString();
            } else {
                whereClause = null;
            }
           Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                   whereClause, null, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        callback.accept(cursor);
                    } while (cursor.moveToNext());
                }
                callback.finished();
            } finally {
                cursor.close();
            }
        }
    }

    public static void getData(final ContentResolver contentResolver, final Uri uri, final Integer id, final Callback<Cursor> callback) {
        getData(contentResolver, uri, null, id, callback);
    }
    public static void getData(final ContentResolver contentResolver, final Uri uri, final String[] projection, final Integer id, final Callback<Cursor> callback) {
        if (callback != null) {
            final String whereClause;
            if (id != null) {
                final StringBuffer whereClauseBuf = new StringBuffer();
                whereClauseBuf.append("_id");
                whereClauseBuf.append("=");
                whereClauseBuf.append(id);
                whereClause = whereClauseBuf.toString();
            } else {
                whereClause = null;
            }
            Cursor cursor = contentResolver.query(uri, projection,
                    whereClause, null, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        callback.accept(cursor);
                    } while (cursor.moveToNext());
                }
                callback.finished();
            } finally {
                cursor.close();
            }
        }
    }

    public static void phonebookContacts(final Context context, final String number, final Callback<List<Long>> callback, final Callback<List<Long>> callbackStar) {
        final List<Long> list = new ArrayList<>();
        final List<Long> starList = new ArrayList<>();
        final Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        CursorUtil.getData(context.getContentResolver(), lookupUri, new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.STARRED}, null, new CursorUtil.Callback<Cursor>() {
            @Override
            public void accept(Cursor cursor) {
                final long contactID = cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                final int starred = cursor.getInt(cursor.getColumnIndex(ContactsContract.PhoneLookup.STARRED));
                list.add(contactID);
                if (starred == 1) {
                    starList.add(contactID);
                }
            }

            @Override
            public void finished() {
                if (starList.size() > 0) {
                    if (callbackStar != null) {
                        callbackStar.accept(starList);
                    } else if (callback != null) {
                        callback.accept(list);
                    }
                } else {
                    if (callback != null) {
                        callback.accept(list);
                    }
                }
            }
        });
    }

}
