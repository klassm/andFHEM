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
import android.os.ResultReceiver;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.room.RoomListService;

import java.util.ArrayList;

import static li.klass.fhem.constants.Actions.*;
import static li.klass.fhem.constants.BundleExtraKeys.*;

public class RoomListIntentService extends ConvenientIntentService {

    public RoomListIntentService() {
        super(RoomListIntentService.class.getName());

    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {

        RoomListService roomListService = RoomListService.INSTANCE;
        if (intent.getAction().equals(GET_ALL_ROOMS_DEVICE_LIST)) {
            RoomDeviceList allRoomsDeviceList = roomListService.getAllRoomsDeviceList(updatePeriod);
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, allRoomsDeviceList);
        } else if (intent.getAction().equals(GET_ROOM_NAME_LIST)) {
            ArrayList<String> roomNameList = roomListService.getRoomNameList(updatePeriod);
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, ROOM_LIST, roomNameList);
        } else if (intent.getAction().equals(GET_ROOM_DEVICE_LIST)) {
            String roomName = intent.getStringExtra(ROOM_NAME);
            RoomDeviceList roomDeviceList = roomListService.getDeviceListForRoom(roomName, updatePeriod);
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, roomDeviceList);
        } else if (intent.getAction().equals(GET_DEVICE_FOR_NAME)) {
            String deviceName = intent.getStringExtra(DEVICE_NAME);
            Device device = roomListService.getDeviceForName(deviceName, updatePeriod);
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, DEVICE, device);
        }

        return STATE.DONE;
    }
}
