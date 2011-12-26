package li.klass.fhem.activities.deviceDetail;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.domain.FS20Device;

public class FS20DeviceDetailActivity extends DeviceDetailActivity<FS20Device> {

    private ProgressDialog progressDialog;

    public void onFS20Click(final View view) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(FS20DeviceDetailActivity.this, "", getResources().getString(R.string.switching));
            }

            @Override
            protected Void doInBackground(Void... voids) {
                FS20Device device = (FS20Device) view.getTag();
                device.toggleState();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                update();
                progressDialog.dismiss();
            }
        }.execute(null);

    }
}
