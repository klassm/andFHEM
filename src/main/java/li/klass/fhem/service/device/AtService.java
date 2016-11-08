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

package li.klass.fhem.service.device;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.AtDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.room.RoomListService;

@Singleton
public class AtService {
    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    RoomListService roomListService;
    @Inject
    GenericDeviceService genericDeviceService;

    private static final Logger LOG = LoggerFactory.getLogger(AtService.class);

    @Inject
    public AtService() {
    }

    public void createNew(final String timerName, int hour, int minute, int second, String repetition, String type,
                          String targetDeviceName, String targetState, String targetStateAppendix, final boolean isActive,
                          final Context context) {
        AtDevice device = new AtDevice();

        setValues(hour, minute, second, repetition, type, targetDeviceName, targetState, targetStateAppendix, device);

        String definition = device.toFHEMDefinition();
        String command = "define " + timerName + " at " + definition;
        commandExecutionService.executeSafely(command, Optional.absent(), context, new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {
                handleDisabled(timerName, isActive, context);

                context.sendBroadcast(new Intent(Actions.DO_UPDATE)
                        .putExtra(BundleExtraKeys.DO_REFRESH, true));
            }
        });
    }

    private void setValues(int hour, int minute, int second, String repetition, String type, String targetDeviceName, String targetState, String targetStateAppendix, AtDevice device) {
        device.setHour(hour);
        device.setMinute(minute);
        device.setSecond(second);
        device.setRepetition(AtDevice.AtRepetition.valueOf(repetition));
        device.setTimerType(AtDevice.TimerType.valueOf(type));
        device.setTargetDevice(targetDeviceName);
        device.setTargetState(targetState);
        device.setTargetStateAddtionalInformation(targetStateAppendix);
    }

    public void modify(final String timerName, int hour, int minute, int second, String repetition, String type,
                       String targetDeviceName, String targetState, String targetStateAppendix, final boolean isActive, final Context context) {
        Optional<AtDevice> deviceOptional = roomListService.getDeviceForName(timerName, Optional.absent(), context);

        if (!deviceOptional.isPresent()) {
            LOG.info("cannot find device for {}", timerName);
            return;
        }

        final AtDevice device = deviceOptional.get();
        setValues(hour, minute, second, repetition, type, targetDeviceName, targetState, targetStateAppendix, device);
        String definition = device.toFHEMDefinition();
        String command = "modify " + timerName + " " + definition;

        commandExecutionService.executeSafely(command, Optional.absent(), context, new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {
                handleDisabled(timerName, isActive, context);
                genericDeviceService.update(device, context);
            }
        });

    }

    private String handleDisabled(String timerName, boolean isActive, Context context) {
        return commandExecutionService.executeSync(String.format("attr %s %s %s", timerName, "disable", isActive ? "0" : "1"), Optional.absent(), context);
    }
}
