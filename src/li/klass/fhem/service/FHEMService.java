package li.klass.fhem.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.data.DataProviderSwitch;
import li.klass.fhem.data.provider.graph.GraphEntry;
import li.klass.fhem.data.provider.graph.GraphProvider;
import li.klass.fhem.domain.Device;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FHEMService {
    public static final FHEMService INSTANCE = new FHEMService();

    private FHEMService() {}

    public void executeCommand(String command) {
        DataProviderSwitch.INSTANCE.getCurrentProvider().executeCommand(command);
    }

    public void executeSafely(Context context, String command, ExecuteOnSuccess executeOnSuccess) {
        new ExecuteTask(context, executeOnSuccess).execute(command);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<GraphEntry>> getGraphData(Device device, List<String> columnSpecifications) {
        if (device.getFileLog() == null) return null;

        Map<String, List<GraphEntry>> data = new HashMap<String, List<GraphEntry>>();

        GraphProvider graphProvider = GraphProvider.INSTANCE;
        for (String columnSpec : columnSpecifications) {
            String fileLogDeviceName = device.getFileLog().getName();
            List<GraphEntry> valueEntries = graphProvider.getCurrentGraphEntriesFor(fileLogDeviceName, columnSpec);
            data.put(columnSpec, valueEntries);
        }

        return data;
    }

    class ExecuteTask extends AsyncTask<String, Void, Exception> {

        private volatile Throwable occurredException = null;
        private Context context;

        private ProgressDialog progressDialog;
        private ExecuteOnSuccess executeOnSuccess;


        public ExecuteTask(Context context, ExecuteOnSuccess executeOnSuccess) {
            this.context = context;
            this.executeOnSuccess = executeOnSuccess;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (context != null) {
                progressDialog = ProgressDialog.show(context, "", context.getResources().getString(R.string.executing));
            }
        }

        @Override
        protected Exception doInBackground(String... params) {
            String command = params[0];
            try {
                executeCommand(command);

                return null;
            } catch(Exception e) {
                Log.i(FHEMService.class.getName(), "an error occurred while executing command " + command, e);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception exception) {
            super.onPostExecute(exception);
            if (exception != null) {
                int messageId = R.string.updateError;
                if (exception instanceof HostConnectionException) {
                    messageId = R.string.updateErrorHostConnection;
                } else if (occurredException instanceof DeviceListParseException) {
                    messageId = R.string.updateErrorDeviceListParse;
                }
                Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
            } else if (executeOnSuccess != null) {
                executeOnSuccess.onSuccess();
            }

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}
