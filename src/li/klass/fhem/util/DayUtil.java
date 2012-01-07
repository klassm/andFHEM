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

public class DayUtil {
    
    private static Map<String, Integer> shortNameToStringIdMap = new HashMap<String, Integer>();
    
    static {
        shortNameToStringIdMap.put("MON", R.string.monday);
        shortNameToStringIdMap.put("TUE", R.string.tuesday);
        shortNameToStringIdMap.put("WED", R.string.wednesday);
        shortNameToStringIdMap.put("THU", R.string.thursday);
        shortNameToStringIdMap.put("FRI", R.string.friday);
        shortNameToStringIdMap.put("SAT", R.string.saturday);
        shortNameToStringIdMap.put("SUN", R.string.sunday);
    }
    
    public static List<Integer> getSortedDayStringIdList() {
        return Arrays.asList(R.string.monday, R.string.tuesday, R.string.wednesday, R.string.thursday,
                R.string.friday, R.string.saturday, R.string.sunday);
    }
    
    public static Integer getDayStringIdForShortName(String shortName) {
        shortName = shortName.toUpperCase();
        return shortNameToStringIdMap.get(shortName);
    }
    
    public static String getShortNameForStringId(Integer id) {
        for (Map.Entry<String, Integer> entry : shortNameToStringIdMap.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey().toLowerCase();
            }
        }
        return null;
    }
}
