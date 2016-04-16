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

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.exception.CommandExecutionException;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.service.room.RoomListHolderService;

import static com.google.common.base.Optional.absent;
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_FINISHED;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;

public class RoomListUpdateIntentService extends ConvenientIntentService {
    private static final Logger LOG = LoggerFactory.getLogger(RoomListUpdateIntentService.class);

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    DeviceListParser deviceListParser;

    @Inject
    RoomListHolderService roomListHolderService;

    public RoomListUpdateIntentService() {
        super(RoomListUpdateIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        if (action.equals(Actions.DO_REMOTE_UPDATE)) {
            return doRemoteUpdate(Optional.fromNullable(intent.getStringExtra(DEVICE_NAME)));
        } else {
            return STATE.DONE;
        }
    }

    private STATE doRemoteUpdate(Optional<String> deviceName) {
        LOG.info("doRemoteUpdate() - starting remote update");
        Optional<RoomDeviceList> result = deviceName.isPresent()
                ? getRemoteDeviceUpdate(deviceName.get())
                : getRemoteRoomDeviceListMap();
        LOG.info("doRemoteUpdate() - remote device list update finished");
        boolean success = false;
        if (result.isPresent()) {
            success = roomListHolderService.storeDeviceListMap(result.get(), this);
            if (success) LOG.info("doRemoteUpdate() - update was successful, sending result");
        } else {
            LOG.info("doRemoteUpdate() - update was not successful, sending empty device list");
        }
        startService(new Intent(REMOTE_UPDATE_FINISHED).putExtra(BundleExtraKeys.SUCCESS, success).setClass(this, RoomListIntentService.class));
        return STATE.DONE;
    }

    private Optional<RoomDeviceList> getRemoteDeviceUpdate(String deviceName) {
        LOG.info("getRemoteDeviceUpdate(deviceName=%s) - fetching xmllist from remote", deviceName);
        try {
            String message = getResources().getString(R.string.updatingDeviceListForDevice, deviceName);
            getBaseContext().sendBroadcast(new Intent(Actions.SHOW_TOAST).putExtra(BundleExtraKeys.CONTENT, message));
            String result = commandExecutionService.executeSafely("xmllist " + deviceName, this);
            if (result == null) return absent();
            Optional<RoomDeviceList> parsed = Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, this));
            RoomDeviceList cached = roomListHolderService.getCachedRoomDeviceListMap();
            if (parsed.isPresent()) {
                cached.addDevice(parsed.get().getDeviceFor(deviceName), getBaseContext());
            }
            return Optional.of(cached);
        } catch (CommandExecutionException e) {
            LOG.info("getRemoteDeviceUpdate - error during command execution", e);
            return Optional.absent();
        }
    }

    private synchronized Optional<RoomDeviceList> getRemoteRoomDeviceListMap() {
        LOG.info("getRemoteRoomDeviceListMap - fetching device list from remote");
        String message = getResources().getString(R.string.updatingDeviceList);
        getBaseContext().sendBroadcast(new Intent(Actions.SHOW_TOAST).putExtra(BundleExtraKeys.CONTENT, message));

        try {
            String result = commandExecutionService.executeSafely("xmllist", this);
            if (result == null) return absent();
            return Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, this));
        } catch (CommandExecutionException e) {
            LOG.info("getRemoteRoomDeviceListMap - error during command execution", e);
            return Optional.absent();
        }
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
