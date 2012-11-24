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
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.DayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Class accumulating operations for FHT devices.
 */
public class FHTService {
    public static final FHTService INSTANCE = new FHTService();

    private FHTService() {
    }

    /**
     * Sets the desired temperature. The action will only be executed if the new desired temperature is different to
     * the already set one.
     * @param device concerned device
     * @param desiredTemperatureToSet new desired temperature value
     */
    public void setDesiredTemperature(FHTDevice device, double desiredTemperatureToSet) {
        String command = "set " + device.getName() + " desired-temp " + desiredTemperatureToSet;
        if (desiredTemperatureToSet != device.getDesiredTemp()) {
            CommandExecutionService.INSTANCE.executeSafely(command);
            device.setDesiredTemp(desiredTemperatureToSet);
        }
    }

    /**
     * Sets the mode attribute of a given FHT device. The action will only be executed if the new mode is different to
     * the already set one.
     * @param device concerned device
     * @param mode new mode to set.
     * @param desiredTemperature temperature to set (only holiday and holiday_short
     * @param holiday1 holiday attribute one (only holiday and holiday_short
     * @param holiday2 holiday attribute two (only holiday and holiday_short
     */
    public void setMode(FHTDevice device, FHTMode mode, double desiredTemperature, int holiday1, int holiday2) {
        if (mode != FHTMode.UNKNOWN && device.getMode() != mode) {
            Log.e(FHTService.class.getName(), "changing mode for device " + device.getName() +
                    " from " + device.getMode() + " to " + mode);

            String command = "set " + device.getName() + " mode " + mode.name().toLowerCase();

            if (mode == FHTMode.HOLIDAY || mode == FHTMode.HOLIDAY_SHORT) {
                command += " holiday1 " + holiday1 + " holiday2 " + holiday2 + " desired-temp " + desiredTemperature;
            }

            CommandExecutionService.INSTANCE.executeSafely(command);
            device.setMode(mode);
        }
    }

    /**
     * Sets the day temperature. The action will only be executed if the new day temperature is different to
     * the already set one.
     * @param device concerned device
     * @param dayTemperature new day temperature to set
     */
    public void setDayTemperature(FHTDevice device, double dayTemperature) {
        if (device.getDayTemperature() != dayTemperature) {
            String command = "set " + device.getName() + " day-temp " + dayTemperature;
            CommandExecutionService.INSTANCE.executeSafely(command);
            device.setDayTemperature(dayTemperature);
        }
    }

    /**
     * Sets the night temperature. The action will only be executed if the new night temperature is different to
     * the already set one.
     * @param device concerned device
     * @param nightTemperature new night temperature to set
     */
    public void setNightTemperature(FHTDevice device, double nightTemperature) {
        if (device.getNightTemperature() != nightTemperature) {
            String command = "set " + device.getName() + " night-temp " + nightTemperature;
            CommandExecutionService.INSTANCE.executeSafely(command);
            device.setNightTemperature(nightTemperature);
        }
    }

    /**
     * Sets the window open temperature. The action will only be executed if the new window open temperature is
     * different to the already set one.
     * @param device concerned device
     * @param windowOpenTemp new window open temperature to set
     */
    public void setWindowOpenTemp(FHTDevice device, double windowOpenTemp) {
        if (device.getWindowOpenTemp() != windowOpenTemp) {
            String command = "set " + device.getName() + " windowopen-temp " + windowOpenTemp;
            CommandExecutionService.INSTANCE.executeSafely(command);
            device.setWindowOpenTemp(windowOpenTemp);
        }
    }

    /**
     * Sets a new timetable for a given device. The action will only be executed if the new timetable is
     * different to the already set one.
     * @param device concerned device
     */
    public void setTimetableFor(FHTDevice device) {
        if (! device.hasChangedDayControlMapValues()) {
            return;
        }

        List<String> changeParts = new ArrayList<String>();
        for (FHTDayControl fhtDayControl : device.getDayControlMap().values()) {
            String shortDayName = DayUtil.getShortNameForStringId(fhtDayControl.getDayId());

            if (! fhtDayControl.getFrom1().equals(fhtDayControl.getFrom1Changed())) {
                changeParts.add(shortDayName + "-from1 " + fhtDayControl.getFrom1Changed());
            }
            if (! fhtDayControl.getFrom2().equals(fhtDayControl.getFrom2Changed())) {
                changeParts.add(shortDayName + "-from2 " + fhtDayControl.getFrom2Changed());
            }
            if (! fhtDayControl.getTo1().equals(fhtDayControl.getTo1Changed())) {
                changeParts.add(shortDayName + "-to1 " + fhtDayControl.getTo1Changed());
            }
            if (! fhtDayControl.getTo2().equals(fhtDayControl.getTo2Changed())) {
                changeParts.add(shortDayName + "-to2 " + fhtDayControl.getTo2Changed());
            }
        }

        for (String command : generateTimetableCommands(device, changeParts)) {
            CommandExecutionService.INSTANCE.executeSafely(command);
        }
        device.setChangedDayControlMapValuesAsCurrent();
    }

    /**
     * Generates the actual timetable commands from some given command parts. A FHT command may contain up to 8 command
     * parts. As FHT command evaluation is lazy, this should be used excessively.
     * @param device concerned device
     * @param commandParts some parts of the future commands. A part is like "mon-from1 08:00".
     * @return list of FHT commands like "set device_name mon-from1 08:00 mon-from2 17:00"
     */
    public List<String> generateTimetableCommands(FHTDevice device, List<String> commandParts) {
        List<String> commands = new ArrayList<String>();
        StringBuilder currentCommand = new StringBuilder();
        int currentCommandSize = 0;

        for (String commandPart : commandParts) {
            if (currentCommandSize >= 8) {
                commands.add("set " + device.getName() + " " + currentCommand.toString());
                currentCommand = new StringBuilder();
                currentCommandSize = 0;
            }
            currentCommand.append(commandPart).append(" ");

            currentCommandSize++;
        }

        if (currentCommand.length() > 0) {
            commands.add("set " + device.getName() + " " + currentCommand.toString());
        }

        return commands;
    }

    /**
     * Reset the changed timetable values to defaults
     * @param device device to change
     */
    public void resetTimetable(FHTDevice device) {
        device.resetDayControlMapValues();
    }

    public void refreshValues(FHTDevice device) {
        CommandExecutionService.INSTANCE.executeSafely("set " + device.getName() + " refreshvalues");
    }
}
