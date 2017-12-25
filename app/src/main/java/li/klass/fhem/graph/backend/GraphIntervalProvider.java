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

package li.klass.fhem.graph.backend;

import android.content.Context;
import android.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.inject.Inject;

import li.klass.fhem.update.backend.command.execution.Command;
import li.klass.fhem.update.backend.command.execution.CommandExecutionService;
import li.klass.fhem.util.DateFormatUtil;

class GraphIntervalProvider {
    private CommandExecutionService commandExecutionService;
    private static final int CURRENT_DAY_TIMESPAN = -1;

    @Inject
    public GraphIntervalProvider(CommandExecutionService commandExecutionService) {
        this.commandExecutionService = commandExecutionService;
    }

    Interval getIntervalFor(DateTime startDate, DateTime endDate, Context context) {
        if (startDate == null || endDate == null) {
            return getDefaultInterval(context);
        }
        return new Interval(startDate, endDate);
    }

    private Interval getDefaultInterval(Context context) {
        String result = commandExecutionService.executeSync(new Command("{{ TimeNow() }}"));
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
