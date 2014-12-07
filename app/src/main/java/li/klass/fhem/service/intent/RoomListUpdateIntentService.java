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

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.exception.CommandExecutionException;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.service.room.RoomListHolderService;
import li.klass.fhem.service.room.RoomListService;

import static com.google.common.base.Optional.absent;
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_FINISHED;

public class RoomListUpdateIntentService extends ConvenientIntentService {
    private static final Logger LOG = LoggerFactory.getLogger(RoomListUpdateIntentService.class);

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    DeviceListParser deviceListParser;

    @Inject
    RoomListService roomListService;

    @Inject
    RoomListHolderService roomListHolderService;

    public RoomListUpdateIntentService() {
        super(RoomListUpdateIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        if (action.equals(Actions.DO_REMOTE_UPDATE)) {
            return doRemoteUpdate();
        } else {
            return STATE.DONE;
        }
    }

    private STATE doRemoteUpdate() {
        LOG.info("doRemoteUpdate() - starting remote update");
        Optional<RoomDeviceList> result = getRemoteRoomDeviceListMap();
        LOG.info("doRemoteUpdate() - remote device list update finished");
        if (result.isPresent()) {
            roomListHolderService.storeDeviceListMap(result.get());
            startService(new Intent(REMOTE_UPDATE_FINISHED).setClass(this, RoomListIntentService.class));
            LOG.info("doRemoteUpdate() - update was successful, sending result");
            return STATE.DONE;
        } else {
            startService(new Intent(REMOTE_UPDATE_FINISHED).setClass(this, RoomListIntentService.class));
            LOG.info("doRemoteUpdate() - update was not successful, sending empty device list");
            return STATE.DONE;
        }
    }

    private synchronized Optional<RoomDeviceList> getRemoteRoomDeviceListMap() {
        LOG.info("getRemoteRoomDeviceListMap() - getRemoteRoomDeviceListMap() : fetching device list from remote");

        try {
            String result = commandExecutionService.executeSafely("xmllist");
            if (result == null) return absent();
            return Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result));
        } catch (CommandExecutionException e) {
            LOG.info("getRemoteRoomDeviceListMap() - error during command execution", e);
            return Optional.absent();
        }
    }
}
