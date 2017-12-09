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

package li.klass.fhem.devices.backend.at

import li.klass.fhem.R

enum class AtRepetition(val stringId: Int, val weekdayOrdinate: Int = -1) {
    ONCE(R.string.timer_overview_once), EVERY_DAY(R.string.timer_overview_every_day),
    WEEKEND(R.string.timer_overview_weekend), WEEKDAY(R.string.timer_overview_weekday),
    MONDAY(R.string.monday, 1), TUESDAY(R.string.tuesday, 2), WEDNESDAY(R.string.wednesday, 3),
    THURSDAY(R.string.thursday, 4), FRIDAY(R.string.friday, 5), SATURDAY(R.string.saturday, 6), SUNDAY(R.string.sunday, 0);

    companion object {
        fun getRepetitionForWeekdayOrdinate(ordinate: Int): AtRepetition? =
                values().firstOrNull { it.weekdayOrdinate == ordinate }
    }
}
