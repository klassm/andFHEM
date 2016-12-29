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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration;
import li.klass.fhem.domain.heating.schedule.configuration.IntervalType;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.util.StateToSet;

import static li.klass.fhem.util.DayUtil.Day;

public class WeekProfile<H extends BaseHeatingInterval, C extends HeatingConfiguration<H, D, C>, D extends FhemDevice<D>>
        implements Serializable {

    private final C configuration;
    private Map<Day, DayProfile<H, D, C>> dayProfiles = new HashMap<>();

    public WeekProfile(C configuration) {
        for (Day day : Day.values()) {
            dayProfiles.put(day, configuration.createDayProfileFor(day, configuration));
        }

        this.configuration = configuration;
    }

    public List<DayProfile<H, D, C>> getChangedDayProfiles() {
        List<DayProfile<H, D, C>> changedDayProfiles = new ArrayList<>();

        for (DayProfile<H, D, C> dayProfile : dayProfiles.values()) {
            if (dayProfile.isModified()) {
                changedDayProfiles.add(dayProfile);
            }
        }

        return changedDayProfiles;
    }

    public List<DayProfile<H, D, C>> getSortedDayProfiles() {
        List<DayProfile<H, D, C>> result = new ArrayList<>();

        for (Day day : Day.values()) {
            result.add(dayProfiles.get(day));
        }

        return result;
    }

    public DayProfile<H, D, C> getDayProfileFor(Day day) {
        return dayProfiles.get(day);
    }

    public void fillWith(XmlListDevice xmlListDevice) {
        configuration.fillWith(this, xmlListDevice);
    }

    public void readNode(String key, String value) {
        configuration.readNode(this, key, value);
    }

    public List<String> getSubmitCommands(String deviceName) {
        return configuration.generateScheduleCommands(deviceName, this);
    }

    public List<StateToSet> getStatesToSet() {
        return configuration.generatedStatesToSet(this);
    }

    public void acceptChanges() {
        for (DayProfile<H, D, C> dayProfile : dayProfiles.values()) {
            dayProfile.acceptChanges();
        }
    }

    public void reset() {
        for (DayProfile<H, D, C> dayProfile : dayProfiles.values()) {
            dayProfile.reset();
        }
    }

    /**
     * Format the given text. If null or time equals 24:00, return off
     *
     * @param time time to check
     * @return formatted time
     */
    public String formatTimeForDisplay(String time) {
        return configuration.formatTimeForDisplay(time);
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
