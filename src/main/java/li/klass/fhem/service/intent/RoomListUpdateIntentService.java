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
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.service.room.RoomListUpdateService;

import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_FINISHED;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;

public class RoomListUpdateIntentService extends ConvenientIntentService {
    private static final Logger LOG = LoggerFactory.getLogger(RoomListUpdateIntentService.class);

    @Inject
    RoomListUpdateService roomListUpdateService;

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
        boolean success = deviceName.isPresent()
                ? roomListUpdateService.update(deviceName.get(), this)
                : roomListUpdateService.updateAllDevices(this);
        LOG.info("doRemoteUpdate() - remote device list update finished");
        startService(new Intent(REMOTE_UPDATE_FINISHED).putExtra(BundleExtraKeys.SUCCESS, success).setClass(this, RoomListIntentService.class));
        return STATE.DONE;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
