package li.klass.fhem.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.RoomListAdapter;
import li.klass.fhem.dataprovider.FHEMService;

import java.util.List;

public class RoomListActivity extends ListActivity {

    private List<String> roomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomList = FHEMService.INSTANCE.getRoomList();
        RoomListAdapter adapter = new RoomListAdapter(this, R.layout.room, roomList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String roomName = roomList.get(position);
        Intent intent = new Intent();
        intent.setClass(this, RoomDetailActivity.class);
        intent.putExtras(new Bundle());
        intent.putExtra("roomName", roomName);

        startActivity(intent);
    }
}
