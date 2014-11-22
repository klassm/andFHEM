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
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.intent.voice.VoiceCommandService;
import li.klass.fhem.service.intent.voice.VoiceResult;
import li.klass.fhem.service.room.RoomListService;

import static com.google.common.collect.FluentIterable.from;
import static li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired.REQUIRED;

public class VoiceCommandIntentService extends ConvenientIntentService {

    @Inject
    RoomListService roomListService;

    @Inject
    VoiceCommandService voiceCommandService;

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

        Optional<VoiceResult> result = voiceCommandService.resultFor(command);
        if (!result.isPresent() || !(result.get() instanceof VoiceResult.Success)) {
            return false;
        }

        VoiceResult.Success success = (VoiceResult.Success) result.get();

        startService(new Intent(Actions.DEVICE_SET_STATE)
                .setClass(this, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, success.deviceName)
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, success.targetState));

        return true;
    }


}
