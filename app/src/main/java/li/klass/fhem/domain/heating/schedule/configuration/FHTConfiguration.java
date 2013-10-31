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

package li.klass.fhem.domain.heating.schedule.configuration;

import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.Reject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FHTConfiguration extends HeatingConfiguration<FromToHeatingInterval, FHTDevice, FHTConfiguration> {
    public static final String OFF_TIME = "00:00";

    public FHTConfiguration() {
        super(OFF_TIME, 2, NumberOfIntervalsType.FIXED);
    }

    @Override
    public void readNode(WeekProfile<FromToHeatingInterval, FHTConfiguration, FHTDevice> weekProfile, String key, String value) {
        if (!key.endsWith("FROM1") && !key.endsWith("FROM2") && !key.endsWith("TO1") && !key.endsWith("TO2")) {
            return;
        }

        String shortName = key.substring(0, 3);
        DayUtil.Day day = DayUtil.getDayForShortName(shortName);

        if (day == null) return;

        DayProfile<FromToHeatingInterval, FHTDevice, FHTConfiguration> dayProfile = weekProfile.getDayProfileFor(day);
        Reject.ifNull(dayProfile);

        int intervalId = (key.charAt(key.length() - 1) - '0') - 1;
        FromToHeatingInterval interval = dayProfile.getHeatingIntervalAt(intervalId);

        if (value.equals("24:00")) {
            value = "00:00";
        }

        if (key.contains("FROM")) {
            interval.setFromTime(value);
        } else {
            interval.setToTime(value);
        }
    }

    public List<String> generateScheduleCommands(FHTDevice device,
                                                 WeekProfile<FromToHeatingInterval, FHTConfiguration, FHTDevice> weekProfile) {
        List<DayProfile<FromToHeatingInterval, FHTDevice, FHTConfiguration>> changedDayProfiles = weekProfile.getChangedDayProfiles();
        if (changedDayProfiles.size() == 0) return Collections.emptyList();

        List<String> commandParts = generateCommandParts(changedDayProfiles);
        return generateCommands(device, commandParts);
    }

    protected List<String> generateCommandParts(List<DayProfile<FromToHeatingInterval, FHTDevice, FHTConfiguration>> changedDayProfiles) {
        List<String> commandParts = new ArrayList<String>();

        for (DayProfile<FromToHeatingInterval, FHTDevice, FHTConfiguration> dayProfile : changedDayProfiles) {
            DayUtil.Day day = dayProfile.getDay();
            String shortDayName = DayUtil.getShortNameFor(day);

            for (int i = 0; i < dayProfile.getNumberOfHeatingIntervals(); i++) {
                FromToHeatingInterval heatingInterval = dayProfile.getHeatingIntervalAt(i);

                if (heatingInterval.isModified()) {

                    if (!heatingInterval.getFromTime().equals(heatingInterval.getChangedFromTime())) {
                        commandParts.add(shortDayName + "-from" + (i + 1) + " " + heatingInterval.getChangedFromTime());
                    }
                    if (!heatingInterval.getToTime().equals(heatingInterval.getChangedToTime())) {
                        commandParts.add(shortDayName + "-to" + (i + 1) + " " + heatingInterval.getChangedToTime());
                    }
                }
            }
        }

        return commandParts;
    }

    protected List<String> generateCommands(FHTDevice device, List<String> commandParts) {
        List<String> commands = new ArrayList<String>();
        StringBuilder currentCommand = new StringBuilder();
        int currentCommandSize = 0;

        for (String commandPart : commandParts) {
            if (currentCommandSize >= 8) {
                commands.add("set " + device.getName() + " " + currentCommand.toString().trim());
                currentCommand = new StringBuilder();
                currentCommandSize = 0;
            }
            currentCommand.append(commandPart).append(" ");

            currentCommandSize++;
        }

        if (currentCommand.length() > 0) {
            commands.add("set " + device.getName() + " " + currentCommand.toString().trim());
        }

        return commands;
    }

    @Override
    public FromToHeatingInterval createHeatingInterval() {
        return new FromToHeatingInterval(this);
    }

    @Override
    public DayProfile<FromToHeatingInterval, FHTDevice, FHTConfiguration> createDayProfileFor(DayUtil.Day day, FHTConfiguration configuration) {
        return new DayProfile<FromToHeatingInterval, FHTDevice, FHTConfiguration>(day, configuration);
    }

    @Override
    public String getOffTime() {
        return OFF_TIME;
    }
}
