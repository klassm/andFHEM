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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

public class GraphService {
    public static final GraphService INSTANCE = new GraphService();
    public static final SimpleDateFormat GRAPH_ENTRY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    private GraphService() {
    }

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
                                                                          final Calendar startDate, final Calendar endDate) {

        if (device.getFileLog() == null) return null;

        HashMap<ChartSeriesDescription, List<GraphEntry>> data = new HashMap<ChartSeriesDescription, List<GraphEntry>>();

        for (ChartSeriesDescription seriesDescription : seriesDescriptions) {
            String columnSpec = seriesDescription.getColumnSpecification();
            String fileLogDeviceName = device.getFileLog().getName();

            List<GraphEntry> valueEntries = getCurrentGraphEntriesFor(fileLogDeviceName, columnSpec, startDate, endDate);

            data.put(seriesDescription, valueEntries);
        }

        return data;
    }

    /**
     * Collects FileLog entries from FHEM matching a given column specification. The results will be turned into
     * {@link GraphEntry} objects and be returned.
     *
     * @param fileLogName name of the fileLog. This usually equals to "FileLog_${deviceName}".
     * @param columnSpec  column specification to read.
     * @param startDate   read FileLog entries from the given date
     * @param endDate     read FileLog entries up to the given date
     * @return read fileLog entries converted to {@link GraphEntry} objects.
     */
    private List<GraphEntry> getCurrentGraphEntriesFor(String fileLogName, String columnSpec,
                                                       Calendar startDate, Calendar endDate) {
        String result = fileLogData(fileLogName, startDate.getTime(), endDate.getTime(), columnSpec);
        return findGraphEntries(result);
    }

    public String fileLogData(String logName, Date fromDate, Date toDate,
                              String columnSpec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        String command = "get " + logName + " - - "
                + dateFormat.format(fromDate) + " " + dateFormat.format(toDate)
                + " " + columnSpec;

        String data = CommandExecutionService.INSTANCE.executeSafely(command);
        if (data == null) return null;

        return data.replaceAll("#" + columnSpec, "");
    }

    /**
     * Looks for any {@link GraphEntry} objects within the returned String. Unfortunately, FHEM does not return any
     * line delimiters, to that a pretty complicated regular expression has to be applied.
     *
     * @param content content to parse
     * @return found {@link GraphEntry} objects.
     */
    List<GraphEntry> findGraphEntries(String content) {
        List<GraphEntry> result = new ArrayList<GraphEntry>();

        if (content == null) return result;

        content = content.replaceAll("\r", "");

        String[] entries = content.split("\n");
        for (String entry : entries) {

            String[] parts = entry.split(" ");
            if (parts.length != 2) continue;

            String entryTime = parts[0];
            String entryValue = parts[1];

            try {
                Date entryDate = GRAPH_ENTRY_DATE_FORMAT.parse(entryTime);
                float entryFloatValue = Float.valueOf(entryValue);

                result.add(new GraphEntry(entryDate, entryFloatValue));
            } catch (ParseException e) {
                Log.e(GraphService.class.getName(), "cannot parse date " + entryTime, e);
            } catch (NumberFormatException e) {
                Log.e(GraphService.class.getName(), "cannot parse number " + entryValue, e);
            }
        }

        return result;
    }
}
