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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.fht

import li.klass.fhem.util.DateTimeProvider
import org.joda.time.DateTime
import javax.inject.Inject

class HolidayShortCalculator @Inject constructor(val dateTimeProvider: DateTimeProvider) {

    fun holidayShortIsTomorrow(switchTime: DateTime, baseline: DateTime): Boolean {
        val currentMinute = baseline.hourOfDay * 60 + baseline.minuteOfHour
        val switchMinute = switchTime.hourOfDay * 60 + switchTime.minuteOfHour

        return switchMinute < currentMinute
    }

    fun calculateHoliday1ValueFrom(hour: Int, minute: Int): Int =
            hour * 6 + minute / 10


    fun holiday1SwitchTimeFor(hourOfDay: Int, minute: Int): DateTime {
        var hour = hourOfDay

        val newMinute = (Math.round(minute / 10.0) * 10 % 60).toInt()
        if (newMinute == 0) {
            hour += if (minute > 30) 1 else 0
        }
        hour %= 24

        val now = dateTimeProvider.now()
        var switchTime = DateTime(now.year, now.monthOfYear, now.dayOfMonth, hour, newMinute)

        if (holidayShortIsTomorrow(switchTime, now)) {
            switchTime = switchTime.plusDays(1)
        }
        return switchTime
    }
}