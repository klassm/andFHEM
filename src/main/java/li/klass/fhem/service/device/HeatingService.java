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
import android.util.Log;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.heating.ComfortTempDevice;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.EcoTempDevice;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.WindowOpenTempDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.ArrayUtil;

@Singleton
public class HeatingService {

    public static final String TAG = HeatingService.class.getName();
    public static final Logger LOGGER = LoggerFactory.getLogger(HeatingService.class);
    private static final String SET_COMMAND = "set %s %s %s";

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    public HeatingService() {
    }

    /**
     * Sets the desired temperature. The action will only be executed if the new desired temperature is different to
     * the already set one.
     *
     * @param device                  concerned device
     * @param desiredTemperatureToSet new desired temperature value
     * @param context                 context
     */
    public void setDesiredTemperature(final DesiredTempDevice device, final double desiredTemperatureToSet, Context context) {
        String command = String.format(SET_COMMAND, device.getName(), device.getDesiredTempCommandFieldName(), desiredTemperatureToSet);
        if (desiredTemperatureToSet != device.getDesiredTemp()) {
            commandExecutionService.executeSafely(command, Optional.<String>absent(), context, new CommandExecutionService.SuccessfulResultListener() {
                @Override
                public void onResult(String result) {
                    device.setDesiredTemp(desiredTemperatureToSet);
                }
            });

        }
    }

    /**
     * Sets the mode attribute of a given HeatingDevice device. The action will only be executed if the new mode is different to
     * the already set one.
     *
     * @param device  concerned device
     * @param mode    new mode to set.
     * @param context context
     */
    @SuppressWarnings("unchecked")
    public <MODE extends Enum<MODE>, D extends HeatingDevice<MODE, ?, ?, ?>> void setMode(final D device, final MODE mode, Context context) {
        if (mode == device.getHeatingMode()) {
            Log.e(TAG, "won't change heating mode, as it is already set!");
            return;
        }

        if (ArrayUtil.contains(device.getIgnoredHeatingModes(), mode)) {
            Log.e(TAG, "won't send heating mode, as it is ignored: " + mode.name());
            device.setHeatingMode(mode);
            return;
        }

        Log.e(TAG, "changing mode for device " + device.getName() +
                " from " + device.getHeatingMode() + " to " + mode);

        String command = String.format(SET_COMMAND, device.getName(),
                device.getHeatingModeCommandField(),
                mode.name().toLowerCase(Locale.getDefault()));
        commandExecutionService.executeSafely(command, Optional.<String>absent(), context, new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {
                device.setHeatingMode(mode);
            }
        });

    }


    /**
     * Sets the window open temperature. The action will only be executed if the new window open temperature is
     * different to the already set one.
     *
     * @param device         concerned device
     * @param windowOpenTemp new window open temperature to set
     * @param context        context
     */
    public void setWindowOpenTemp(final WindowOpenTempDevice device, final double windowOpenTemp, Context context) {
        if (device.getWindowOpenTemp() == windowOpenTemp) {
            return;
        }

        Log.e(TAG, "set window open temp of device " + device.getName() + " to " + windowOpenTemp);
        String command = String.format(SET_COMMAND, device.getName(), device.getWindowOpenTempCommandFieldName(), windowOpenTemp);
        commandExecutionService.executeSafely(command, Optional.<String>absent(), context, new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {
                device.setWindowOpenTemp(windowOpenTemp);
            }
        });

    }

    public void setComfortTemperature(final ComfortTempDevice device, final double temperature, Context context) {
        if (device.getComfortTemp() == temperature) {
            return;
        }

        Log.e(TAG, "set comfort temp of device " + device.getName() + " to " + temperature);
        String command = String.format(SET_COMMAND, device.getName(), device.getComfortTempCommandFieldName(), temperature);
        commandExecutionService.executeSafely(command, Optional.<String>absent(), context, new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {
                device.setComfortTemp(temperature);
            }
        });

    }

    public void setEcoTemperature(final EcoTempDevice device, final double temperature, Context context) {
        if (device.getEcoTemp() == temperature) {
            return;
        }

        Log.i(TAG, "set eco temp of device " + device.getName() + " to " + temperature);
        String command = String.format(SET_COMMAND, device.getName(), device.getEcoTempCommandFieldName(), temperature);
        commandExecutionService.executeSafely(command, Optional.<String>absent(), context, new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {
                device.setEcoTemp(temperature);
            }
        });

    }

    @SuppressWarnings("unchecked")
    public void setWeekProfileFor(HeatingDevice device, Context context) {
        WeekProfile weekProfile = device.getWeekProfile();
        List<String> commands = weekProfile.getSubmitCommands(device.getName());
        LOGGER.info("setWeekProfileFor - generated {}", commands);

        for (String command : commands) {
            commandExecutionService.executeSync(command, Optional.<String>absent(), context);
        }

        weekProfile.acceptChanges();
    }

    public void resetWeekProfile(HeatingDevice device) {
        device.getWeekProfile().reset();
    }
}
