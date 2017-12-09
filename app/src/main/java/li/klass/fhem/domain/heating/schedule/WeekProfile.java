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

package li.klass.fhem.domain.heating.schedule;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration;
import li.klass.fhem.domain.heating.schedule.configuration.HeatingIntervalConfiguration;
import li.klass.fhem.domain.heating.schedule.configuration.IntervalType;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.util.StateToSet;

import static li.klass.fhem.util.DayUtil.Day;

public class WeekProfile<INTERVAL extends BaseHeatingInterval<INTERVAL>, C extends HeatingConfiguration<INTERVAL, C>>
        implements Serializable {

    private final C configuration;
    private Map<Day, DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> dayProfiles = new HashMap<>();

    public WeekProfile(C configuration) {
        this.configuration = configuration;
        for (Day day : Day.values()) {
            dayProfiles.put(day, createDayProfileFor(day));
        }
    }

    private DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> createDayProfileFor(Day day) {
        return new DayProfile<>(day, (HeatingIntervalConfiguration<INTERVAL>) configuration);
    }

    public List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> getChangedDayProfiles() {
        List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> changedDayProfiles = new ArrayList<>();

        for (DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> dayProfile : dayProfiles.values()) {
            if (dayProfile.isModified()) {
                changedDayProfiles.add(dayProfile);
            }
        }

        return changedDayProfiles;
    }

    public List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> getSortedDayProfiles() {
        List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> result = new ArrayList<>();

        for (Day day : Day.values()) {
            result.add(dayProfiles.get(day));
        }

        return result;
    }

    public DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> getDayProfileFor(Day day) {
        return dayProfiles.get(day);
    }

    public List<String> getSubmitCommands(String deviceName) {
        return configuration.generateScheduleCommands(deviceName, this);
    }

    public List<StateToSet> getStatesToSet() {
        return configuration.generatedStatesToSet(this);
    }

    public void acceptChanges() {
        for (DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> dayProfile : dayProfiles.values()) {
            dayProfile.acceptChanges();
        }
    }

    public void reset() {
        for (DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> dayProfile : dayProfiles.values()) {
            dayProfile.reset();
        }
    }

    /**
     * Format the given text. If null or time equals 24:00, return off
     *
     * @param time time to check
     * @param context
     * @return formatted time
     */
    public String formatTimeForDisplay(String time, Context context) {
        return configuration.formatTimeForDisplay(time, context);
    }

    public String formatTimeForCommand(String time) {
        return configuration.formatTimeForCommand(time);
    }

    public IntervalType getIntervalType() {
        return configuration.getIntervalType();
    }

    public C getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return "WeekProfile{" +
                "dayProfiles=" + dayProfiles +
                '}';
    }
}
