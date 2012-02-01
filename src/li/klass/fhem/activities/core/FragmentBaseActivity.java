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

package li.klass.fhem.activities.core;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.widget.Toast;
import li.klass.fhem.ApplicationUrls;
import li.klass.fhem.R;
import li.klass.fhem.activities.PreferencesActivity;
import li.klass.fhem.activities.base.Updateable;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.AllDevicesFragment;
import li.klass.fhem.fragments.FavoritesFragment;
import li.klass.fhem.fragments.RoomListFragment;
import li.klass.fhem.fragments.core.ActionBarShowTabs;
import li.klass.fhem.fragments.core.TopLevelFragment;
import li.klass.fhem.service.room.RoomListService;

import java.lang.reflect.Constructor;
import java.util.Stack;

import static li.klass.fhem.constants.Actions.*;

public abstract class FragmentBaseActivity extends FragmentActivity implements ActionBar.TabListener, Updateable {
    public static final int DIALOG_UPDATING = 1;
    public static final int DIALOG_EXECUTING = 2;

    private static final int FAVORITES_TAB = 1;
    private static final int ROOM_LIST_TAB = 2;
    private static final int ALL_DEVICES_TAB = 3;

    private Fragment currentFragment;
    private Stack<Fragment> stack = new Stack<Fragment>();

    private Receiver broadcastReceiver;
    private Menu optionsMenu;

    private Handler updateHandler;

    private class Receiver extends BroadcastReceiver {

        private final IntentFilter intentFilter;

        private Receiver() {
            intentFilter = new IntentFilter();
            intentFilter.addAction(Actions.SHOW_FRAGMENT);
            intentFilter.addAction(Actions.DISMISS_UPDATING_DIALOG);
            intentFilter.addAction(Actions.DO_UPDATE);
            intentFilter.addAction(SHOW_EXECUTING_DIALOG);
            intentFilter.addAction(DISMISS_EXECUTING_DIALOG);
            intentFilter.addAction(SHOW_TOAST);
            intentFilter.addAction(DO_UPDATE);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String action = intent.getAction();
                        if (action.equals(Actions.SHOW_FRAGMENT)) {
                            String fragmentName = intent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME);
                            Constructor<?> constructor = Class.forName(fragmentName).getConstructor(Bundle.class);
                            Fragment fragment = (Fragment) constructor.newInstance(intent.getExtras());
                            switchToFragment(fragment);
                        } else if (action.equals(Actions.DISMISS_UPDATING_DIALOG)) {
                            optionsMenu.findItem(R.id.menu_refresh).setVisible(true);
                            optionsMenu.findItem(R.id.menu_refresh_progress).setVisible(false);
                        } else if (action.equals(Actions.DO_UPDATE) && intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false)) {
                            optionsMenu.findItem(R.id.menu_refresh).setVisible(false);
                            optionsMenu.findItem(R.id.menu_refresh_progress).setVisible(true);
                        }
                        else if (action.equals(SHOW_EXECUTING_DIALOG)) {
                            showDialogSafely(FragmentBaseActivity.DIALOG_EXECUTING);
                        } else if (action.equals(DISMISS_EXECUTING_DIALOG)) {
                            dismissDialog(FragmentBaseActivity.DIALOG_EXECUTING);
                        } else if (action.equals(SHOW_TOAST)) {
                            Toast.makeText(FragmentBaseActivity.this, intent.getIntExtra(BundleExtraKeys.TOAST_STRING_ID, 0), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(FragmentBaseActivity.class.getName(), "exception occurred while receiving broadcast", e);
                    }
                }
            });
        }

        public IntentFilter getIntentFilter() {
            return intentFilter;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateHandler = new Handler();
        updateHandler.postDelayed(new Runnable() {
            boolean firstRun = true;
            @Override
            public void run() {
                String updateTime = PreferenceManager.getDefaultSharedPreferences(FragmentBaseActivity.this).getString("AUTO_UPDATE_TIME", "-1");
                Long millis = Long.valueOf(updateTime);

                if (! firstRun && millis != -1) {
                    Intent updateIntent = new Intent(Actions.DO_UPDATE);
                    updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
                    sendBroadcast(updateIntent);

                    Log.d(FragmentBaseActivity.class.getName(), "update");
                }

                if (millis == -1) {
                    millis = 30 * 1000L;
                }
                updateHandler.postDelayed(this, millis);

                firstRun = false;
            }
        }, 0);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);

        ActionBar.Tab favoritesTab = actionBar.newTab()
                .setText(R.string.tab_favorites)
                .setTabListener(this)
                .setTag(FAVORITES_TAB);
        actionBar.addTab(favoritesTab);

        ActionBar.Tab roomListTab = actionBar.newTab()
                .setText(R.string.tab_roomList)
                .setTabListener(this)
                .setTag(ROOM_LIST_TAB);
        actionBar.addTab(roomListTab);

        ActionBar.Tab allDevicesTab = actionBar.newTab()
                .setText(R.string.tab_alldevices)
                .setTabListener(this)
                .setTag(ALL_DEVICES_TAB);
        actionBar.addTab(allDevicesTab);

        actionBar.setDisplayHomeAsUpEnabled(true);

        broadcastReceiver = new Receiver();
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        RoomListService.INSTANCE.storeDeviceListMap();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        Fragment fragment = null;
        switch(Integer.valueOf(tab.getTag().toString())){
            case FAVORITES_TAB:
                fragment = new FavoritesFragment();
                break;
            case ROOM_LIST_TAB:
                fragment = new RoomListFragment();
                break;
            case ALL_DEVICES_TAB:
                fragment = new AllDevicesFragment();
                break;
        }
        switchToFragment(fragment);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        onTabSelected(tab, ft);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.optionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menu_refresh:
                Intent refreshIntent = new Intent(Actions.DO_UPDATE);
                refreshIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
                sendBroadcast(refreshIntent);

                return true;

            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, PreferencesActivity.class);
                startActivity(settingsIntent);

                return true;

            case R.id.menu_help:
                Uri helpUri = Uri.parse(ApplicationUrls.HELP_PAGE);
                Intent helpIntent = new Intent(Intent.ACTION_VIEW, helpUri);
                startActivity(helpIntent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDialogSafely(int dialogId) {
        if (!isFinishing()) {
            showDialog(dialogId);
        }
    }

    @Override
    public void onBackPressed() {
        if (! stack.empty()) {
            Fragment previousFragment = stack.pop();
            switchToFragment(previousFragment, false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);

        switch (id) {
            case DIALOG_UPDATING:
                return ProgressDialog.show(this, "", getResources().getString(R.string.updating));
            case DIALOG_EXECUTING:
                return ProgressDialog.show(this, "", getResources().getString(R.string.executing));
        }
        return null;
    }


    /**
     * Shows the given fragment in the main content section.
     * @param fragment fragment to show.
     */
    private void switchToFragment(Fragment fragment) {
        switchToFragment(fragment, true);
    }

    /**
     * Shows the given fragment in the main content section.
     * @param fragment fragment to show
     * @param putToStack put the fragment to history. Usually true, except when back is pressed (history)
     */
    private void switchToFragment(Fragment fragment, boolean putToStack) {
        if (fragment != null) {
            if (currentFragment != null && putToStack) stack.add(currentFragment);

            int navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
            if (fragment instanceof ActionBarShowTabs) {
                navigationMode = ActionBar.NAVIGATION_MODE_TABS;
            }
            if (fragment instanceof TopLevelFragment) {
                stack.clear();
            }

            getSupportActionBar().setNavigationMode(navigationMode);

            currentFragment = fragment;

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
    }
}
