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

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.update.backend.xmllist.DeviceNode;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.StateToSet;

import static com.google.common.collect.Lists.newArrayList;

public abstract class HeatingConfiguration<H extends BaseHeatingInterval<H>, C extends HeatingConfiguration<H, C>>
        implements Serializable, HeatingIntervalConfiguration<H> {


    private final int intervalMinutesMustBeDivisibleBy;

    public enum NumberOfIntervalsType {
        FIXED, DYNAMIC;

    }
    public final String offTime;
    public final int maximumNumberOfHeatingIntervals;

    public final NumberOfIntervalsType numberOfIntervalsType;
    private static final Logger LOG = LoggerFactory.getLogger(HeatingConfiguration.class);

    public HeatingConfiguration(String offTime, int maximumNumberOfHeatingIntervals, NumberOfIntervalsType numberOfIntervalsType, int intervalMinutesMustBeDivisibleBy) {
        this.offTime = offTime;
        this.maximumNumberOfHeatingIntervals = maximumNumberOfHeatingIntervals;
        this.numberOfIntervalsType = numberOfIntervalsType;
        this.intervalMinutesMustBeDivisibleBy = intervalMinutesMustBeDivisibleBy;
    }

    protected H getOrCreateInterval(WeekProfile<H, C> weekProfile, DayUtil.Day day, int index) {
        H interval = weekProfile.getDayProfileFor(day).getHeatingIntervalAt(index);
        if (interval == null) {
            interval = createHeatingInterval();
            weekProfile.getDayProfileFor(day).addHeatingInterval(interval);
        }

        return interval;
    }

    public WeekProfile<H, C> fillWith(XmlListDevice xmlListDevice) {
        WeekProfile<H, C> weekProfile = new WeekProfile<>((C) this);
        Map<String, DeviceNode> states = xmlListDevice.getStates();
        for (DeviceNode node : states.values()) {
            readNode(weekProfile, node.getKey(), node.getValue());
        }
        afterXMLRead(weekProfile);
        return weekProfile;
    }

    public abstract void readNode(WeekProfile<H, C> weekProfile, String key, String value);

    public List<String> generateScheduleCommands(String deviceName, WeekProfile<H, C> weekProfile) {
        List<StateToSet> statesToSet = generatedStatesToSet(weekProfile);
        List<String> result = newArrayList();
        for (StateToSet state : statesToSet) {
            result.add("set " + deviceName + " " + state.getKey() + " " + state.getValue());
        }
        LOG.info("generateScheduleCommands - resultingCommands: {}", result);
        return result;
    }

    public List<StateToSet> generatedStatesToSet(WeekProfile<H, C> weekProfile) {
        List<StateToSet> result = newArrayList();
        List<DayProfile<H, HeatingIntervalConfiguration<H>>> changedDayProfiles = weekProfile.getChangedDayProfiles();
        LOG.info("generateScheduleCommands - {} day(s) contain changes", changedDayProfiles.size());
        for (DayProfile<H, HeatingIntervalConfiguration<H>> dayProfile : changedDayProfiles) {
            result.addAll(generateStateToSetFor(dayProfile));
        }
        return result;
    }

    protected abstract List<StateToSet> generateStateToSetFor(DayProfile<H, HeatingIntervalConfiguration<H>> dayProfile);

    public String formatTimeForDisplay(String time, Context context) {
        return time;
    }

    public String formatTimeForCommand(String time) {
        return time;
    }

    public void afterXMLRead(WeekProfile<H, C> weekProfile) {
    }

    public int getIntervalMinutesMustBeDivisibleBy() {
        return intervalMinutesMustBeDivisibleBy;
    }

    public IntervalType getIntervalType() {
        return null;
    }

    @Override
    public int getMaximumNumberOfHeatingIntervals() {
        return maximumNumberOfHeatingIntervals;
    }

    @Override
    public NumberOfIntervalsType getNumberOfIntervalsType() {
        return numberOfIntervalsType;
    }
}