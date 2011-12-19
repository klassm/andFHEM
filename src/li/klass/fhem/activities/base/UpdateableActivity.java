package li.klass.fhem.activities.base;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;

public abstract class UpdateableActivity<T> extends Activity {
    public static final int DIALOG_UPDATE = 1;
    protected Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateData(currentData);
        }
    };
    protected T currentData;

    @SuppressWarnings("unchecked")
    public void update(boolean refresh) {
        showDialog(DIALOG_UPDATE);
        new UpdateAction(refresh).execute();
    }

    protected void updateContent(boolean refresh) {
        currentData = getCurrentData(refresh);
        updateHandler.sendMessage(Message.obtain());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_UPDATE:
                return ProgressDialog.show(this, "", getResources().getString(R.string.updating), false, true);
            default:
                return null;
        }
    }

    protected abstract T getCurrentData(boolean refresh);

    protected abstract void updateData(T data);

    class UpdateAction extends AsyncTask<Void, Void, Void> {

        private boolean refresh;
        private volatile Throwable occurredException = null;

        public UpdateAction(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                UpdateableActivity.this.updateContent(refresh);
            } catch (Exception e) {
                occurredException = e;
                Log.e(BaseActivity.class.getName(), "an error occurred while updating", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            UpdateableActivity.this.dismissDialog(DIALOG_UPDATE);
            if (occurredException != null) {
                int messageId = R.string.updateError;
                if (occurredException instanceof HostConnectionException) {
                    messageId = R.string.updateErrorHostConnection;
                } else if (occurredException instanceof DeviceListParseException) {
                    messageId = R.string.updateErrorDeviceListParse;
                }
                Toast.makeText(UpdateableActivity.this, messageId, Toast.LENGTH_LONG).show();
            }
        }
    }
}
