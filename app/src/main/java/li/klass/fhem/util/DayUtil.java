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

package li.klass.fhem.util;

import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

import li.klass.fhem.R;

import static li.klass.fhem.util.DayUtil.Day.FRIDAY;
import static li.klass.fhem.util.DayUtil.Day.MONDAY;
import static li.klass.fhem.util.DayUtil.Day.SATURDAY;
import static li.klass.fhem.util.DayUtil.Day.SUNDAY;
import static li.klass.fhem.util.DayUtil.Day.THURSDAY;
import static li.klass.fhem.util.DayUtil.Day.TUESDAY;
import static li.klass.fhem.util.DayUtil.Day.WEDNESDAY;

public class DayUtil {
    public enum Day {
        MONDAY(R.string.monday),
        TUESDAY(R.string.tuesday),
        WEDNESDAY(R.string.wednesday),
        THURSDAY(R.string.thursday),
        FRIDAY(R.string.friday),
        SATURDAY(R.string.saturday),
        SUNDAY(R.string.sunday);
        private final int stringId;

        private Day(int stringId) {
            this.stringId = stringId;
        }

        public int getStringId() {
            return stringId;
        }
    }

    private static final Map<String, Day> SHORT_NAME_TO_STRING_ID_MAP = Maps.newHashMap();

    static {
        SHORT_NAME_TO_STRING_ID_MAP.put("MON", MONDAY);
        SHORT_NAME_TO_STRING_ID_MAP.put("TUE", TUESDAY);
        SHORT_NAME_TO_STRING_ID_MAP.put("WED", WEDNESDAY);
        SHORT_NAME_TO_STRING_ID_MAP.put("THU", THURSDAY);
        SHORT_NAME_TO_STRING_ID_MAP.put("FRI", FRIDAY);
        SHORT_NAME_TO_STRING_ID_MAP.put("SAT", SATURDAY);
        SHORT_NAME_TO_STRING_ID_MAP.put("SUN", SUNDAY);
    }

    public static Day getDayForShortName(String shortName) {
        shortName = shortName.toUpperCase(Locale.getDefault());
        return SHORT_NAME_TO_STRING_ID_MAP.get(shortName);
    }

    public static String getShortNameFor(Day day) {
        for (Map.Entry<String, Day> entry : SHORT_NAME_TO_STRING_ID_MAP.entrySet()) {
            if (entry.getValue() == day) {
                return entry.getKey().toLowerCase(Locale.getDefault());
            }
        }
        return null;
    }
}
