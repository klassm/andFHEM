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

    public boolean update(String deviceName, Context context) {
        Optional<RoomDeviceList> result = getRemoteDeviceUpdate(deviceName, context);
        LOG.info("update({}) - remote device list update finished", deviceName);
        boolean success = false;
        if (result.isPresent()) {
            success = roomListHolderService.storeDeviceListMap(result.get(), context);
            if (success) LOG.info("update({}) - update was successful, sending result", deviceName);
        } else {
            LOG.info("update({}) - update was not successful, sending empty device list", deviceName);
        }
        return success;
    }

    public boolean updateAllDevices(Context context) {
        Optional<RoomDeviceList> result = getRemoteRoomDeviceListMap(context);
        LOG.info("updateAllDevices() - remote device list update finished");
        boolean success = false;
        if (result.isPresent()) {
            success = roomListHolderService.storeDeviceListMap(result.get(), context);
            if (success) LOG.info("updateAllDevices() - update was successful, sending result");
        } else {
            LOG.info("updateAllDevices() - update was not successful, sending empty device list");
        }
        return success;
    }

    private Optional<RoomDeviceList> getRemoteDeviceUpdate(String deviceName, Context context) {
        LOG.info("getRemoteDeviceUpdate(deviceName=%s) - fetching xmllist from remote", deviceName);
        try {
            String result = commandExecutionService.executeSafely("xmllist " + deviceName, context);
            if (result == null) return absent();
            Optional<RoomDeviceList> parsed = Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, context));
            RoomDeviceList cached = roomListHolderService.getCachedRoomDeviceListMap();
            if (parsed.isPresent()) {
                cached.addDevice(parsed.get().getDeviceFor(deviceName), context);
            }
            return Optional.of(cached);
        } catch (CommandExecutionException e) {
            LOG.info("getRemoteDeviceUpdate - error during command execution", e);
            return Optional.absent();
        }
    }

    private synchronized Optional<RoomDeviceList> getRemoteRoomDeviceListMap(Context context) {
        LOG.info("getRemoteRoomDeviceListMap - fetching device list from remote");

        try {
            String result = commandExecutionService.executeSafely("xmllist", context);
            if (result == null) return absent();
            return Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, context));
        } catch (CommandExecutionException e) {
            LOG.info("getRemoteRoomDeviceListMap - error during command execution", e);
            return Optional.absent();
        }
    }
}
