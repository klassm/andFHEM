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

package li.klass.fhem.activities.base;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Toast;
import li.klass.fhem.ApplicationUrls;
import li.klass.fhem.R;
import li.klass.fhem.activities.CurrentActivityProvider;
import li.klass.fhem.activities.PreferencesActivity;
import li.klass.fhem.service.favorites.FavoritesService;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.domain.Device;
import li.klass.fhem.service.ExecuteOnSuccess;
import li.klass.fhem.util.device.DeviceActionUtil;

public abstract class BaseActivity<ADAPTER> extends Activity implements Updateable {
    public static final int OPTION_UPDATE = 1;
    public static final int OPTION_PREFERENCES = 2;
    public static final int OPTION_HELP = 3;
    public static final int OPTION_DONATE = 4;

    public static final int CONTEXT_MENU_FAVORITES_ADD = 1;
    public static final int CONTEXT_MENU_FAVORITES_DELETE = 2;
    public static final int CONTEXT_MENU_RENAME = 3;
    public static final int CONTEXT_MENU_DELETE = 4;
    public static final int CONTEXT_MENU_MOVE = 5;
    public static final int CONTEXT_MENU_ALIAS = 6;

    protected Device contextMenuClickedDevice;
    private long backPressStart;
    protected ADAPTER adapter;

    protected ExecuteOnSuccess updateOnSuccessAction = new ExecuteOnSuccess() {
        @Override
        public void onSuccess() {
            update(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CurrentActivityProvider.INSTANCE.setCurrentActivity(this);

        setLayout();
        adapter = initializeLayoutAndReturnAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem updateItem = menu.add(0, OPTION_UPDATE, 0, getResources().getString(R.string.update));
        updateItem.setIcon(R.drawable.ic_menu_refresh);

        MenuItem preferencesItem = menu.add(0, OPTION_PREFERENCES, 0, getResources().getString(R.string.preferences));
        preferencesItem.setIcon(android.R.drawable.ic_menu_preferences);

        MenuItem helpItem = menu.add(0, OPTION_HELP, 0, getResources().getString(R.string.help));
        helpItem.setIcon(android.R.drawable.ic_menu_help);

        MenuItem donateItem = menu.add(0, OPTION_DONATE, 0, getResources().getString(R.string.donate));
        donateItem.setIcon(android.R.drawable.ic_menu_agenda);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int itemId = item.getItemId();

        switch (itemId) {
            case OPTION_UPDATE:
                update(true);
                break;
            case OPTION_PREFERENCES:
                Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferencesIntent);
                break;
            case OPTION_HELP:
                Uri helpUri = Uri.parse(ApplicationUrls.HELP_PAGE);
                Intent helpIntent = new Intent(Intent.ACTION_VIEW, helpUri);
                startActivity(helpIntent);
                break;
            case OPTION_DONATE:
                Uri donateUri = Uri.parse(ApplicationUrls.DONATE_PAGE);
                Intent donateIntent = new Intent(Intent.ACTION_VIEW, donateUri);
                startActivity(donateIntent);
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        CurrentActivityProvider.INSTANCE.setCurrentActivity(this);
        update(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        RoomListService.INSTANCE.storeDeviceListMap();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object tag = info.targetView.getTag();

        if (tag == null) return;
        if (tag instanceof Device) {
            contextMenuClickedDevice = (Device) tag;
            Resources resources = getResources();
            menu.add(0, CONTEXT_MENU_FAVORITES_ADD, 0, resources.getString(R.string.context_addtofavorites));
            menu.add(0, CONTEXT_MENU_RENAME, 0, resources.getString(R.string.context_rename));
            menu.add(0, CONTEXT_MENU_DELETE, 0, resources.getString(R.string.context_delete));
            menu.add(0, CONTEXT_MENU_MOVE, 0, resources.getString(R.string.context_move));
            menu.add(0, CONTEXT_MENU_ALIAS, 0, resources.getString(R.string.context_alias));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getRepeatCount() == 0) {
                backPressStart = System.currentTimeMillis();
                Log.d(BaseActivity.class.getName(), "back press start " + backPressStart);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long diff = System.currentTimeMillis() - backPressStart;
            Log.d(BaseActivity.class.getName(), "back press up " + diff);
            if (diff < 200) {
                super.onBackPressed();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case CONTEXT_MENU_FAVORITES_ADD:
                FavoritesService.INSTANCE.addFavorite(contextMenuClickedDevice);
                Toast.makeText(this, R.string.context_favoriteadded, Toast.LENGTH_SHORT).show();
                return true;
            case CONTEXT_MENU_RENAME:
                DeviceActionUtil.renameDevice(this, contextMenuClickedDevice);
                return true;
            case CONTEXT_MENU_DELETE:
                DeviceActionUtil.deleteDevice(this, contextMenuClickedDevice);
                return true;
            case CONTEXT_MENU_MOVE:
                DeviceActionUtil.moveDevice(this, contextMenuClickedDevice);
                return true;
            case CONTEXT_MENU_ALIAS:
                DeviceActionUtil.setAlias(this, contextMenuClickedDevice);
                return true;
        }
        return false;
    }

    protected abstract ADAPTER initializeLayoutAndReturnAdapter();
    protected abstract void setLayout();

}
