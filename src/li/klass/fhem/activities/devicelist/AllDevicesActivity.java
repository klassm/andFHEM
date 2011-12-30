package li.klass.fhem.activities.devicelist;

import android.os.Bundle;
import li.klass.fhem.R;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.RoomListService;

public class AllDevicesActivity extends DeviceListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String roomTitle = getResources().getString(R.string.allRoomsTitle);
        setTitle(roomTitle);
    }

    @Override
    protected RoomDeviceList getCurrentData(boolean refresh) {
        return RoomListService.INSTANCE.deviceListForAllRooms(refresh);
    }
}
