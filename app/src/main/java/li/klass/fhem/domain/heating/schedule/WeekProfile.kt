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

package li.klass.fhem.domain.heating.schedule

import android.content.Context
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.configuration.HeatingIntervalConfiguration
import li.klass.fhem.domain.heating.schedule.configuration.IntervalType
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval
import li.klass.fhem.util.DayUtil.Day
import li.klass.fhem.util.StateToSet
import java.io.Serializable
import java.util.*

class WeekProfile<INTERVAL : BaseHeatingInterval<INTERVAL>, C : HeatingConfiguration<INTERVAL, C>>(
        val configuration: C) : Serializable {
    private val dayProfiles =
            HashMap<Day, DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>>()

    val changedDayProfiles: List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>>
        get() {
            val changedDayProfiles =
                    ArrayList<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>>()

            for (dayProfile in dayProfiles.values) {
                if (dayProfile.isModified) {
                    changedDayProfiles.add(dayProfile)
                }
            }

            return changedDayProfiles
        }

    val sortedDayProfiles: List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>>
        get() {
            val result = ArrayList<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>>()

            for (day in Day.values()) {
                result.add(dayProfiles[day]!!)
            }

            return result
        }

    val statesToSet: List<StateToSet>
        get() = configuration.generatedStatesToSet(this)

    val intervalType: IntervalType
        get() = configuration.intervalType

    init {
        for (day in Day.values()) {
            dayProfiles[day] = createDayProfileFor(day)
        }
    }

    private fun createDayProfileFor(
            day: Day): DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> {
        return DayProfile(day, configuration as HeatingIntervalConfiguration<INTERVAL>)
    }

    fun getDayProfileFor(
            day: Day): DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> = dayProfiles[day]!!

    fun reset() {
        for (dayProfile in dayProfiles.values) {
            dayProfile.reset()
        }
    }

    /**
     * Format the given text. If null or time equals 24:00, return off
     *
     * @param time    time to check
     * @param context
     * @return formatted time
     */
    fun formatTimeForDisplay(time: String, context: Context): String {
        return configuration.formatTimeForDisplay(time, context)
    }

    fun formatTimeForCommand(time: String): String {
        return configuration.formatTimeForCommand(time)
    }

    fun replaceDayProfilesWithDataFrom(weekProfile: WeekProfile<INTERVAL, *>) {
        weekProfile.dayProfiles.forEach { (day, fromProfile) ->
            getDayProfileFor(day).replaceHeatingIntervalsWith(fromProfile.getHeatingIntervals())
        }
    }

    override fun toString(): String {
        return "WeekProfile{dayProfiles=$dayProfiles}"
    }
}
