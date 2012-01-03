package li.klass.fhem.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.activities.devicelist.RoomDetailActivity;
import li.klass.fhem.adapter.rooms.RoomListAdapter;
import li.klass.fhem.service.room.RoomListListener;
import li.klass.fhem.service.room.RoomListService;

import java.util.ArrayList;
import java.util.List;

public class RoomListActivity extends BaseActivity<RoomListAdapter> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        update(false);
    }

    @Override
    protected RoomListAdapter initializeLayoutAndReturnAdapter() {
        ListView roomList = (ListView) findViewById(R.id.roomList);

        RoomListAdapter adapter = new RoomListAdapter(this, R.layout.room_list_name, new ArrayList<String>());
        roomList.setAdapter(adapter);

        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String roomName = String.valueOf(view.getTag());
                Intent intent = new Intent();
                intent.setClass(RoomListActivity.this, RoomDetailActivity.class);
                intent.putExtras(new Bundle());
                intent.putExtra("roomName", roomName);

                startActivity(intent);
            }
        });

        return adapter;
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.room_list);
    }

    @Override
    public void update(boolean doUpdate) {
        RoomListService.INSTANCE.getRoomList(this, doUpdate, new RoomListListener() {

            @Override
            public void onRoomListRefresh(List<String> rooms) {
                adapter.updateData(rooms);
            }
        });
    }
}
