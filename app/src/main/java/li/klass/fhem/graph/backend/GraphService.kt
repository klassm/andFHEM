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
import android.util.Log
import com.google.common.base.Optional
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.graph.backend.gplot.GPlotSeries
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

data class LogDataDefinition(val logDevice: String, val pattern: String, val series: GPlotSeries)

@Singleton
class GraphService @Inject constructor(
        private val commandExecutionService: CommandExecutionService,
        private val graphIntervalProvider: GraphIntervalProvider,
        private val deviceListService: DeviceListService
) {

    /**
     * Retrieves [GraphEntry] objects from FHEM. When the entries are available, the given listener object will
     * be notified.
     *
     * @param device             concerned device
     * @param connectionId       id of the server or absent (absent will use the currently selected server
     * @param svgGraphDefinition svg graph definition
     * @param startDate          read FileLog entries from the given date
     * @param endDate            read FileLog entries up to the given date
     * @param context            context     @return read graph data or null (if the device does not have a FileLog device)
     */
    fun getGraphData(device: FhemDevice, connectionId: String?, svgGraphDefinition: SvgGraphDefinition,
                     startDate: DateTime?, endDate: DateTime?, context: Context): GraphData {
        val interval = graphIntervalProvider.getIntervalFor(startDate, endDate, context)

        val series: Set<GPlotSeries> = svgGraphDefinition.plotDefinition.let {
            it.leftAxis.series + it.rightAxis.series
        }.toSet()

        LOG.info("getGraphData - getting graph data for device {} and {} series", device.name, series.size)

        val data = series
                .mapNotNull { getLogDefinitionFor(it, svgGraphDefinition, connectionId) }
                .map {
                    it.series to getCurrentGraphEntriesFor(it, connectionId, interval, svgGraphDefinition.plotReplace, svgGraphDefinition.plotfunction)
                }.toMap()

        return GraphData(data, interval)
    }

    private fun getLogDefinitionFor(series: GPlotSeries, svgGraphDefinition: SvgGraphDefinition, connectionId: String?): LogDataDefinition? {
        val dataProviders = series.dataProvider
        val custom = dataProviders.customLogDevice
        if (custom != null) {
            return LogDataDefinition(logDevice = custom.logDevice, pattern = custom.pattern, series = series)
        }

        val logDevice = svgGraphDefinition.logDeviceName
        val device = deviceListService.getDeviceForName(logDevice, connectionId) ?: return null
        return when (device.xmlListDevice.type) {
            "FileLog" ->
                dataProviders.fileLog?.pattern?.let { pattern -> LogDataDefinition(device.name, pattern, series) }
            "DbLog" ->
                dataProviders.dbLog?.pattern?.let { pattern -> LogDataDefinition(device.name, pattern, series) }
            else -> null
        }
    }


    /**
     * Collects FileLog entries from FHEM matching a given column specification. The results will be turned into
     * [GraphEntry] objects and be returned.
     *
     * @param logDefinition
     * @param connectionId id of the server or absent (absent will use the currently selected server)
     * @param interval     Interval containing start and end date
     * @param plotReplace  key=value pairs to replace
     * @param plotfunction SPEC parameters to replace      @return read logDevices entries converted to [GraphEntry] objects.
     */
    private fun getCurrentGraphEntriesFor(logDefinition: LogDataDefinition,
                                          connectionId: String?, interval: Interval,
                                          plotReplace: Map<String, String>,
                                          plotfunction: List<String>): List<GraphEntry> {
        val graphEntries = findGraphEntries(
                loadLogData(logDefinition, connectionId, interval, plotReplace, plotfunction))
        LOG.info("getCurrentGraphEntriesFor - found {} graph entries for logDevice {}", graphEntries.size, logDefinition.logDevice)
        return graphEntries
    }

    private fun loadLogData(logDefinition: LogDataDefinition, connectionId: String?, interval: Interval,
                            plotReplace: Map<String, String>,
                            plotfunction: List<String>): String {
        val fromDateFormatted = DATE_TIME_FORMATTER.print(interval.start)
        val toDateFormatted = DATE_TIME_FORMATTER.print(interval.end)


        var command = String.format(COMMAND_TEMPLATE, logDefinition.logDevice, fromDateFormatted, toDateFormatted, logDefinition.pattern)
        for ((key, value) in plotReplace) {
            LOG.trace("Replace {} by {}", key, value)
            command = command.replace(("%" + key + "%").toRegex(), value)
        }
        for (i in plotfunction.indices) {
            command = command.replace(("<SPEC" + (i + 1) + ">").toRegex(), plotfunction[i])
        }
        LOG.trace("Command: {}", command)
        val result = commandExecutionService.executeSync(Command(command, Optional.fromNullable(connectionId)))
                ?.replace("#[^\\\\]*\\\\[rn]".toRegex(), "")
                ?: throw IllegalStateException("could not get a response for command $command")
        return "\n\r$result"
    }

    internal fun findGraphEntries(content: String?): List<GraphEntry> {
        content ?: return emptyList()

        return content.replace("\r".toRegex(), "")
                .split("\n".toRegex())
                .filterNot { it.isEmpty() }
                .mapNotNull { parseEntry(it) }
    }

    internal fun parseEntry(entry: String): GraphEntry? {
        val parts = entry.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
        if (parts.size != 2) return null

        val entryTime = parts[0]
        val entryValue = parts[1]
        LOG.trace("Entry {}", entry);
        try {
            if (ENTRY_FORMAT.length == entryTime.length) {
                val entryDate = GRAPH_ENTRY_DATE_FORMATTER.parseDateTime(entryTime)
                val entryFloatValue = java.lang.Float.valueOf(entryValue)

                return GraphEntry(entryDate, entryFloatValue)
            } else {
                LOG.debug("silent ignore of {}, as having a wrong time format", entryTime)
            }
        } catch (e: NumberFormatException) {
            Log.e(GraphService::class.java.name, "cannot parse date $entryTime", e)
        } catch (e: Exception) {
            Log.e(GraphService::class.java.name, "cannot parse number $entryValue", e)
        }

        return null
    }

    companion object {
        private const val ENTRY_FORMAT = "yyyy-MM-dd_HH:mm:ss"
        private val GRAPH_ENTRY_DATE_FORMATTER = DateTimeFormat.forPattern(ENTRY_FORMAT)
        internal val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm")
        internal const val COMMAND_TEMPLATE = "get %s - - %s %s %s"

        private val LOG = LoggerFactory.getLogger(GraphService::class.java)
    }
}
