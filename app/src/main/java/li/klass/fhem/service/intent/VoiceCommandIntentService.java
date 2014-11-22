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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.room.RoomListService;

import static li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired.REQUIRED;

public class VoiceCommandIntentService extends ConvenientIntentService {

    public static final String COMMAND_START = "schalte|switch|set";
    private Map<String, String> START_REPLACE = ImmutableMap.<String, String>builder()
            .put(COMMAND_START, "set").build();

    private Map<String, String> STATE_REPLACE = ImmutableMap.<String, String>builder()
            .put("an|[n]?ein|1", "on")
            .put("aus", "off").build();
    @Inject
    RoomListService roomListService;

    private static final Logger LOG = LoggerFactory.getLogger(VoiceCommandIntentService.class);

    public VoiceCommandIntentService() {
        super(VoiceCommandIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        if (roomListService.updateRoomDeviceListIfRequired(intent, updatePeriod) == REQUIRED) {
            return STATE.DONE;
        }

        if (action.equalsIgnoreCase(Actions.RECOGNIZE_VOICE_COMMAND)) {
            String command = intent.getStringExtra(BundleExtraKeys.COMMAND);
            if (handleCommand(command)) {
                return STATE.SUCCESS;
            } else {
                return STATE.ERROR;
            }
        }

        return STATE.DONE;
    }

    private boolean handleCommand(String command) {
        command = command.toLowerCase();

        String[] parts = command.split(" ");
        if (parts.length != 3) return false;

        String starter = replace(parts[0], START_REPLACE);
        if (!starter.equals("set")) return false;

        String deviceName = parts[1];
        String state = replace(parts[2], STATE_REPLACE);

        RoomDeviceList devices = roomListService.getAllRoomsDeviceList();
        for (Device device : devices.getAllDevices()) {
            String alias = device.getAlias();
            if ((!Strings.isNullOrEmpty(alias) && alias.equalsIgnoreCase(deviceName)
                    || device.getName().equalsIgnoreCase(deviceName)) && device.getSetList().contains(state)) {

                LOG.info("switch {} to {}", device.getName(), state);
                startService(new Intent(Actions.DEVICE_SET_STATE)
                        .setClass(this, DeviceIntentService.class)
                        .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                        .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, state));
                return true;
            }
        }
        return false;
    }

    private String replace(String in, Map<String, String> toReplace) {
        for (Map.Entry<String, String> entry : toReplace.entrySet()) {
            in = in.replaceAll(entry.getKey(), entry.getValue());
        }
        return in;
    }
}
