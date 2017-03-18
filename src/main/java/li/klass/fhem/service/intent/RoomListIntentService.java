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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.device.GraphDefinitionsForDeviceService;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.Tasker;

import static li.klass.fhem.constants.Actions.CLEAR_DEVICE_LIST;
import static li.klass.fhem.constants.Actions.DEVICE_GRAPH_DEFINITIONS;
import static li.klass.fhem.constants.Actions.GET_ALL_ROOMS_DEVICE_LIST;
import static li.klass.fhem.constants.Actions.GET_DEVICE_FOR_NAME;
import static li.klass.fhem.constants.Actions.GET_ROOM_DEVICE_LIST;
import static li.klass.fhem.constants.Actions.GET_ROOM_NAME_LIST;
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_FINISHED;
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_RESET;
import static li.klass.fhem.constants.Actions.UPDATE_DEVICE_WITH_UPDATE_MAP;
import static li.klass.fhem.constants.Actions.UPDATE_IF_REQUIRED;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.LAST_UPDATE;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.SENDER;
import static li.klass.fhem.constants.BundleExtraKeys.SUCCESS;
import static li.klass.fhem.constants.BundleExtraKeys.UPDATE_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.VIBRATE;
import static li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired;

public class RoomListIntentService extends ConvenientIntentService {

    @Inject
    RoomListService roomListService;

    @Inject
    GraphDefinitionsForDeviceService graphDefinitionsForDeviceService;

    private static final Logger LOG = LoggerFactory.getLogger(RoomListIntentService.class);

    public RoomListIntentService() {
        super(RoomListIntentService.class.getName());
    }

    @Override
    protected State handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        LOG.info("handleIntent() - receiving intent with action {}", action);

        Optional<String> connectionId = Optional.fromNullable(intent.getStringExtra(CONNECTION_ID));
        if (REMOTE_UPDATE_RESET.equals(action)) {
            LOG.trace("handleIntent() - resetting update progress");
            roomListService.resetUpdateProgress(this);
            return State.SUCCESS;
        }

        if (!REMOTE_UPDATE_FINISHED.equals(action) &&
                roomListService.updateRoomDeviceListIfRequired(intent, updatePeriod, this) == RemoteUpdateRequired.REQUIRED) {
            LOG.trace("handleIntent() - need to update room device list, intent is pending, so stop here");
            return State.DONE;
        }

        if (GET_ALL_ROOMS_DEVICE_LIST.equals(action)) {
            LOG.trace("handleIntent() - handling all rooms device list");
            RoomDeviceList allRoomsDeviceList = roomListService.getAllRoomsDeviceList(connectionId, this);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, allRoomsDeviceList, connectionId);
        } else if (GET_ROOM_NAME_LIST.equals(action)) {
            LOG.trace("handleIntent() - resolving room name list");
            ArrayList<String> roomNameList = roomListService.getRoomNameList(connectionId, this);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, ROOM_LIST, roomNameList, connectionId);
        } else if (GET_ROOM_DEVICE_LIST.equals(action)) {
            String roomName = intent.getStringExtra(ROOM_NAME);
            LOG.trace("handleIntent() - resolving device list for room={}", roomName);
            RoomDeviceList roomDeviceList = roomListService.getDeviceListForRoom(roomName, connectionId, this);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS, DEVICE_LIST, roomDeviceList, connectionId);
        } else if (GET_DEVICE_FOR_NAME.equals(action)) {
            String deviceName = intent.getStringExtra(DEVICE_NAME);
            LOG.trace("handleIntent() - resolving device for name={}", deviceName);
            Optional<FhemDevice> device = roomListService.getDeviceForName(deviceName, connectionId, this);
            if (!device.isPresent()) {
                LOG.info("cannot find device for {}", deviceName);
                return State.ERROR;
            }
            ImmutableSet<SvgGraphDefinition> svgGraphDefinitions = graphDefinitionsForDeviceService.graphDefinitionsFor(this, device.get().getXmlListDevice(), connectionId);
            sendResultWithLastUpdate(resultReceiver, ResultCodes.SUCCESS,
                    ImmutableMap.of(DEVICE, device.get(), DEVICE_GRAPH_DEFINITIONS, svgGraphDefinitions), connectionId);
        } else if (UPDATE_DEVICE_WITH_UPDATE_MAP.equals(action)) {
            String deviceName = intent.getStringExtra(DEVICE_NAME);
            LOG.trace("handleIntent() - updating device with update map, device={}", deviceName);
            @SuppressWarnings("unchecked")
            Map<String, String> updates = (Map<String, String>) intent.getSerializableExtra(UPDATE_MAP);
            boolean vibrateUponNotification = intent.getBooleanExtra(VIBRATE, false);

            roomListService.parseReceivedDeviceStateMap(deviceName, updates, vibrateUponNotification, this);
            Tasker.requestQuery(this);
        } else if (REMOTE_UPDATE_FINISHED.equals(action)) {
            LOG.trace("handleIntent() - remote update finished");
            roomListService.remoteUpdateFinished(this, intent.getBooleanExtra(SUCCESS, true));
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
        } else if (CLEAR_DEVICE_LIST.equals(action)) {
            roomListService.clearDeviceList(connectionId, this);
        }

        return State.DONE;
    }

    private void sendResultWithLastUpdate(ResultReceiver receiver, int resultCode,
                                          String bundleExtrasKey, Serializable value, Optional<String> connectionId) {
        sendResultWithLastUpdate(receiver, resultCode, ImmutableMap.of(bundleExtrasKey, value), connectionId);
    }

    private void sendResultWithLastUpdate(ResultReceiver receiver, int resultCode,
                                          Map<String, Serializable> values, Optional<String> connectionId) {
        if (receiver != null) {
            Bundle bundle = new Bundle();
            for (Map.Entry<String, Serializable> entry : values.entrySet()) {
                bundle.putSerializable(entry.getKey(), entry.getValue());
            }
            bundle.putLong(LAST_UPDATE, roomListService.getLastUpdate(connectionId, this));
            receiver.send(resultCode, bundle);
        }
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
