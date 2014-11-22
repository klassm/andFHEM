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

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.log.LogDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

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

    /**
     * Retrieves {@link GraphEntry} objects from FHEM. When the entries are available, the given listener object will
     * be notified.
     *
     * @param device             concerned device
     * @param seriesDescriptions series descriptions each representing one series in the resulting chart
     * @param startDate          read FileLog entries from the given date
     * @param endDate            read FileLog entries up to the given date
     * @return read graph data or null (if the device does not have a FileLog device)
     */
    @SuppressWarnings("unchecked")
    public HashMap<ChartSeriesDescription, List<GraphEntry>> getGraphData(Device device, ArrayList<ChartSeriesDescription> seriesDescriptions,
                                                                          final DateTime startDate, final DateTime endDate) {

        if (device.getLogDevices().isEmpty()) return null;

        HashMap<ChartSeriesDescription, List<GraphEntry>> data = newHashMap();

        for (ChartSeriesDescription seriesDescription : seriesDescriptions) {
            data.put(seriesDescription, getCurrentGraphEntriesFor(device, seriesDescription, startDate, endDate));
        }

        return data;
    }

    /**
     * Collects FileLog entries from FHEM matching a given column specification. The results will be turned into
     * {@link GraphEntry} objects and be returned.
     *
     * @param device            device to load graph entries from.
     * @param seriesDescription chart description
     * @param startDate         read FileLog entries from the given date
     * @param endDate           read FileLog entries up to the given date
     * @return read logDevices entries converted to {@link GraphEntry} objects.
     */
    private List<GraphEntry> getCurrentGraphEntriesFor(Device device,
                                                       ChartSeriesDescription seriesDescription,
                                                       DateTime startDate, DateTime endDate) {
        String result = loadLogData(device, startDate, endDate, seriesDescription);
        return findGraphEntries(result);
    }

    public String loadLogData(Device device, DateTime fromDate, DateTime toDate,
                              ChartSeriesDescription seriesDescription) {
        String fromDateFormatted = DATE_TIME_FORMATTER.print(fromDate);
        String toDateFormatted = DATE_TIME_FORMATTER.print(toDate);

        StringBuilder result = new StringBuilder();

        @SuppressWarnings("unchecked")
        List<LogDevice<?>> logDevices = device.getLogDevices();
        for (LogDevice<?> logDevice : logDevices) {
            String command = logDevice.getGraphCommandFor(device, fromDateFormatted,
                    toDateFormatted, seriesDescription);

            String data = commandExecutionService.executeSafely(command);
            if (data != null) {
                result.append("\n\r").append(data.replaceAll("#" + seriesDescription, ""));
            }
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

            String[] parts = entry.split(" ");
            if (parts.length != 2) continue;

            String entryTime = parts[0];
            String entryValue = parts[1];

            try {
                if (ENTRY_FORMAT.length() == entryTime.length()) {
                    DateTime entryDate = GRAPH_ENTRY_DATE_FORMATTER.parseDateTime(entryTime);
                    float entryFloatValue = Float.valueOf(entryValue);

                    result.add(new GraphEntry(entryDate, entryFloatValue));
                } else {
                    LOG.trace("silent ignore of {}, as having a wrong time format", entryTime);
                }
            } catch (NumberFormatException e) {
                Log.e(GraphService.class.getName(), "cannot parse date " + entryTime, e);
            } catch (Exception e) {
                Log.e(GraphService.class.getName(), "cannot parse number " + entryValue, e);
            }
        }

        return result;
    }
}
