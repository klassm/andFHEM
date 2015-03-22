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

import java.util.Locale;

import javax.inject.Inject;

import li.klass.fhem.activities.CommandIndicatorActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.service.TextToSpeechService;
import li.klass.fhem.service.intent.voice.VoiceCommandService;
import li.klass.fhem.service.intent.voice.VoiceResult;
import li.klass.fhem.service.room.RoomListService;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_TARGET_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMES_TO_SEND;
import static li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired.REQUIRED;

public class VoiceCommandIntentService extends ConvenientIntentService {

    @Inject
    RoomListService roomListService;

    @Inject
    VoiceCommandService voiceCommandService;

    @Inject
    LicenseIntentService licenseIntentService;

    public VoiceCommandIntentService() {
        super(VoiceCommandIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(final Intent intent, long updatePeriod, final ResultReceiver resultReceiver) {
        final String action = intent.getAction();

        if (roomListService.updateRoomDeviceListIfRequired(intent, updatePeriod, this) == REQUIRED) {
            return STATE.DONE;
        }

        LicenseIntentService.IsPremiumListener listener = new LicenseIntentService.IsPremiumListener() {
            @Override
            public void isPremium(boolean isPremium) {
                if (!isPremium) return;

                if (action.equalsIgnoreCase(Actions.RECOGNIZE_VOICE_COMMAND)) {
                    String command = intent.getStringExtra(BundleExtraKeys.COMMAND);
                    if (handleCommand(command)) {
                        sendNoResult(resultReceiver, ResultCodes.SUCCESS);
                    } else {
                        sendNoResult(resultReceiver, ResultCodes.ERROR);
                    }
                }
            }
        };

        licenseIntentService.isPremium(listener);

        return STATE.DONE;
    }

    private boolean handleCommand(String command) {
        command = command.toLowerCase(Locale.getDefault());

        Optional<VoiceResult> result = voiceCommandService.resultFor(command, this);

        if (!result.isPresent()) {
            return false;
        }

        VoiceResult voiceResult = result.get();

        if (voiceResult instanceof VoiceResult.Success) {
            handleSuccess((VoiceResult.Success) voiceResult);
            return true;
        } else {
            handleError((VoiceResult.Error) voiceResult);
            return false;
        }
    }

    private void handleError(VoiceResult.Error voiceResult) {
        speak(getString(voiceResult.errorType.stringId));
    }

    private void speak(String text) {
        startService(new Intent(Actions.SAY).putExtra(BundleExtraKeys.TEXT, text).setClass(this, TextToSpeechService.class));
    }

    private void handleSuccess(VoiceResult.Success success) {

        startService(new Intent(Actions.DEVICE_SET_STATE)
                .setClass(this, DeviceIntentService.class)
                .putExtra(DEVICE_NAME, success.deviceName)
                .putExtra(TIMES_TO_SEND, 2)
                .putExtra(DEVICE_TARGET_STATE, success.targetState));

        startActivity(new Intent(VoiceCommandIntentService.this, CommandIndicatorActivity.class)
                .setFlags(FLAG_ACTIVITY_NEW_TASK));
    }
}
