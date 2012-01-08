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
            Log.e(UpdateDialogAsyncTask.class.getName(), "error occurred while updating", exception);
            int messageId;
            if (exception instanceof HostConnectionException) {
                messageId = R.string.updateErrorHostConnection;
            } else if (exception instanceof DeviceListParseException) {
                messageId = R.string.updateErrorDeviceListParse;
            } else {
                throw new RuntimeException(exception);
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