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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import li.klass.fhem.service.room.RoomListService;

import static li.klass.fhem.service.room.RoomListService.NEVER_UPDATE_PERIOD;

public class ExternalApiService extends Service {

    public static final int ROOM_LIST = 1;

    private final Messenger messenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ROOM_LIST:
                    ArrayList<String> deviceNames = RoomListService.INSTANCE
                            .getAvailableDeviceNames(NEVER_UPDATE_PERIOD);
                    replyTo(msg, Message.obtain(null, 1, deviceNames));

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void replyTo(Message incoming, Message outgoing) {
        try {
            if (incoming.replyTo != null) {
                incoming.replyTo.send(outgoing);
            } else {
                messenger.send(outgoing);
            }
        } catch (RemoteException e) {
            Log.e(ExternalApiService.class.getName(), "cannot send message", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}
