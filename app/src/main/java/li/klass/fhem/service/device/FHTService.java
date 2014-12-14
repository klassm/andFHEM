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

import android.util.Log;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.service.CommandExecutionService;

/**
 * Class accumulating operations for FHT devices.
 */
@Singleton
public class FHTService {

    @Inject
    CommandExecutionService commandExecutionService;

    /**
     * Sets the mode attribute of a given FHT device. The action will only be executed if the new mode is different to
     * the already set one.
     *
     * @param device             concerned device
     * @param mode               new mode to set.
     * @param desiredTemperature temperature to set (only holiday and holiday_short
     * @param holiday1           holiday attribute one (only holiday and holiday_short
     * @param holiday2           holiday attribute two (only holiday and holiday_short
     */
    public void setMode(FHTDevice device, FHTMode mode, double desiredTemperature, int holiday1, int holiday2) {
        if (mode != FHTMode.UNKNOWN && device.getHeatingMode() != mode) {
            Log.e(FHTService.class.getName(), "changing mode for device " + device.getName() +
                    " from " + device.getHeatingMode() + " to " + mode);

            String command = "set " + device.getName() + " " + device.getHeatingModeCommandField() + " " + mode.name().toLowerCase(Locale.getDefault());

            if (mode == FHTMode.HOLIDAY || mode == FHTMode.HOLIDAY_SHORT) {
                command += " holiday1 " + holiday1 + " holiday2 " + holiday2 + " desired-temp " + desiredTemperature;
            }

            commandExecutionService.executeSafely(command);
            device.setHeatingMode(mode);
        }
    }

    /**
     * Sets the day temperature. The action will only be executed if the new day temperature is different to
     * the already set one.
     *
     * @param device         concerned device
     * @param dayTemperature new day temperature to set
     */
    public void setDayTemperature(FHTDevice device, double dayTemperature) {
        if (device.getDayTemperature() != dayTemperature) {
            String command = "set " + device.getName() + " day-temp " + dayTemperature;
            commandExecutionService.executeSafely(command);
            device.setDayTemperature(dayTemperature);
        }
    }

    /**
     * Sets the night temperature. The action will only be executed if the new night temperature is different to
     * the already set one.
     *
     * @param device           concerned device
     * @param nightTemperature new night temperature to set
     */
    public void setNightTemperature(FHTDevice device, double nightTemperature) {
        if (device.getNightTemperature() != nightTemperature) {
            String command = "set " + device.getName() + " night-temp " + nightTemperature;
            commandExecutionService.executeSafely(command);
            device.setNightTemperature(nightTemperature);
        }
    }

    public void refreshValues(FHTDevice device) {
        commandExecutionService.executeSafely("set " + device.getName() + " refreshvalues");
    }
}
