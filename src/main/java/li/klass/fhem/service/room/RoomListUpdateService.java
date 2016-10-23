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

package li.klass.fhem.service.room;

import android.content.Context;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.exception.CommandExecutionException;
import li.klass.fhem.service.CommandExecutionService;

import static com.google.common.base.Optional.absent;

@Singleton
public class RoomListUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(RoomListUpdateService.class);

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    DeviceListParser deviceListParser;

    @Inject
    RoomListHolderService roomListHolderService;

    @Inject
    public RoomListUpdateService() {
    }

    public boolean updateSingleDevice(String deviceName, int delay, Optional<String> connectionId, Context context) {
        Optional<RoomDeviceList> result = getPartialRemoteDeviceUpdate(deviceName, delay, connectionId, context);
        LOG.info("updateSingleDevice({}) - remote device list update finished", deviceName);
        return update(context, result);
    }

    public boolean updateRoom(String roomName, Optional<String> connectionId, Context context) {
        Optional<RoomDeviceList> result = getPartialRemoteDeviceUpdate("room=" + roomName, 0, connectionId, context);
        LOG.info("updateRoom({}) - remote device list update finished", roomName);
        return update(context, result);
    }

    private boolean update(Context context, Optional<RoomDeviceList> result) {
        boolean success = false;
        if (result.isPresent()) {
            success = roomListHolderService.storeDeviceListMap(result.get(), context);
            if (success) LOG.info("update - update was successful, sending result");
        } else {
            LOG.info("update - update was not successful, sending empty device list");
        }
        return success;
    }

    public boolean updateAllDevices(Optional<String> connectionId, Context context) {
        Optional<RoomDeviceList> result = getRemoteRoomDeviceListMap(context, connectionId);
        LOG.info("updateAllDevices() - remote device list update finished");
        return update(context, result);
    }

    private Optional<RoomDeviceList> getPartialRemoteDeviceUpdate(String devSpec, int delay, Optional<String> connectionId, Context context) {
        LOG.info("getPartialRemoteDeviceUpdate(devSpec={}) - fetching xmllist from remote", devSpec);
        try {
            String result = commandExecutionService.executeSafely("xmllist " + devSpec, delay, connectionId, context);
            if (result == null) return absent();
            Optional<RoomDeviceList> parsed = Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, context));
            RoomDeviceList cached = roomListHolderService.getCachedRoomDeviceListMap(context);
            if (parsed.isPresent()) {
                cached.addAllDevicesOf(parsed.get(), context);
            }
            return Optional.of(cached);
        } catch (CommandExecutionException e) {
            LOG.info("getPartialRemoteDeviceUpdate - error during command execution", e);
            return Optional.absent();
        }
    }

    private synchronized Optional<RoomDeviceList> getRemoteRoomDeviceListMap(Context context, Optional<String> connectionId) {
        LOG.info("getRemoteRoomDeviceListMap - fetching device list from remote");

        try {
            String result = commandExecutionService.executeSafely("xmllist", connectionId, context);
            if (result == null) return absent();
            return Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, context));
        } catch (CommandExecutionException e) {
            LOG.info("getRemoteRoomDeviceListMap - error during command execution", e);
            return Optional.absent();
        }
    }
}
