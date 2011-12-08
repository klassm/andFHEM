package li.klass.fhem.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.adapter.RoomDetailAdapter;
import li.klass.fhem.dataprovider.FavoritesService;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.widget.NestedListView;

public class FavoritesActivity extends UpdateableActivity {

    private RoomDetailAdapter roomDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.room_detail);

        String roomTitle = getResources().getString(R.string.allRoomsTitle);
        setTitle(roomTitle);

        roomDetailAdapter = new RoomDetailAdapter(this, new RoomDeviceList("favorites"));
        NestedListView nestedListView = (NestedListView) findViewById(R.id.deviceMap);
        nestedListView.setAdapter(roomDetailAdapter);

        registerForContextMenu(nestedListView);

        update(false);
    }

    @Override
    protected RoomDeviceList getCurrentRoomDeviceList(boolean refresh) {
        return FavoritesService.INSTANCE.getFavorites();
    }

    @Override
    protected void updateData(RoomDeviceList roomDeviceList) {
        roomDetailAdapter.updateData(roomDeviceList);
    }

        @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object tag = info.targetView.getTag();

        if (tag == null) return;
        if (tag instanceof Device) {
            contextMenuClickedDevice = (Device) tag;
            Resources resources = getResources();
            menu.add(0, CONTEXT_MENU_FAVORITES_DELETE, 0, resources.getString(R.string.context_removefavorite));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case CONTEXT_MENU_FAVORITES_DELETE:
                FavoritesService.INSTANCE.removeFavorite(contextMenuClickedDevice);
                update(false);
                Toast.makeText(this, R.string.context_favoriteremoved, Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }
}
