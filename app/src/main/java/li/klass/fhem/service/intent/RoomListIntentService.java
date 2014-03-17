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

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.room.RoomListService;

import static li.klass.fhem.constants.Actions.GET_ALL_ROOMS_DEVICE_LIST;
import static li.klass.fhem.constants.Actions.GET_DEVICE_FOR_NAME;
import static li.klass.fhem.constants.Actions.GET_ROOM_DEVICE_LIST;
import static li.klass.fhem.constants.Actions.GET_ROOM_NAME_LIST;
import static li.klass.fhem.constants.Actions.UPDATE_DEVICE_WITH_UPDATE_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.UPDATE_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.VIBRATE;

public class RoomListIntentService extends ConvenientIntentService {

    public RoomListIntentService() {
        super(RoomListIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        RoomListService roomListService = RoomListService.INSTANCE;
        if (GET_ALL_ROOMS_DEVICE_LIST.equals(intent.getAction())) {
            RoomDeviceList allRoomsDeviceList = roomListService.getAllRoomsDeviceList(updatePeriod);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, allRoomsDeviceList);
        } else if (GET_ROOM_NAME_LIST.equals(intent.getAction())) {
            ArrayList<String> roomNameList = roomListService.getRoomNameList(updatePeriod);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, ROOM_LIST, roomNameList);
        } else if (GET_ROOM_DEVICE_LIST.equals(intent.getAction())) {
            String roomName = intent.getStringExtra(ROOM_NAME);
            RoomDeviceList roomDeviceList = roomListService.getDeviceListForRoom(roomName, updatePeriod);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, roomDeviceList);
        } else if (GET_DEVICE_FOR_NAME.equals(intent.getAction())) {
            String deviceName = intent.getStringExtra(DEVICE_NAME);
            Device device = roomListService.getDeviceForName(deviceName, updatePeriod);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE, device);
        } else if (UPDATE_DEVICE_WITH_UPDATE_MAP.equals(intent.getAction())) {
            String deviceName = intent.getStringExtra(DEVICE_NAME);
            @SuppressWarnings("unchecked")
            Map<String, String> updates = (Map<String, String>) intent.getSerializableExtra(UPDATE_MAP);
            boolean vibrateUponNotification = intent.getBooleanExtra(VIBRATE, false);

            roomListService.parseReceivedDeviceStateMap(deviceName, updates, vibrateUponNotification);

            sendBroadcast(new Intent(Actions.DO_UPDATE));
        }

        return STATE.DONE;
    }

    private void sendResultWithLastUpdate(ResultReceiver receiver, int resultCode,
                                          String bundleExtrasKey, Serializable value) {
        if (receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(bundleExtrasKey, value);
            bundle.putLong(BundleExtraKeys.LAST_UPDATE, RoomListService.INSTANCE.getLastUpdate());
            receiver.send(resultCode, bundle);
        }
    }

}
