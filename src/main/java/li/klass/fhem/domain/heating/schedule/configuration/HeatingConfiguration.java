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

import java.io.Serializable;
import java.util.List;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.util.DayUtil;

import static li.klass.fhem.util.DayUtil.Day;

public abstract class HeatingConfiguration<H extends BaseHeatingInterval, D extends Device<D>, C extends HeatingConfiguration<H, D, C>>
        implements Serializable {

    public enum NumberOfIntervalsType {
        FIXED, DYNAMIC
    }

    public final String offTime;
    public final int maximumNumberOfHeatingIntervals;
    public final NumberOfIntervalsType numberOfIntervalsType;

    public HeatingConfiguration(String offTime, int maximumNumberOfHeatingIntervals, NumberOfIntervalsType numberOfIntervalsType) {
        this.offTime = offTime;
        this.maximumNumberOfHeatingIntervals = maximumNumberOfHeatingIntervals;
        this.numberOfIntervalsType = numberOfIntervalsType;
    }

    protected H getOrCreateInterval(WeekProfile<H, C, D> weekProfile, DayUtil.Day day, int index) {
        H interval = weekProfile.getDayProfileFor(day).getHeatingIntervalAt(index);
        if (interval == null) {
            interval = createHeatingInterval();
            weekProfile.getDayProfileFor(day).addHeatingInterval(interval);
        }

        return interval;
    }


    public abstract void readNode(WeekProfile<H, C, D> weekProfile, String key, String value);

    public abstract H createHeatingInterval();

    public abstract DayProfile<H, D, C> createDayProfileFor(Day day, C configuration);

    public abstract List<String> generateScheduleCommands(D device, WeekProfile<H, C, D> weekProfile);

    public String getOffTime() {
        return null;
    }

    public void afterXMLRead(WeekProfile<H, C, D> weekProfile) {
    }

    public IntervalType getIntervalType() {
        return null;
    }
}