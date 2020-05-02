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

import android.content.Context
import li.klass.fhem.domain.heating.schedule.DayProfile
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.DayUtil.Day
import li.klass.fhem.util.StateToSet
import org.slf4j.LoggerFactory
import java.io.Serializable

abstract class HeatingConfiguration<H : BaseHeatingInterval<H>, C : HeatingConfiguration<H, C>>(
        val offTime: String,
        private val maximumNumberOfHeatingIntervals: Int,
        private val numberOfIntervalsType: NumberOfIntervalsType,
        val intervalMinutesMustBeDivisibleBy: Int
) : Serializable, HeatingIntervalConfiguration<H> {

    enum class NumberOfIntervalsType {
        FIXED, DYNAMIC
    }

    protected fun getOrCreateInterval(weekProfile: WeekProfile<H, C>, day: Day?, index: Int): H {
        var interval = weekProfile.getDayProfileFor(day!!).getHeatingIntervalAt(index)
        if (interval == null) {
            interval = createHeatingInterval()
            weekProfile.getDayProfileFor(day).addHeatingInterval(interval)
        }
        return interval!!
    }

    fun fillWith(xmlListDevice: XmlListDevice): WeekProfile<H, C> {
        val weekProfile = WeekProfile(this as C)
        val states: Map<String, DeviceNode> = xmlListDevice.states
        for ((_, key, value) in states.values) {
            readNode(weekProfile, key, value)
        }
        afterXMLRead(weekProfile)
        return weekProfile
    }

    abstract fun readNode(weekProfile: WeekProfile<H, C>, key: String, value: String)
    fun generateScheduleCommands(deviceName: String, weekProfile: WeekProfile<H, C>): List<String> {
        val statesToSet = generatedStatesToSet(weekProfile)
        val result: MutableList<String> = mutableListOf()
        for (state in statesToSet) {
            result.add("set " + deviceName + " " + state.key + " " + state.value)
        }
        LOG.info("generateScheduleCommands - resultingCommands: {}", result)
        return result
    }

    fun generatedStatesToSet(weekProfile: WeekProfile<H, C>): List<StateToSet> {
        val result: MutableList<StateToSet> = mutableListOf()
        val changedDayProfiles: List<DayProfile<H, HeatingIntervalConfiguration<H>>> = weekProfile.changedDayProfiles
        LOG.info("generateScheduleCommands - {} day(s) contain changes", changedDayProfiles.size)
        for (dayProfile in changedDayProfiles) {
            result.addAll(generateStateToSetFor(dayProfile))
        }
        return result
    }

    protected abstract fun generateStateToSetFor(dayProfile: DayProfile<H, HeatingIntervalConfiguration<H>>): List<StateToSet>

    open fun formatTimeForDisplay(time: String, context: Context?): String = time

    open fun formatTimeForCommand(time: String): String = time

    open fun afterXMLRead(weekProfile: WeekProfile<H, C>) {}

    abstract fun getIntervalType(): IntervalType

    override fun getMaximumNumberOfHeatingIntervals(): Int {
        return maximumNumberOfHeatingIntervals
    }

    override fun getNumberOfIntervalsType(): NumberOfIntervalsType {
        return numberOfIntervalsType
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HeatingConfiguration::class.java)
    }
}