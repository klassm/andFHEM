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

package li.klass.fhem.service.graph;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import org.joda.time.DateTime;
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
import li.klass.fhem.domain.log.LogDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.graph.gplot.GPlotSeries;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class GraphService {
    public static final String ENTRY_FORMAT = "yyyy-MM-dd_HH:mm:ss";
    public static final DateTimeFormatter GRAPH_ENTRY_DATE_FORMATTER = DateTimeFormat.forPattern(ENTRY_FORMAT);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm");

    private static final Logger LOG = LoggerFactory.getLogger(GraphService.class);

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    public GraphService() {
    }

    /**
     * Retrieves {@link GraphEntry} objects from FHEM. When the entries are available, the given listener object will
     * be notified.
     *
     * @param device             concerned device
     * @param svgGraphDefinition svg graph definition
     * @param startDate          read FileLog entries from the given date
     * @param endDate            read FileLog entries up to the given date
     * @param context            context
     * @return read graph data or null (if the device does not have a FileLog device)
     */
    @SuppressWarnings("unchecked")
    public HashMap<GPlotSeries, List<GraphEntry>> getGraphData(FhemDevice device, SvgGraphDefinition svgGraphDefinition,
                                                               final DateTime startDate, final DateTime endDate, Context context) {

        if (device.getSvgGraphDefinitions().isEmpty()) return null;

        HashMap<GPlotSeries, List<GraphEntry>> data = newHashMap();

        Set<GPlotSeries> series = Sets.newHashSet();
        series.addAll(svgGraphDefinition.getPlotDefinition().getLeftAxis().getSeries());
        series.addAll(svgGraphDefinition.getPlotDefinition().getRightAxis().getSeries());

        for (GPlotSeries plotSeries : series) {
            data.put(plotSeries, getCurrentGraphEntriesFor(svgGraphDefinition.getLogDevice(), plotSeries, startDate, endDate, context, svgGraphDefinition.getPlotfunction()));
        }

        return data;
    }

    /**
     * Collects FileLog entries from FHEM matching a given column specification. The results will be turned into
     * {@link GraphEntry} objects and be returned.
     *
     * @param logDevice   logDevice to load graph entries from.
     * @param gPlotSeries chart description
     * @param startDate   read FileLog entries from the given date
     * @param endDate     read FileLog entries up to the given date
     * @param context     context
     * @param plotfunction SPEC parameters to replace
     * @return read logDevices entries converted to {@link GraphEntry} objects.
     */
    private List<GraphEntry> getCurrentGraphEntriesFor(LogDevice logDevice,
                                                       GPlotSeries gPlotSeries,
                                                       DateTime startDate, DateTime endDate, Context context, List<String> plotfunction) {
        return findGraphEntries(loadLogData(logDevice, startDate, endDate, gPlotSeries, context, plotfunction));
    }

    public String loadLogData(LogDevice logDevice, DateTime fromDate, DateTime toDate,
                              GPlotSeries plotSeries, Context context, List<String> plotfunction) {
        String fromDateFormatted = DATE_TIME_FORMATTER.print(fromDate);
        String toDateFormatted = DATE_TIME_FORMATTER.print(toDate);

        StringBuilder result = new StringBuilder();

        @SuppressWarnings("unchecked")
        String command = logDevice.getGraphCommandFor(fromDateFormatted,
                toDateFormatted, plotSeries);

        for (int i = 0; i < plotfunction.size(); i++) {
            command = command.replaceAll("<SPEC" + (i + 1) + ">", plotfunction.get(i));
        }

        String data = commandExecutionService.executeSafely(command, context);
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
