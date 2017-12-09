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

package li.klass.fhem.domain.heating.schedule.configuration

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists.newArrayList
import li.klass.fhem.domain.heating.schedule.DayProfile
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval
import li.klass.fhem.util.DayUtil
import li.klass.fhem.util.StateToSet
import java.util.*

class CULHMConfiguration : HeatingConfiguration<FilledTemperatureInterval, CULHMConfiguration>(
        "",
        MAXIMUM_NUMBER_OF_HEATING_INTERVALS,
        HeatingConfiguration.NumberOfIntervalsType.DYNAMIC,
        10
) {

    override fun readNode(weekProfile: WeekProfile<FilledTemperatureInterval, CULHMConfiguration>, key: String, value: String) {
        if (!key.matches("(R_(P1_)?[0-9]_)?tempList([a-zA-Z]{3})".toRegex())) return

        val shortName = key.replace("(R_(P1_)?[0-9]_)?tempList".toRegex(), "")
        val day = DayUtil.getDayForShortName(shortName) ?: return

        val parts = value.replace("set[_]?[ ]?".toRegex(), "")
                .trim { it <= ' ' }
                .split(" ".toRegex())
                .dropLastWhile { it.isEmpty() }

        parts.forEachIndexed { i, part ->
            val index = i / 2

            val interval = getOrCreateInterval(weekProfile, day, index)
            if (i % 2 == 0) { // time desc
                interval.switchTime = part
                if (part == "24:00") {
                    interval.isTimeFixed = true
                }
            } else { //temperature desc
                interval.temperature = java.lang.Double.valueOf(part)!!
            }
        }
    }

    public override fun generateStateToSetFor(dayProfile: DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>>): List<StateToSet> {

        val heatingIntervals = newArrayList(dayProfile.getHeatingIntervals())
        Collections.sort(heatingIntervals)

        val command = heatingIntervals
                .map { "${it.changedSwitchTime} ${it.changedTemperature}" }
                .joinToString(separator = " ")

        val shortName = DayUtil.getShortNameFor(dayProfile.day)
        shortName ?: return emptyList()

        val shortNameToSet = (shortName[0].toUpperCase()) + shortName.substring(1)

        return ImmutableList.of(StateToSet("tempList" + shortNameToSet, command))
    }

    override fun afterXMLRead(weekProfile: WeekProfile<FilledTemperatureInterval, CULHMConfiguration>) {
        super.afterXMLRead(weekProfile)
        weekProfile.sortedDayProfiles.forEach { addFixedMidnightIntervalIfRequired(it) }
    }

    /**
     * One interval always has to relate to midnight. For CUL_HM, this is always the last one,
     * representing the time _until_ midnight. The method adds this interval if not being already
     * present.
     *
     * @param dayProfile day profile to add the midnight to.
     */
    private fun addFixedMidnightIntervalIfRequired(
            dayProfile: DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>>) {

        val intervals = dayProfile.getHeatingIntervals()

        val hasMidnightSwitchTime = intervals.any { "24:00" == it.switchTime }

        if (!hasMidnightSwitchTime) {
            val heatingInterval = createHeatingInterval()

            heatingInterval.changedSwitchTime = "24:00"
            heatingInterval.changedTemperature = MINIMUM_TEMPERATURE
            heatingInterval.isTimeFixed = true

            dayProfile.addHeatingInterval(heatingInterval)
        }
    }

    override fun createHeatingInterval(): FilledTemperatureInterval = FilledTemperatureInterval()

    override fun getIntervalType(): IntervalType? = IntervalType.TO

    companion object {
        private val MAXIMUM_NUMBER_OF_HEATING_INTERVALS = 24
        private val MINIMUM_TEMPERATURE = 5.5
    }
}
