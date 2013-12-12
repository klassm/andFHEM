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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import li.klass.fhem.domain.MaxDevice;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.util.DayUtil;

public class MAXConfiguration extends HeatingConfiguration<FilledTemperatureInterval, MaxDevice, MAXConfiguration> {

    public static final int MAXIMUM_NUMBER_OF_HEATING_INTERVALS = 13;

    public MAXConfiguration() {
        super("", MAXIMUM_NUMBER_OF_HEATING_INTERVALS, NumberOfIntervalsType.DYNAMIC);
    }

    @Override
    public void readNode(WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile, String key, String value) {
        if (!key.startsWith("WEEKPROFILE") || (!key.endsWith("TEMP") && !key.endsWith("TIME"))) return;
        key = key.replaceAll("-", "_");

        // B0 is a pretty strange fix. Sometimes it seems to be places instead of a degree (°) sign
        value = value.replaceAll("[^0-9. -:/]", "").replaceAll("  ", " ");

        int lastDash = key.lastIndexOf("_");
        int dayDash = key.indexOf("_", "WEEKPROFILE_".length());

        if (lastDash == -1 || dayDash == -1) return;

        String dayShortName = key.substring(dayDash + 1, lastDash);
        DayUtil.Day day = DayUtil.getDayForShortName(dayShortName);

        if (day == null) return;

        if (key.endsWith("TEMP")) {
            parseTemp(day, value, weekProfile);
        } else if (key.endsWith("TIME")) {
            parseTime(day, value, weekProfile);
        }
    }

    private void parseTemp(DayUtil.Day day, String value, WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile) {
        String[] temperatures = value.split("/");
        for (int i = 0; i < temperatures.length; i++) {
            String temperatureValue = temperatures[i].trim();
            double temperature = Double.valueOf(temperatureValue);

            getOrCreateInterval(weekProfile, day, i).setTemperature(temperature);
        }
    }

    private void parseTime(DayUtil.Day day, String value, WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile) {
        String[] timeIntervals = value.split("/");
        for (int i = 0; i < timeIntervals.length; i++) {
            String switchTime = extractSwitchTime(timeIntervals[i]);
            boolean isTimeFixed = i == 0;

            FilledTemperatureInterval interval = getOrCreateInterval(weekProfile, day, i);
            interval.setSwitchTime(switchTime);
            interval.setTimeFixed(isTimeFixed);
        }
    }

    private String extractSwitchTime(String timeInterval) {
        timeInterval = timeInterval.trim();
        int firstDash = timeInterval.indexOf("-");
        return timeInterval.substring(0, firstDash);
    }

    @Override
    public FilledTemperatureInterval createHeatingInterval() {
        return new FilledTemperatureInterval();
    }

    @Override
    public DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration> createDayProfileFor(DayUtil.Day day, MAXConfiguration configuration) {
        return new DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration>(day, configuration);
    }

    @Override
    public List<String> generateScheduleCommands(MaxDevice device, WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile) {
        List<String> result = new ArrayList<String>();

        List<? extends DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration>> changedDayProfiles = weekProfile.getChangedDayProfiles();
        for (DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration> dayProfile : changedDayProfiles) {
            result.add(generateCommandFor(device, dayProfile));
        }

        return result;
    }

    protected <D extends DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration>>
    String generateCommandFor(MaxDevice device, D dayProfile) {

        StringBuilder builder = new StringBuilder();

        List<FilledTemperatureInterval> heatingIntervals = new ArrayList<FilledTemperatureInterval>(dayProfile.getHeatingIntervals());
        Collections.sort(heatingIntervals);

        for (int i = 0; i < heatingIntervals.size(); i++) {
            FilledTemperatureInterval interval = heatingIntervals.get(i);
            if (i == 0) {
                builder.append(interval.getChangedTemperature());
            } else {
                builder.append(",");
                builder.append(interval.getChangedSwitchTime());
                builder.append(",");
                builder.append(interval.getChangedTemperature());
            }
        }

        String shortName = DayUtil.getShortNameFor(dayProfile.getDay());
        char firstChar = shortName.charAt(0);
        shortName = ((char) (firstChar - 'a' + 'A')) + shortName.substring(1);

        return "set " + device.getName() + " weekProfile " + shortName + " " + builder.toString();
    }


    @Override
    public void afterXMLRead(WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile) {
        super.afterXMLRead(weekProfile);

        List<DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration>>
                profiles = weekProfile.getSortedDayProfiles();

        for (DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration> profile : profiles) {
            addMidnightIntervalIfNotAvailable(profile);
        }
    }

    private <D extends DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration>> void
    addMidnightIntervalIfNotAvailable(D profile) {

        boolean foundMidnightInterval = false;
        for (FilledTemperatureInterval interval : profile.getHeatingIntervals()) {
            if (interval.getChangedSwitchTime().equals("00:00")) {
                foundMidnightInterval = true;
                break;
            }
        }

        if (!foundMidnightInterval) {
            FilledTemperatureInterval interval = new FilledTemperatureInterval();
            interval.setChangedSwitchTime("00:00");
            interval.setChangedTemperature(MaxDevice.MINIMUM_TEMPERATURE);
            interval.setTimeFixed(true);

            profile.addHeatingInterval(interval);
        }
    }

    @Override
    public IntervalType getIntervalType() {
        return IntervalType.FROM;
    }
}
