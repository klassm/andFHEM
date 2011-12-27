package li.klass.fhem.activities.deviceDetail;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.domain.FS20Device;

public class FS20DeviceDetailActivity extends DeviceDetailActivity<FS20Device> {

    private volatile ProgressDialog progressDialog;

    public void onFS20Click(final View view) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(FS20DeviceDetailActivity.this, "", getResources().getString(R.string.switching));
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String deviceName = (String) view.getTag();
                FS20Device device = FHEMService.INSTANCE.deviceListForAllRooms(false).getDeviceFor(deviceName);
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
