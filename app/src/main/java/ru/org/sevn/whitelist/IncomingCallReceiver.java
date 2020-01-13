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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class IncomingCallReceiver extends BroadcastReceiver {
    //private ITelephony telephonyService;

    public class HungUper {
        public void hangUp(String phoneNumber) {

        }
    }

    private void adjustVol(final Context context, final String phoneNumber) {
        if (phoneNumber.contains(MainActivity.SW_OFF_VOL)) {
            AudioUtil.setSMSCallVolume(context, 0);
        }
    }

    private void icall(final Context context, final String phoneNumber, final HungUper hungUper) {
        CursorUtil.phonebookContacts(context, phoneNumber,
                new CursorUtil.Callback<List<Long>>() {
                    @Override
                    public void accept(List<Long> longs) {
                        adjustVol(context, phoneNumber);
                        if (longs.size() > 0) {

                        } else {
                            hungUper.hangUp(phoneNumber);
                        }
                    }
                },
                new CursorUtil.Callback<List<Long>>() {
                    @Override
                    public void accept(List<Long> longs) {
                        adjustVol(context, phoneNumber);
                        if (longs.size() > 0) {
                            //TODO increase volume
                            if (!phoneNumber.contains(MainActivity.SW_OFF_VOL)) {
                                AudioUtil.setSMSCallVolume(context, 100);
                            }
                        } else {
                            hungUper.hangUp(phoneNumber);
                        }
                    }
                }
        );
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            final Class c = Class.forName(tm.getClass().getName());
            final Bundle bundle = intent.getExtras();
            final String phoneNumber = bundle.getString("incoming_number");
            Log.e("INCOMING", phoneNumber);
            final StringBuilder sb = new StringBuilder();
            for (final Method m : c.getDeclaredMethods()) {
                sb.append(">" + m.getName()).append("\n");
            }
            MainActivity.LAST_NOTICE = sb.toString();
            //toast(context, "INCOMING>" + phoneNumber, Toast.LENGTH_SHORT);
            HungUper hungUper;
            hungUper = new HungUper() {
                @Override
                public void hangUp(String phoneNumber) {
                    AudioUtil.setSMSCallVolume(context, 0);
                }
            };
            try {
                final Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                final ITelephony telephonyService = (ITelephony) m.invoke(tm);
                hungUper = new HungUper() {
                    @Override
                    public void hangUp(String phoneNumber) {
                        if (!IncomingCallReceiver.this.hangUp(telephonyService, phoneNumber)) {
                            AudioUtil.setSMSCallVolume(context, 0);
                        }
                    }
                };

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    final Method m = c.getDeclaredMethod("getITelephonyMSim");
                    m.setAccessible(true);
                    final Object telephonyService = m.invoke(tm);

                    final Method mcall = telephonyService.getClass().getMethod("endCall", int.class);
                    final Method mgetPreferredDataSubscription = telephonyService.getClass().getMethod("getPreferredDataSubscription");
                    final int csub = (int) mgetPreferredDataSubscription.invoke(telephonyService);

                    hungUper = new HungUper() {
                        @Override
                        public void hangUp(String phoneNumber) {
                            if (!hangUpOld(csub, mcall, telephonyService, phoneNumber)) {
                                AudioUtil.setSMSCallVolume(context, 0);
                            }
                        }
                    };
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
            icall(context, phoneNumber, hungUper);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean hangUpOld(int csub, Method mcall, final Object telephonyService, final String phoneNumber) {
        boolean ret = false;
        try {
            ret = (boolean) mcall.invoke(telephonyService, csub);
            if (!ret) {
                ret = (boolean) mcall.invoke(telephonyService, csub + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret) {
            Log.e("HANG UP", phoneNumber);
            //toast(context, "HANG UP>" + phoneNumber, Toast.LENGTH_SHORT);
            MainActivity.LAST_NOTICE = "LAST BLOCKED:" + phoneNumber;
        }
        return ret;
    }

    private boolean hangUp(final ITelephony telephonyService, final String phoneNumber) {
        try {
            telephonyService.silenceRinger();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            telephonyService.endCall();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        Log.e("HANG UP", phoneNumber);
        //toast(context, "HANG UP>" + phoneNumber, Toast.LENGTH_SHORT);
        MainActivity.LAST_NOTICE = "LAST BLOCKED:" + phoneNumber;
        return true;
    }

    private void toast(Context context, String msg, int length) {
        try {
            Toast.makeText(context, msg, length).show();
        } catch (Exception e) {
            Util.anyToast(context, msg, length);
        }
    }

}