package li.klass.fhem.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;

/**
 * Represents an {@link AsyncTask}, but shows an update dialog during execution.
 */
public abstract class UpdateDialogAsyncTask extends AsyncTask<Void, Void, Exception> {

    private volatile Throwable occurredException = null;
    private Context context;

    private volatile ProgressDialog progressDialog;
    private ExecuteOnSuccess executeOnSuccess;


    public UpdateDialogAsyncTask(Context context, ExecuteOnSuccess executeOnSuccess) {
        this.context = context;
        this.executeOnSuccess = executeOnSuccess;
    }

    @SuppressWarnings("unchecked")
    public void executeTask() {
        execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (context != null) {
            progressDialog = ProgressDialog.show(context, "", context.getResources().getString(getExecuteDialogMessage()));
        }
    }

    @Override
    protected Exception doInBackground(Void... params) {
        try {
            executeCommand();

            return null;
        } catch(Exception e) {
            Log.i(CommandExecutionService.class.getName(), "an error occurred while executing command " + e);
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
    
    protected int getExecuteDialogMessage() {
        return R.string.executing;
    }

    protected abstract void executeCommand();
}