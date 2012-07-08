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
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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

package li.klass.fhem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.core.ActionBarShowTabs;
import li.klass.fhem.fragments.core.DeviceListFragment;
import li.klass.fhem.fragments.core.TopLevelFragment;

public class FavoritesFragment extends DeviceListFragment implements TopLevelFragment, ActionBarShowTabs {

    @SuppressWarnings("unused")
    public FavoritesFragment(Bundle bundle) {
        super(bundle);
    }

    @SuppressWarnings("unused")
    public FavoritesFragment() {}

    @Override
    protected String getUpdateAction() {
        return Actions.FAVORITE_ROOM_LIST;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object tag = info.targetView.getTag();

        if (tag == null) return;
        if (tag instanceof Device) {
            menu.removeItem(CONTEXT_MENU_FAVORITES_ADD);
            menu.add(0, CONTEXT_MENU_FAVORITES_DELETE, 0, R.string.context_removefavorite);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (! super.onContextItemSelected(item)) {
            switch (item.getItemId()) {
                case CONTEXT_MENU_FAVORITES_DELETE:
                    Intent favoriteRemoveIntent = new Intent(Actions.FAVORITE_REMOVE);
                    favoriteRemoveIntent.putExtra(BundleExtraKeys.DEVICE, contextMenuClickedDevice);
                    favoriteRemoveIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            Toast.makeText(getActivity(), R.string.context_favoriteremoved, Toast.LENGTH_SHORT).show();
                            update(false);
                        }
                    });
                    getActivity().startService(favoriteRemoveIntent);
                    return true;
            }
        }
        return false;
    }
}
