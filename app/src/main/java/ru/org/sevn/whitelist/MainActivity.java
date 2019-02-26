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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final Map<String, Boolean> PERMISSIONS = new HashMap<>();
    public static String LAST_NOTICE = "Hi!";

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.tvHelloNotice);
        textView.setMovementMethod(new ScrollingMovementMethod());
        Util.askPermission(this, PERMISSIONS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
        );
        AudioUtil.accessDND(this);
    }

    private void develop() {
        zzz();
        zzz1(this, "4959876543");
        //(495) 987-6543
        //+7 495 987-12-12
        zzz1(this, "+7 495 987-12-12");
        zzz1(this, "+74959871212");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textView != null) {
            textView.setText(LAST_NOTICE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            Util.askPermissionResult(permissions, grantResults, PERMISSIONS);
        }
    }

    public static void zzz1(final Context context, final String number) {
        final Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        CursorUtil.getData(context.getContentResolver(), lookupUri, null, new CursorUtil.Callback<Cursor>() {
            @Override
            public void accept(Cursor cursor) {
                final long contactID = cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                final Integer name1 = cursor.getInt(cursor.getColumnIndex(ContactsContract.PhoneLookup.STARRED));
                Log.e("contact", "" + ":"+name+":"+contactID+":"+name1);
            }
        });
    }
    private void zzz() {
        if (Util.hasPermission(PERMISSIONS, Manifest.permission.READ_CONTACTS)) {
            CursorUtil.getData(getContentResolver(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, new CursorUtil.Callback<Cursor>() {
                @Override
                public void accept(Cursor cursor) {
                    String phoneNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Integer contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    Log.e("phoneNum", "" + Util.phoneNumber(phoneNum)+":"+phoneNum+":"+name+":"+contactId);
//                    CursorUtil.getData(getContentResolver(), ContactsContract.Contacts.CONTENT_URI, contactId, new CursorUtil.Callback<Cursor>() {
//                        @Override
//                        public void accept(Cursor cursor) {
//                            cursor.getColumnIndex(ContactsContract.Contacts.STARRED)
//                        }
//                    });
                }
            });
        } else {
            Log.e("phoneNum", "no PERM");
        }
    }
}
