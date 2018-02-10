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
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.graph.backend.gplot.GPlotSeries;
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition;
import li.klass.fhem.update.backend.command.execution.Command;
import li.klass.fhem.update.backend.command.execution.CommandExecutionService;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class GraphService {
    private static final String ENTRY_FORMAT = "yyyy-MM-dd_HH:mm:ss";
    private static final DateTimeFormatter GRAPH_ENTRY_DATE_FORMATTER = DateTimeFormat.forPattern(ENTRY_FORMAT);
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm");
    static final String COMMAND_TEMPLATE = "get %s - - %s %s %s";

    private static final Logger LOG = LoggerFactory.getLogger(GraphService.class);

    private CommandExecutionService commandExecutionService;
    private GraphIntervalProvider graphIntervalProvider;

    @Inject
    GraphService(CommandExecutionService commandExecutionService, GraphIntervalProvider graphIntervalProvider) {
        this.commandExecutionService = commandExecutionService;
        this.graphIntervalProvider = graphIntervalProvider;
    }

    /**
     * Retrieves {@link GraphEntry} objects from FHEM. When the entries are available, the given listener object will
     * be notified.
     *
     * @param device             concerned device
     * @param connectionId       id of the server or absent (absent will use the currently selected server
     * @param svgGraphDefinition svg graph definition
     * @param startDate          read FileLog entries from the given date
     * @param endDate            read FileLog entries up to the given date
     * @param context            context     @return read graph data or null (if the device does not have a FileLog device)
     */
    @SuppressWarnings("unchecked")
    public GraphData getGraphData(FhemDevice device, Optional<String> connectionId, SvgGraphDefinition svgGraphDefinition,
                                  final DateTime startDate, final DateTime endDate, Context context) {
        Interval interval = getIntervalFor(startDate, endDate, context);

        HashMap<GPlotSeries, List<GraphEntry>> data = newHashMap();

        Set<GPlotSeries> series = Sets.newHashSet();
        series.addAll(svgGraphDefinition.getPlotDefinition().getLeftAxis().getSeries());
        series.addAll(svgGraphDefinition.getPlotDefinition().getRightAxis().getSeries());

        LOG.info("getGraphData - getting graph data for device {} and {} series", device.getName(), series.size());

        for (GPlotSeries plotSeries : series) {
            data.put(plotSeries, getCurrentGraphEntriesFor(svgGraphDefinition.getLogDeviceName(), connectionId, plotSeries, interval, svgGraphDefinition.getPlotfunction()));
        }

        return new GraphData(data, interval);
    }


    private Interval getIntervalFor(DateTime startDate, DateTime endDate, Context context) {
        return graphIntervalProvider.getIntervalFor(startDate, endDate, context);
    }

    /**
     * Collects FileLog entries from FHEM matching a given column specification. The results will be turned into
     * {@link GraphEntry} objects and be returned.
     *
     * @param logDevice    logDevice to load graph entries from.
     * @param connectionId id of the server or absent (absent will use the currently selected server)
     * @param gPlotSeries  chart description
     * @param interval     Interval containing start and end date
     * @param plotfunction SPEC parameters to replace      @return read logDevices entries converted to {@link GraphEntry} objects.
     */
    private List<GraphEntry> getCurrentGraphEntriesFor(String logDevice,
                                                       Optional<String> connectionId, GPlotSeries gPlotSeries,
                                                       Interval interval, List<String> plotfunction) {
        List<GraphEntry> graphEntries = findGraphEntries(loadLogData(logDevice, connectionId, interval, gPlotSeries, plotfunction));
        LOG.info("getCurrentGraphEntriesFor - found {} graph entries for logDevice {}", graphEntries.size(), logDevice);
        return graphEntries;
    }

    String loadLogData(String logDevice, Optional<String> connectionId, Interval interval,
                       GPlotSeries plotSeries, List<String> plotfunction) {
        String fromDateFormatted = DATE_TIME_FORMATTER.print(interval.getStart());
        String toDateFormatted = DATE_TIME_FORMATTER.print(interval.getEnd());

        StringBuilder result = new StringBuilder();

        String command = String.format(COMMAND_TEMPLATE, logDevice, fromDateFormatted, toDateFormatted, plotSeries.getLogDef());
        for (int i = 0; i < plotfunction.size(); i++) {
            command = command.replaceAll("<SPEC" + (i + 1) + ">", plotfunction.get(i));
        }

        String data = commandExecutionService.executeSync(new Command(command, connectionId));
        if (data != null) {
            result.append("\n\r").append(data.replaceAll("#[^\\\\]*\\\\[rn]", ""));
        }

        return result.toString();
    }

    /**
     * Looks for any {@link GraphEntry} objects within the returned String. Unfortunately, FHEM does not return any
     * line delimiters, to that a pretty complicated regular expression has to be applied.
     *
     * @param content content to parse
     * @return found {@link GraphEntry} objects.
     */
    List<GraphEntry> findGraphEntries(String content) {
        List<GraphEntry> result = newArrayList();

        if (content == null) return result;

        content = content.replaceAll("\r", "");

        String[] entries = content.split("\n");
        for (String entry : entries) {

            Optional<GraphEntry> parsed = parseEntry(entry);
            if (parsed.isPresent()) {
                result.add(parsed.get());
            }
        }

        return result;
    }

    Optional<GraphEntry> parseEntry(String entry) {
        String[] parts = entry.split(" ");
        if (parts.length != 2) return Optional.absent();

        String entryTime = parts[0];
        String entryValue = parts[1];

        try {
            if (ENTRY_FORMAT.length() == entryTime.length()) {
                DateTime entryDate = GRAPH_ENTRY_DATE_FORMATTER.parseDateTime(entryTime);
                float entryFloatValue = Float.valueOf(entryValue);

                return Optional.of(new GraphEntry(entryDate, entryFloatValue));
            } else {
                LOG.trace("silent ignore of {}, as having a wrong time format", entryTime);
            }
        } catch (NumberFormatException e) {
            Log.e(GraphService.class.getName(), "cannot parse date " + entryTime, e);
        } catch (Exception e) {
            Log.e(GraphService.class.getName(), "cannot parse number " + entryValue, e);
        }
        return Optional.absent();
    }
}
