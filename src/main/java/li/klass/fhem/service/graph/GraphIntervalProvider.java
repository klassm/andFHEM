package li.klass.fhem.service.graph;

import android.content.Context;
import android.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.inject.Inject;

import li.klass.fhem.service.Command;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.DateFormatUtil;

class GraphIntervalProvider {
    private CommandExecutionService commandExecutionService;
    private static final int CURRENT_DAY_TIMESPAN = -1;

    @Inject
    public GraphIntervalProvider(CommandExecutionService commandExecutionService) {
        this.commandExecutionService = commandExecutionService;
    }

    public Interval getIntervalFor(DateTime startDate, DateTime endDate, Context context) {
        if (startDate == null || endDate == null) {
            return getDefaultInterval(context);
        }
        return new Interval(startDate, endDate);
    }

    private Interval getDefaultInterval(Context context) {
        String result = commandExecutionService.executeSync(new Command("{{ TimeNow() }}"), context);
        if (result == null) {
            return getIntervalForTimespan(context, DateTime.now());
        }
        return getIntervalForTimespan(context, DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime(result));
    }

    private Interval getIntervalForTimespan(Context context, DateTime endDate) {
        int hoursToSubtract = getChartingDefaultTimespan(context);
        if (hoursToSubtract == CURRENT_DAY_TIMESPAN) {
            hoursToSubtract = 24;
        }
        return new Interval(endDate.minusHours(hoursToSubtract), endDate);
    }

    private int getChartingDefaultTimespan(Context context) {
        String timeSpan = PreferenceManager.getDefaultSharedPreferences(context).getString("GRAPH_DEFAULT_TIMESPAN", "24");
        return Integer.valueOf(timeSpan.trim());
    }
}
