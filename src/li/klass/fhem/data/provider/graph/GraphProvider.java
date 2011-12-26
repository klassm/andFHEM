package li.klass.fhem.data.provider.graph;

import android.util.Log;
import li.klass.fhem.data.DataProviderSwitch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GraphProvider {
    public static final GraphProvider INSTANCE = new GraphProvider();

    private GraphProvider() {
    }

    public List<GraphEntry> getCurrentGraphEntriesFor(String fileLogName, String columnSpec) {
        Date now = new Date();
        Date today = new Date(now.getYear(), now.getMonth(), now.getDate());
        Date yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

        String result = DataProviderSwitch.INSTANCE.getCurrentProvider().fileLogData(fileLogName, yesterday, today, columnSpec);
        result = result.replace("#" + columnSpec, "");

        String yesterdayFormat = dateFormat.format(yesterday);
        String todayFormat = dateFormat.format(today);

        List<String> parts = splitIntoParts(result, yesterdayFormat, todayFormat);

        return partsToGraphEntries(parts);
    }

    private List<GraphEntry> partsToGraphEntries(List<String> parts) {
        SortedSet<GraphEntry> entries = new TreeSet<GraphEntry> ();

        SimpleDateFormat providedDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

        Date currentDate = null;
        float currentValue;
        for (String part : parts) {
            if (part.trim().isEmpty()) {
                continue;
            }

            if (part.contains("-") && part.contains("_")) {
                try {
                    currentDate = providedDateFormat.parse(part);
                } catch (ParseException e) {
                    Log.e(GraphProvider.class.getName(), "cannot parse date " + part, e);
                }
            } else if (! part.substring(1).contains("-") && currentDate != null) {
                try {
                    currentValue = Float.valueOf(part);
                    GraphEntry graphEntry = new GraphEntry(currentDate, currentValue);
                    entries.add(graphEntry);
                } catch (NumberFormatException e) {
                    Log.e(GraphProvider.class.getName(), "cannot format " + part, e);
                }
            }
        }

        return new ArrayList<GraphEntry>(entries);
    }

    private List<String> splitIntoParts(String result, String yesterdayFormat, String todayFormat) {
        List<String> parts = new ArrayList<String>();
        String[] split = result.split(" ");
        for (String s : split) {
            if (s.contains(yesterdayFormat)) {
                String[] split1 = s.split(yesterdayFormat);
                parts.add(split1[0]);
                parts.add(yesterdayFormat + split1[1]);
            } else if (s.contains(todayFormat)) {
                String[] split1 = s.split(todayFormat);
                parts.add(split1[0]);
                parts.add(todayFormat + split1[1]);
            } else {
                parts.add(s);
            }
        }
        return parts;
    }
}
