package li.klass.fhem.activities.deviceDetail;

import android.view.View;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.device.FS20Service;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

public class FS20DeviceDetailActivity extends DeviceDetailActivity<FS20Device> {

    public void onFS20Click(final View view) {

        RoomListService.INSTANCE.getAllRoomsDeviceList(this, false, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                String deviceName = (String) view.getTag();
                FS20Device device = roomDeviceList.getDeviceFor(deviceName);
                FS20Service.INSTANCE.toggleState(FS20DeviceDetailActivity.this, device, updateOnSuccessAction);
            }
        });

    }
}
