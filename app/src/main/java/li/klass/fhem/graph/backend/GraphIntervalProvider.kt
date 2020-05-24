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

package li.klass.fhem.graph.backend

import android.content.Context
import android.preference.PreferenceManager
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.util.DateFormatUtil
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.ReadablePeriod
import javax.inject.Inject

class GraphIntervalProvider @Inject constructor(
        private val commandExecutionService: CommandExecutionService
) {

    fun getIntervalFor(startDate: DateTime?, endDate: DateTime?, fixedrange: Pair<ReadablePeriod, ReadablePeriod>?, context: Context, connectionId: String?): Interval =
            if (startDate == null || endDate == null) {
                getDefaultInterval(context, connectionId, fixedrange)
            } else Interval(startDate, endDate)

    private fun getDefaultInterval(context: Context, connectionId: String?, fixedrange: Pair<ReadablePeriod, ReadablePeriod>?): Interval {
        val result = commandExecutionService.executeSync(Command("{{ TimeNow() }}", connectionId))
                ?: return getIntervalForTimespan(context, fixedrange, DateTime.now())
        return getIntervalForTimespan(context, fixedrange, DateFormatUtil.FHEM_DATE_FORMAT.parseDateTime(result))
    }

    private fun getIntervalForTimespan(context: Context, fixedrange : Pair<ReadablePeriod, ReadablePeriod>?, endDate: DateTime): Interval {
        fixedrange?.let {
            return Interval(endDate.minus(fixedrange.first).plus(fixedrange.second), endDate.plus(fixedrange.second))
        }
        // App settings apply only if not overridden by server
        val hoursToSubtract = getChartingDefaultTimespan(context)
        if (hoursToSubtract == CURRENT_DAY_TIMESPAN) {
            return Interval(endDate.withHourOfDay(0).withMinuteOfHour(0), endDate)
        }
        return Interval(endDate.minusHours(hoursToSubtract), endDate)
    }

    fun getChartingDefaultTimespan(context: Context): Int {
        val timeSpan = PreferenceManager.getDefaultSharedPreferences(context).getString("GRAPH_DEFAULT_TIMESPAN", "24")
        return Integer.valueOf(timeSpan!!.trim { it <= ' ' })
    }

    companion object {
        private const val CURRENT_DAY_TIMESPAN = -1
    }
}
