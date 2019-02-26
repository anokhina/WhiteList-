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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.List;

public class IncomingCallReceiver extends BroadcastReceiver {
    //private ITelephony telephonyService;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            final Class c = Class.forName(tm.getClass().getName());
            final Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            final ITelephony telephonyService = (ITelephony) m.invoke(tm);
            final Bundle bundle = intent.getExtras();
            final String phoneNumber = bundle.getString("incoming_number");
            Log.e("INCOMING", phoneNumber);
            //toast(context, "INCOMING>" + phoneNumber, Toast.LENGTH_SHORT);
            CursorUtil.phonebookContacts(context, phoneNumber,
                    new CursorUtil.Callback<List<Long>>(){
                        @Override
                        public void accept(List<Long> longs) {
                            if (longs.size() > 0) {

                            } else {
                                hangUp(telephonyService, phoneNumber);
                            }
                        }
                    },
                    new CursorUtil.Callback<List<Long>>(){
                        @Override
                        public void accept(List<Long> longs) {
                            if (longs.size() > 0) {
                                //TODO increase volume
                                AudioUtil.setSMSCallVolume(context, 100);
                            } else {
                                hangUp(telephonyService, phoneNumber);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hangUp(final ITelephony telephonyService, final String phoneNumber) {
        telephonyService.silenceRinger();
        telephonyService.endCall();
        Log.e("HANG UP", phoneNumber);
        //toast(context, "HANG UP>" + phoneNumber, Toast.LENGTH_SHORT);
        MainActivity.LAST_NOTICE = "LAST BLOCKED:" + phoneNumber;
    }

    private void toast(Context context, String msg, int length) {
        try {
            Toast.makeText(context, msg, length).show();
        } catch (Exception e) {
            Util.anyToast(context, msg, length);
        }
    }

}