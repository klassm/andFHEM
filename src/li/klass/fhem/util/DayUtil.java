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
