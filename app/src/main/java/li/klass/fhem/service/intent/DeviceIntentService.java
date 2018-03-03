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
import android.util.Log;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.devices.backend.DeviceService;
import li.klass.fhem.devices.backend.GenericDeviceService;
import li.klass.fhem.devices.backend.ToggleableService;
import li.klass.fhem.devices.backend.at.AtService;
import li.klass.fhem.devices.list.favorites.backend.FavoritesService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.graph.backend.GraphService;
import li.klass.fhem.service.NotificationService;
import li.klass.fhem.update.backend.DeviceListService;
import li.klass.fhem.update.backend.command.execution.Command;
import li.klass.fhem.update.backend.command.execution.CommandExecutionService;

import static li.klass.fhem.constants.Actions.DEVICE_DELETE;
import static li.klass.fhem.constants.Actions.DEVICE_MOVE_ROOM;
import static li.klass.fhem.constants.Actions.DEVICE_RENAME;
import static li.klass.fhem.constants.Actions.DEVICE_SET_ALIAS;
import static li.klass.fhem.constants.Actions.RESEND_LAST_FAILED_COMMAND;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NEW_ALIAS;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NEW_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NEW_ROOM;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;

public class DeviceIntentService extends ConvenientIntentService {

    @Inject
    DeviceListService deviceListService;
    @Inject
    DeviceService deviceService;
    @Inject
    FavoritesService favoritesService;
    @Inject
    GenericDeviceService genericDeviceService;
    @Inject
    CommandExecutionService commandExecutionService;
    @Inject
    AtService atService;
    @Inject
    ToggleableService toggleableService;
    @Inject
    OnOffBehavior onOffBehavior;
    @Inject
    GraphService graphService;
    @Inject
    NotificationService notificationService;

    private static final Logger LOG = LoggerFactory.getLogger(DeviceIntentService.class);

    public DeviceIntentService() {
        super(DeviceIntentService.class.getName());
    }

    @Override
    protected State handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {

        String deviceName = intent.getStringExtra(DEVICE_NAME);
        Optional<String> connectionId = Optional.fromNullable(intent.getStringExtra(CONNECTION_ID));

        FhemDevice device = deviceName == null ? null : deviceListService.getDeviceForName(deviceName, connectionId.orNull());
        if (device == null) {
            LOG.info("handleIntent() - cannot find device for {}", deviceName);
            return State.ERROR;
        }

        Log.d(DeviceIntentService.class.getName(), intent.getAction());
        String action = intent.getAction();

        if (DEVICE_RENAME.equals(action)) {
            String newName = intent.getStringExtra(DEVICE_NEW_NAME);
            deviceService.renameDevice(device, newName);
            notificationService.rename(deviceName, newName, this);
            favoritesService.removeFavorite(deviceName);
            favoritesService.addFavorite(newName);

        } else if (DEVICE_DELETE.equals(action)) {
            deviceService.deleteDevice(device);

        } else if (DEVICE_MOVE_ROOM.equals(action)) {
            String newRoom = intent.getStringExtra(DEVICE_NEW_ROOM);
            deviceService.moveDevice(device, newRoom, this);

        } else if (DEVICE_SET_ALIAS.equals(action)) {
            String newAlias = intent.getStringExtra(DEVICE_NEW_ALIAS);
            deviceService.setAlias(device, newAlias);
        } else if (RESEND_LAST_FAILED_COMMAND.equals(action)) {

            Command lastFailedCommand = commandExecutionService.getLastFailedCommand();
            if (lastFailedCommand != null && "xmllist".equalsIgnoreCase(lastFailedCommand.getCommand())) {
                sendBroadcast(new Intent(Actions.DO_UPDATE)
                        .putExtra(DO_REFRESH, true));
            } else {
                commandExecutionService.resendLastFailedCommand();
            }
        }

        return State.SUCCESS;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
