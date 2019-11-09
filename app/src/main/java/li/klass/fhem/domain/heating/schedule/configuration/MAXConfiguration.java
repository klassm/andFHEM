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

import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.StateToSet;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

public class MAXConfiguration extends HeatingConfiguration<FilledTemperatureInterval, MAXConfiguration> {

    public static final int MAXIMUM_NUMBER_OF_HEATING_INTERVALS = 13;
    public static final Pattern WEEKPROFILE_KEY_PATTERN = Pattern.compile("weekprofile_[0-9]+_([^_]+)_(time|temp)");
    public static final double MINIMUM_TEMPERATURE = 4.5;

    public MAXConfiguration() {
        super("", MAXIMUM_NUMBER_OF_HEATING_INTERVALS, NumberOfIntervalsType.DYNAMIC, 5);
    }

    @Override
    public void readNode(WeekProfile<FilledTemperatureInterval, MAXConfiguration> weekProfile, String key, String value) {
        if (!key.startsWith("weekprofile") || (!key.endsWith("temp") && !key.endsWith("time"))) {
            return;
        }

        key = key.replaceAll("-", "_");

        // B0 is a pretty strange fix. Sometimes it seems to be places instead of a degree (°) sign
        value = value.replaceAll("[^0-9. -:/]", "").replaceAll("  ", " ");

        Matcher matcher = WEEKPROFILE_KEY_PATTERN.matcher(key);
        if (!matcher.find()) {
            return;
        }

        String dayShortName = matcher.group(1);
        DayUtil.Day day = DayUtil.getDayForShortName(dayShortName);

        if (day == null) return;

        if (key.endsWith("temp")) {
            parseTemp(day, value, weekProfile);
        } else if (key.endsWith("time")) {
            parseTime(day, value, weekProfile);
        }
    }

    private void parseTemp(DayUtil.Day day, String value, WeekProfile<FilledTemperatureInterval, MAXConfiguration> weekProfile) {
        String[] temperatures = value.split("/");
        for (int i = 0; i < temperatures.length; i++) {
            String temperatureValue = temperatures[i].trim();
            double temperature = extractLeadingDouble(temperatureValue);

            getOrCreateInterval(weekProfile, day, i).setTemperature(temperature);
        }
    }

    private void parseTime(DayUtil.Day day, String value, WeekProfile<FilledTemperatureInterval, MAXConfiguration> weekProfile) {
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
    protected List<StateToSet> generateStateToSetFor(DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>> dayProfile) {
        StringBuilder builder = new StringBuilder();

        List<FilledTemperatureInterval> heatingIntervals = new ArrayList<>(dayProfile.getHeatingIntervals());
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

        return ImmutableList.of(new StateToSet("weekProfile", shortName + " " + builder.toString()));
    }

    @Override
    public void afterXMLRead(WeekProfile<FilledTemperatureInterval, MAXConfiguration> weekProfile) {
        super.afterXMLRead(weekProfile);

        List<DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>>>
                profiles = weekProfile.getSortedDayProfiles();

        for (DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>> profile : profiles) {
            addMidnightIntervalIfNotAvailable(profile);
        }
    }

    private <D extends DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>>> void
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
            interval.setChangedTemperature(MINIMUM_TEMPERATURE);
            interval.setTimeFixed(true);

            profile.addHeatingInterval(interval);
        }
    }

    @Override
    @NotNull
    public IntervalType getIntervalType() {
        return IntervalType.FROM;
    }
}
