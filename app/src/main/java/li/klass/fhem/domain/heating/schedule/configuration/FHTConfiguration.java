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

import java.util.List;
import java.util.Locale;

import li.klass.fhem.R;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.Reject;
import li.klass.fhem.util.StateToSet;

import static com.google.common.collect.Lists.newArrayList;

public class FHTConfiguration extends HeatingConfiguration<FromToHeatingInterval, FHTConfiguration> {
    public static final String OFF_TIME = "24:00";

    public FHTConfiguration() {
        super(OFF_TIME, 2, NumberOfIntervalsType.FIXED, 10);
    }

    @Override
    public void readNode(WeekProfile<FromToHeatingInterval, FHTConfiguration> weekProfile, String key, String value) {
        key = key.toUpperCase(Locale.getDefault());
        if (!key.endsWith("FROM1") && !key.endsWith("FROM2") && !key.endsWith("TO1") && !key.endsWith("TO2")) {
            return;
        }

        String shortName = key.substring(0, 3);
        DayUtil.Day day = DayUtil.getDayForShortName(shortName);

        if (day == null) return;

        DayProfile<FromToHeatingInterval, HeatingIntervalConfiguration<FromToHeatingInterval>> dayProfile = weekProfile.getDayProfileFor(day);
        Reject.ifNull(dayProfile);

        int intervalId = (key.charAt(key.length() - 1) - '0') - 1;
        FromToHeatingInterval interval = dayProfile.getHeatingIntervalAt(intervalId);

        if (key.contains("FROM")) {
            interval.setFromTime(value);
        } else {
            interval.setToTime(value);
        }
    }

    @Override
    protected List<StateToSet> generateStateToSetFor(DayProfile<FromToHeatingInterval, HeatingIntervalConfiguration<FromToHeatingInterval>> dayProfile) {
        DayUtil.Day day = dayProfile.getDay();
        String shortDayName = DayUtil.getShortNameFor(day);
        List<StateToSet> result = newArrayList();

        for (int i = 0; i < dayProfile.getNumberOfHeatingIntervals(); i++) {
            FromToHeatingInterval heatingInterval = dayProfile.getHeatingIntervalAt(i);

            if (heatingInterval.isModified()) {

                if (heatingInterval.isNew() || !heatingInterval.getFromTime().equals(heatingInterval.getChangedFromTime())) {
                    result.add(new StateToSet(shortDayName + "-from" + (i + 1), heatingInterval.getChangedFromTime()));
                }
                if (heatingInterval.isNew() || !heatingInterval.getToTime().equals(heatingInterval.getChangedToTime())) {
                    result.add(new StateToSet(shortDayName + "-to" + (i + 1), heatingInterval.getChangedToTime()));
                }
            }
        }
        return result;
    }

    @Override
    public FromToHeatingInterval createHeatingInterval() {
        return new FromToHeatingInterval(this);
    }

    @Override
    public String formatTimeForDisplay(String time, Context context) {
        String off = context.getResources().getString(R.string.off);
        return time.replaceAll("24:00", off);
    }

    @Override
    public String formatTimeForCommand(String time) {
        return time.replaceAll("00:00", "24:00");
    }
}
