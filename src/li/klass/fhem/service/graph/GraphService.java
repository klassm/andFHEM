package li.klass.fhem.service.graph;

import android.content.Context;
import android.util.Log;
import li.klass.fhem.domain.Device;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.service.ExecuteOnSuccess;
import li.klass.fhem.service.UpdateDialogAsyncTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphService {
    public static final GraphService INSTANCE = new GraphService();

    private GraphService() {
    }

    public void getGraphData(Context context, final Device device, final List<String> columnSpecifications, final Calendar startDate, final Calendar endDate, final GraphDataReceivedListener listener) {
        if (device.getFileLog() == null) return;

        Map<String, List<GraphEntry>> data = new HashMap<String, List<GraphEntry>>();
        final AtomicReference<Map<String, List<GraphEntry>>> dataReference = new AtomicReference<Map<String, List<GraphEntry>>>(data);

        ExecuteOnSuccess executeOnSuccess = new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                listener.graphDataReceived(dataReference.get());
            }
        };

        new UpdateDialogAsyncTask(context, executeOnSuccess) {

            @Override
            protected void executeCommand() {
                for (String columnSpec : columnSpecifications) {
                    String fileLogDeviceName = device.getFileLog().getName();
                    List<GraphEntry> valueEntries = getCurrentGraphEntriesFor(fileLogDeviceName, columnSpec, startDate, endDate);
                    dataReference.get().put(columnSpec, valueEntries);
                }

            }
        }.executeTask();
    }

    public List<GraphEntry> getCurrentGraphEntriesFor(String fileLogName, String columnSpec, Calendar startDate, Calendar endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

        String startDateFormat = dateFormat.format(startDate.getTime());
        String endDateFormat = dateFormat.format(endDate.getTime());

        String result = DataConnectionSwitch.INSTANCE.getCurrentProvider().fileLogData(fileLogName, startDate.getTime(), endDate.getTime(), columnSpec);
        result = result.replace("#" + columnSpec, "");

        return findGraphEntries(result);
    }

    private List<GraphEntry> findGraphEntries(String content) {
        Pattern pattern = Pattern.compile("([\\d]{4}-[\\d]{2}-[\\d]{2}_[\\d]{2}:[\\d]{2}:[\\d]{2}) ([\\d\\.]+(?=[\\d]{4}))");
        Matcher matcher = pattern.matcher(content);

        List<GraphEntry> result = new ArrayList<GraphEntry>();
        SimpleDateFormat providedDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

        while(matcher.find()) {
            String entryTime = matcher.group(1);
            String entryValue = matcher.group(2);

            try {

                result.add(new GraphEntry(providedDateFormat.parse(entryTime), Float.valueOf(entryValue)));
            } catch (ParseException e) {
                Log.e(GraphService.class.getName(), "cannot parse date " + entryTime, e);
            } catch (NumberFormatException e) {
                Log.e(GraphService.class.getName(), "cannot parse number " + entryValue, e);
            }
        }

        return result;
    }

}
