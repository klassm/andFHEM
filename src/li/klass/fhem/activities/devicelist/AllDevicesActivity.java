package li.klass.fhem.activities.devicelist;

import android.os.Bundle;
import li.klass.fhem.R;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

public class AllDevicesActivity extends DeviceListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String roomTitle = getResources().getString(R.string.allRoomsTitle);
        setTitle(roomTitle);
    }

    @Override
    public void update(boolean doUpdate) {
        RoomListService.INSTANCE.getAllRoomsDeviceList(this, doUpdate, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                adapter.updateData(roomDeviceList);
            }
        });
    }
}
