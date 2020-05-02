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
package li.klass.fhem.util

import li.klass.fhem.R
import java.util.*

object DayUtil {
    private val SHORT_NAME_TO_STRING_ID_MAP: MutableMap<String, Day> = mutableMapOf()

    @JvmStatic
    fun getDayForShortName(shortName: String): Day? =
            SHORT_NAME_TO_STRING_ID_MAP[shortName.toUpperCase(Locale.getDefault())]

    @JvmStatic
    fun getShortNameFor(day: Day): String? {
        for ((key, value) in SHORT_NAME_TO_STRING_ID_MAP) {
            if (value == day) {
                return key.toLowerCase(Locale.getDefault())
            }
        }
        return null
    }

    enum class Day(val stringId: Int) {
        MONDAY(R.string.monday), TUESDAY(R.string.tuesday), WEDNESDAY(R.string.wednesday), THURSDAY(R.string.thursday), FRIDAY(R.string.friday), SATURDAY(R.string.saturday), SUNDAY(R.string.sunday);
    }

    init {
        SHORT_NAME_TO_STRING_ID_MAP["MON"] = Day.MONDAY
        SHORT_NAME_TO_STRING_ID_MAP["TUE"] = Day.TUESDAY
        SHORT_NAME_TO_STRING_ID_MAP["WED"] = Day.WEDNESDAY
        SHORT_NAME_TO_STRING_ID_MAP["THU"] = Day.THURSDAY
        SHORT_NAME_TO_STRING_ID_MAP["FRI"] = Day.FRIDAY
        SHORT_NAME_TO_STRING_ID_MAP["SAT"] = Day.SATURDAY
        SHORT_NAME_TO_STRING_ID_MAP["SUN"] = Day.SUNDAY
    }
}