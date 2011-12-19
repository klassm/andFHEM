package li.klass.fhem.activities.devicelist;

import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.R;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.domain.RoomDeviceList;

public class RoomDetailActivity extends DeviceListActivity {

    private String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        roomName = extras.getString("roomName");

        String roomTitlePrefix = getResources().getString(R.string.roomTitlePrefix);
        setTitle(roomTitlePrefix + " " + roomName);
    }

    @Override
    protected RoomDeviceList getCurrentData(boolean refresh) {
        return FHEMService.INSTANCE.deviceListForRoom(roomName, refresh);
    }
}
