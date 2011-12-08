package li.klass.fhem.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.RoomDetailAdapter;
import li.klass.fhem.dataprovider.FHEMService;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.widget.NestedListView;

public class RoomDetailActivity extends UpdateableActivity {

    private String roomName;
    private RoomDetailAdapter roomDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.room_detail);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        roomName = extras.getString("roomName");

        String roomTitlePrefix = getResources().getString(R.string.roomTitlePrefix);
        setTitle(roomTitlePrefix + " " + roomName);

        roomDetailAdapter = new RoomDetailAdapter(this, new RoomDeviceList(roomName));
        NestedListView nestedListView = (NestedListView) findViewById(R.id.deviceMap);
        nestedListView.setAdapter(roomDetailAdapter);

        registerForContextMenu(nestedListView);

        update(false);

    }

    @Override
    protected RoomDeviceList getCurrentRoomDeviceList(boolean refresh) {
        return FHEMService.INSTANCE.deviceListForRoom(roomName, refresh);
    }

    @Override
    protected void updateData(RoomDeviceList roomDeviceList) {
        roomDetailAdapter.updateData(roomDeviceList);
    }
}
