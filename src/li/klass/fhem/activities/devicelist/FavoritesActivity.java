/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.activities.devicelist;

import android.content.res.Resources;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.favorites.FavoritesService;
import li.klass.fhem.service.room.RoomDeviceListListener;

public class FavoritesActivity extends DeviceListActivity {

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

    @Override
    public void update(boolean doUpdate) {
        FavoritesService.INSTANCE.getFavorites(this, doUpdate, new RoomDeviceListListener() {

            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                adapter.updateData(roomDeviceList);
            }
        });
    }
}
