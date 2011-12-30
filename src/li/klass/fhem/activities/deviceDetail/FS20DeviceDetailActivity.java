package li.klass.fhem.activities.deviceDetail;

import android.app.ProgressDialog;
import android.view.View;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.service.FS20Service;
import li.klass.fhem.service.RoomListService;

public class FS20DeviceDetailActivity extends DeviceDetailActivity<FS20Device> {

    private volatile ProgressDialog progressDialog;

    public void onFS20Click(final View view) {
        String deviceName = (String) view.getTag();
        FS20Device device = RoomListService.INSTANCE.deviceListForAllRooms(false).getDeviceFor(deviceName);
        FS20Service.INSTANCE.toggleState(FS20DeviceDetailActivity.this, device, updateOnSuccessAction);
    }
}
