package li.klass.fhem.activities.devicelist;

import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.R;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

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
    public void update(boolean doUpdate) {
        RoomListService.INSTANCE.getRoomDeviceList(this, roomName, doUpdate, new RoomDeviceListListener() {

            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                adapter.updateData(roomDeviceList);
            }
        });
    }
}
