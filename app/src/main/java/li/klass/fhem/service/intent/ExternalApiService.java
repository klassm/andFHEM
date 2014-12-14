/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.service.intent;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.room.RoomListService;

import static com.google.common.collect.Lists.newArrayList;

public class ExternalApiService extends Service {

    public static final int ROOM_LIST = 1;
    public static final int READINGS_VALUE = 2;
    private final Messenger messenger;

    @Inject
    RoomListService roomListService;

    @Inject
    CommandExecutionService commandExecutionService;
    private Message replyMsg;

    public ExternalApiService() {
        messenger = new Messenger(new IncomingHandler(new WeakReference<>(this)));
    }

    private void replyTo(ArrayList<String> outgoing) {
        replyTo(replyMsg, outgoing);
    }

    private void replyTo(Message incoming, ArrayList<String> outgoing) {
        try {
            if (incoming.replyTo != null) {
                Message msg = Message.obtain(null, incoming.what);
                Bundle bundle = new Bundle();
                if (incoming.getData() != null) {
                    //if incoming message has data send it back along with the results
                    bundle = incoming.getData();
                }
                bundle.putStringArrayList("data", outgoing);
                msg.setData(bundle);
                incoming.replyTo.send(msg);
            } else {
                Log.e(ExternalApiService.class.getName(), "cannot send message, no replyTo Messenger set.");
            }
        } catch (RemoteException e) {
            Log.e(ExternalApiService.class.getName(), "cannot send message", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndFHEMApplication) getApplication()).inject(this);
    }

    static class IncomingHandler extends Handler {

        private final WeakReference<ExternalApiService> externalApiServiceWeakReference;

        public IncomingHandler(WeakReference<ExternalApiService> externalApiService) {
            this.externalApiServiceWeakReference = externalApiService;
        }

        @Override
        public void handleMessage(Message msg) {
            final ExternalApiService externalApiService = externalApiServiceWeakReference.get();
            switch (msg.what) {
                case ROOM_LIST:
                    ArrayList<String> deviceNames = externalApiService.roomListService.getAvailableDeviceNames();
                    externalApiService.replyTo(msg, deviceNames);

                    break;
                case READINGS_VALUE:
                    String deviceName = null;
                    String readingName = null;
                    String defaultVal = null;
                    //FIXME: create a new Message to reply with because we will loose msg somewhere in the AsyncTask below
                    externalApiService.replyMsg = Message.obtain(null, msg.what);
                    externalApiService.replyMsg.setData(msg.getData());
                    externalApiService.replyMsg.replyTo = msg.replyTo;
                    if (msg.getData() != null) {
                        if (msg.getData().getString("device") != null) {
                            deviceName = msg.getData().getString("device");
                        }
                        if (msg.getData().getString("reading") != null) {
                            readingName = msg.getData().getString("reading");
                        }
                        if (msg.getData().getString("default") != null) {
                            defaultVal = msg.getData().getString("default");
                        }
                        if (deviceName != null && readingName != null && defaultVal != null) {
                            final Handler handler = new Handler();
                            new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... params) {
                                    return externalApiService.commandExecutionService.executeSafely(String.format("{ReadingsVal('%s','%s','%s')}", params[0], params[1], params[2]));
                                }

                                @Override
                                protected void onPostExecute(final String result) {
                                    // onPostExecute is run from within the UI thread, but Android allows to run multiple UI threads.
                                    // We cannot be sure which one is chosen, so we enforce the right UI thread by using an explicit
                                    // handler.
                                    // see http://stackoverflow.com/questions/10426120/android-got-calledfromwrongthreadexception-in-onpostexecute-how-could-it-be
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ArrayList<String> readingsVal = newArrayList();
                                            readingsVal.add(result);
                                            externalApiService.replyTo(readingsVal);
                                        }
                                    });
                                }
                            }.execute(deviceName, readingName, defaultVal);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}