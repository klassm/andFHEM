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

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

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
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_FINISHED;
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_RESET;
import static li.klass.fhem.constants.Actions.UPDATE_DEVICE_WITH_UPDATE_MAP;
import static li.klass.fhem.constants.Actions.UPDATE_IF_REQUIRED;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.SENDER;
import static li.klass.fhem.constants.BundleExtraKeys.UPDATE_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.VIBRATE;
import static li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired;

public class RoomListIntentService extends ConvenientIntentService {

    @Inject
    RoomListService roomListService;

    private static final Logger LOG = LoggerFactory.getLogger(RoomListIntentService.class);

    public RoomListIntentService() {
        super(RoomListIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        LOG.info("handleIntent() - receiving intent with action {}", action);

        if (REMOTE_UPDATE_RESET.equals(action)) {
            LOG.trace("handleIntent() - resetting update progress");
            roomListService.resetUpdateProgress();
            return STATE.SUCCESS;
        }

        if (!REMOTE_UPDATE_FINISHED.equals(action) &&
                roomListService.updateRoomDeviceListIfRequired(intent, updatePeriod) == RemoteUpdateRequired.REQUIRED) {
            LOG.trace("handleIntent() - need to update room device list, intent is pending, so stopped here");
            return STATE.DONE;
        }

        if (GET_ALL_ROOMS_DEVICE_LIST.equals(action)) {
            LOG.trace("handleIntent() - handling all rooms device list");
            RoomDeviceList allRoomsDeviceList = roomListService.getAllRoomsDeviceList();
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, allRoomsDeviceList);
        } else if (GET_ROOM_NAME_LIST.equals(action)) {
            LOG.trace("handleIntent() - resolving room name list");
            ArrayList<String> roomNameList = roomListService.getRoomNameList();
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, ROOM_LIST, roomNameList);
        } else if (GET_ROOM_DEVICE_LIST.equals(action)) {
            String roomName = intent.getStringExtra(ROOM_NAME);
            LOG.trace("handleIntent() - resolving device list for room={}", roomName);
            RoomDeviceList roomDeviceList = roomListService.getDeviceListForRoom(roomName);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, roomDeviceList);
        } else if (GET_DEVICE_FOR_NAME.equals(action)) {
            String deviceName = intent.getStringExtra(DEVICE_NAME);
            LOG.trace("handleIntent() - resolving device for name={}", deviceName);
            Optional<Device> device = roomListService.getDeviceForName(deviceName);
            if (!device.isPresent()) {
                LOG.info("cannot find device for {}", deviceName);
                return STATE.ERROR;
            }
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE, device.get());
        } else if (UPDATE_DEVICE_WITH_UPDATE_MAP.equals(action)) {
            String deviceName = intent.getStringExtra(DEVICE_NAME);
            LOG.trace("handleIntent() - updating device with update map, device={}", deviceName);
            @SuppressWarnings("unchecked")
            Map<String, String> updates = (Map<String, String>) intent.getSerializableExtra(UPDATE_MAP);
            boolean vibrateUponNotification = intent.getBooleanExtra(VIBRATE, false);

            roomListService.parseReceivedDeviceStateMap(deviceName, updates, vibrateUponNotification);

            sendBroadcast(new Intent(Actions.DO_UPDATE));
        } else if (REMOTE_UPDATE_FINISHED.equals(action)) {
            LOG.trace("handleIntent() - remote update finished");
            roomListService.remoteUpdateFinished();
        } else if (UPDATE_IF_REQUIRED.equals(action)) {
            // If required, the device list will be updated by now. The resend intent will reach us
            // here. The only thing we have to do is notify the receiver that we have
            // updated the device list.
            LOG.trace("handleIntent() - update if required found");
            if (resultReceiver != null) {
                LOG.trace("handleIntent() - sending success to result receiver");
                sendNoResult(resultReceiver, ResultCodes.SUCCESS);
            } else if (intent.hasExtra(SENDER)) {
                Class<?> sender = (Class<?>) intent.getSerializableExtra(SENDER);
                LOG.trace("handleIntent() - sending success intent to {}", sender == null ? "null (?!)" : sender.getSimpleName());
                startService(new Intent(intent)
                        .setAction(REMOTE_UPDATE_FINISHED)
                        .setClass(this, sender));
            }
        }

        return STATE.DONE;
    }

    private void sendResultWithLastUpdate(ResultReceiver receiver, int resultCode,
                                          String bundleExtrasKey, Serializable value) {
        if (receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(bundleExtrasKey, value);
            bundle.putLong(BundleExtraKeys.LAST_UPDATE, roomListService.getLastUpdate());
            receiver.send(resultCode, bundle);
        }
    }
}
