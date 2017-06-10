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

import com.google.common.collect.FluentIterable.from
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.configuration.HeatingIntervalConfiguration
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval
import li.klass.fhem.util.DayUtil
import java.io.Serializable
import java.util.*

class DayProfile<H : BaseHeatingInterval<H>, INTERVAL_CONFIG : HeatingIntervalConfiguration<H>>(val day: DayUtil.Day, configuration: INTERVAL_CONFIG) : Serializable {
    private val heatingIntervals = ArrayList<H>()
    private val deletedIntervals = ArrayList<H>()

    private val heatingConfiguration: HeatingIntervalConfiguration<*>

    init {
        this.heatingConfiguration = configuration

        if (heatingConfiguration.numberOfIntervalsType == HeatingConfiguration.NumberOfIntervalsType.FIXED) {
            for (i in 0..maximumNumberOfHeatingIntervals - 1) {
                heatingIntervals.add(configuration.createHeatingInterval())
            }
        }
    }

    fun addHeatingInterval(interval: H): Boolean {
        if (canAddHeatingInterval()) return false
        heatingIntervals.add(interval)
        return true
    }

    fun deleteHeatingIntervalAt(position: Int): Boolean {
        if (position > heatingIntervals.size) return false

        val interval = heatingIntervals[position]
        deletedIntervals.add(interval)

        return true
    }

    fun getHeatingIntervalAt(position: Int): H? {
        if (position >= heatingIntervals.size) return null

        return heatingIntervals[position]
    }

    val numberOfHeatingIntervals: Int
        get() = heatingIntervals.size

    fun getHeatingIntervals(): List<H> {
        return Collections.unmodifiableList(heatingIntervals)
    }

    fun replaceHeatingIntervalsWith(newIntervals: List<H>) {
        heatingIntervals.clear()
        heatingIntervals.addAll(from(newIntervals)
                .transform { input -> input!!.copy() }.toList())
    }

    private fun canAddHeatingInterval(): Boolean {
        if (heatingConfiguration.getNumberOfIntervalsType() == HeatingConfiguration.NumberOfIntervalsType.FIXED) {
            return false
        }

        val maximumNumberOfHeatingIntervals = maximumNumberOfHeatingIntervals
        return maximumNumberOfHeatingIntervals != -1 && heatingIntervals.size >= maximumNumberOfHeatingIntervals
    }

    val isModified: Boolean
        get() {
            return if (heatingIntervals.any { it.isModified }) true else !deletedIntervals.isEmpty()
        }

    val maximumNumberOfHeatingIntervals: Int
        get() = heatingConfiguration.getMaximumNumberOfHeatingIntervals()

    fun acceptChanges() {
        for (heatingInterval in heatingIntervals) {
            heatingInterval.acceptChanges()
        }
        Collections.sort(heatingIntervals)
        deletedIntervals.clear()
    }

    fun reset() {
        val iterator = heatingIntervals.iterator()
        while (iterator.hasNext()) {
            val heatingInterval = iterator.next()

            heatingInterval.reset()
            if (heatingInterval.isNew) {
                iterator.remove()
            }
        }

        deletedIntervals.filterNotTo(heatingIntervals) { it.isNew }
        deletedIntervals.clear()

        Collections.sort(heatingIntervals)
    }

    override fun toString(): String {
        return "DayProfile{" +
                "day=" + day +
                ", heatingIntervals=" + heatingIntervals +
                ", deletedIntervals=" + deletedIntervals +
                ", heatingConfiguration=" + heatingConfiguration +
                '}'
    }
}
