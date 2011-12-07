package li.klass.fhem.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.R;
import li.klass.fhem.adapter.RoomDetailAdapter;
import li.klass.fhem.dataprovider.FHEMService;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.widget.NestedListView;

public class AllDevicesActivity extends UpdateableActivity {

    private RoomDetailAdapter roomDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.room_detail);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String roomTitle = getResources().getString(R.string.allRoomsTitle);
        setTitle(roomTitle);

        roomDetailAdapter = new RoomDetailAdapter(this, new RoomDeviceList("all"));
        NestedListView nestedListView = (NestedListView) findViewById(R.id.deviceMap);
        nestedListView.setAdapter(roomDetailAdapter);

        update(false);
    }

    @Override
    protected RoomDeviceList getCurrentRoomDeviceList(boolean refresh) {
        return FHEMService.INSTANCE.deviceListForAllRooms(refresh);
    }

    @Override
    protected void updateData(RoomDeviceList roomDeviceList) {
        roomDetailAdapter.updateData(roomDeviceList);
    }
}
