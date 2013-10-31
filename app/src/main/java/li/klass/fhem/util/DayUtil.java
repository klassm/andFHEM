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

import li.klass.fhem.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static li.klass.fhem.util.DayUtil.Day.*;

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

    private static Map<String, Day> shortNameToStringIdMap = new HashMap<String, Day>();

    static {
        shortNameToStringIdMap.put("MON", MONDAY);
        shortNameToStringIdMap.put("TUE", TUESDAY);
        shortNameToStringIdMap.put("WED", WEDNESDAY);
        shortNameToStringIdMap.put("THU", THURSDAY);
        shortNameToStringIdMap.put("FRI", FRIDAY);
        shortNameToStringIdMap.put("SAT", SATURDAY);
        shortNameToStringIdMap.put("SUN", SUNDAY);
    }

    public static List<Integer> getSortedDayStringIdList() {
        return Arrays.asList(R.string.monday, R.string.tuesday, R.string.wednesday, R.string.thursday,
                R.string.friday, R.string.saturday, R.string.sunday);
    }

    public static Day getDayForShortName(String shortName) {
        shortName = shortName.toUpperCase();
        return shortNameToStringIdMap.get(shortName);
    }

    public static String getShortNameForStringId(Integer stringId) {
        for (Map.Entry<String, Day> entry : shortNameToStringIdMap.entrySet()) {
            if (entry.getValue().getStringId() == stringId) {
                return entry.getKey().toLowerCase();
            }
        }
        return null;
    }

    public static String getShortNameFor(Day day) {
        for (Map.Entry<String, Day> entry : shortNameToStringIdMap.entrySet()) {
            if (entry.getValue() == day) {
                return entry.getKey().toLowerCase();
            }
        }
        return null;
    }
}
