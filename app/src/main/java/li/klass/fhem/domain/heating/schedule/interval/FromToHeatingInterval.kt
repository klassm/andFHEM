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
package li.klass.fhem.domain.heating.schedule.interval

import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration

class FromToHeatingInterval : BaseHeatingInterval<FromToHeatingInterval> {
    private var fromTime: String
    private var toTime: String
    var changedFromTime: String
    var changedToTime: String

    constructor(configuration: HeatingConfiguration<*, *>) {
        fromTime = configuration.offTime
        toTime = configuration.offTime
        changedFromTime = configuration.offTime
        changedToTime = configuration.offTime
    }

    constructor(toCopy: FromToHeatingInterval) {
        fromTime = toCopy.getFromTime()
        toTime = toCopy.getToTime()
        changedFromTime = toCopy.changedFromTime
        changedToTime = toCopy.changedToTime
        isNew = true
    }

    fun getFromTime(): String = fromTime

    fun setFromTime(fromTime: String) {
        changedFromTime = fromTime
        this.fromTime = fromTime
    }

    fun getToTime(): String = toTime

    fun setToTime(toTime: String) {
        changedToTime = toTime
        this.toTime = toTime
    }

    override fun isModified(): Boolean =
            super.isModified() || fromTime != changedFromTime || toTime != changedToTime

    override fun acceptChanges() {
        super.acceptChanges()
        fromTime = changedFromTime
        toTime = changedToTime
    }

    override fun reset() {
        changedFromTime = fromTime
        changedToTime = toTime
    }

    override fun copy(): FromToHeatingInterval = FromToHeatingInterval(this)

    override fun compareTo(other: BaseHeatingInterval<*>): Int {
        if (other !is FromToHeatingInterval) return 1
        val compare = changedFromTime.compareTo(other.changedFromTime)
        return if (compare != 0) compare else changedToTime.compareTo(other.changedToTime)
    }
}