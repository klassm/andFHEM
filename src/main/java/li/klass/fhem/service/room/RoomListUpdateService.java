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
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.CommandExecutionService;

import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;

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

    public void updateSingleDevice(String deviceName, int delay, Optional<String> connectionId, Context context, RoomListUpdateListener roomListUpdateListener) {
        executeXmllistPartial(delay, connectionId, context, roomListUpdateListener, deviceName);
    }

    public void updateRoom(String roomName, Optional<String> connectionId, Context context, RoomListUpdateListener roomListUpdateListener) {
        executeXmllistPartial(0, connectionId, context, roomListUpdateListener, "room=" + roomName);
    }

    public void updateAllDevices(Optional<String> connectionId, Context context, RoomListUpdateListener roomListUpdateListener) {
        executeXmllist(0, connectionId, context, roomListUpdateListener, "", new UpdateHandler() {
            @Override
            public RoomDeviceList handle(RoomDeviceList cached, RoomDeviceList parsed) {
                return parsed;
            }
        });
    }

    private boolean update(Context context, Optional<String> connectionId, Optional<RoomDeviceList> result) {
        boolean success = false;
        if (result.isPresent()) {
            success = roomListHolderService.storeDeviceListMap(result.get(), connectionId, context);
            if (success) LOG.info("update - update was successful, sending result");
        } else {
            LOG.info("update - update was not successful, sending empty device list");
        }
        return success;
    }

    private void executeXmllistPartial(int delay, Optional<String> connectionId, final Context context, RoomListUpdateListener updateListener, String devSpec) {
        LOG.info("executeXmllist(devSpec={}) - fetching xmllist from remote", devSpec);
        executeXmllist(delay, connectionId, context, updateListener, " " + devSpec, new UpdateHandler() {
            @Override
            public RoomDeviceList handle(RoomDeviceList cached, RoomDeviceList parsed) {
                cached.addAllDevicesOf(parsed, context);
                return cached;
            }
        });
    }

    private void executeXmllist(final int delay, final Optional<String> connectionId, final Context context, final RoomListUpdateListener updateListener, String xmllistSuffix, final UpdateHandler updateHandler) {
        commandExecutionService.executeSafely("xmllist" + xmllistSuffix, delay, connectionId, context, new CommandExecutionService.ResultListener() {
            @Override
            public void onResult(String result) {
                Optional<RoomDeviceList> roomDeviceList = parseResult(connectionId, context, result, updateHandler);
                boolean success = update(context, connectionId, roomDeviceList);
                updateListener.onUpdateFinished(success);
                if (delay > 0) {
                    context.sendBroadcast(new Intent(Actions.DO_UPDATE).putExtra(DO_REFRESH, false));
                }
            }

            @Override
            public void onError() {
                updateListener.onUpdateFinished(false);
            }
        });
    }

    @NonNull
    private Optional<RoomDeviceList> parseResult(Optional<String> connectionId, Context context, String result, UpdateHandler updateHandler) {
        Optional<RoomDeviceList> parsed = Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, context));
        Optional<RoomDeviceList> cached = roomListHolderService.getCachedRoomDeviceListMap(connectionId, context);
        if (parsed.isPresent()) {
            RoomDeviceList newDeviceList = updateHandler.handle(cached.or(parsed.get()), parsed.get());
            roomListHolderService.storeDeviceListMap(newDeviceList, connectionId, context);
            return Optional.of(newDeviceList);
        }
        return Optional.absent();
    }

    public interface RoomListUpdateListener {
        void onUpdateFinished(boolean result);
    }

    private interface UpdateHandler {
        RoomDeviceList handle(RoomDeviceList cached, RoomDeviceList parsed);
    }
}
