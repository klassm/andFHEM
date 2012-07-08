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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import li.klass.fhem.ApplicationUrls;
import li.klass.fhem.R;
import li.klass.fhem.activities.PreferencesActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.*;
import li.klass.fhem.fragments.core.ActionBarShowTabs;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.core.TopLevelFragment;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.DialogUtil;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static li.klass.fhem.constants.Actions.*;

public abstract class FragmentBaseActivity extends SherlockFragmentActivity implements ActionBar.TabListener, Updateable {
    static class FragmentHistoryStackEntry implements Serializable {
        FragmentHistoryStackEntry(BaseFragment navigationFragment, BaseFragment contentFragment) {
            this.navigationFragment = navigationFragment;
            this.contentFragment = contentFragment;
        }

        private BaseFragment navigationFragment;
        private BaseFragment contentFragment;
    }

    private FragmentHistoryStackEntry currentHistoryStackEntry = null;
    private ArrayList<FragmentHistoryStackEntry> fragmentHistoryStack = new ArrayList<FragmentHistoryStackEntry>();

    private Receiver broadcastReceiver;
    private Menu optionsMenu;

    // an intent waiting to be processed, but received in the wrong activity state (widget problem ..)
    private Intent waitingIntent;

    private Handler autoUpdateHandler;
    private final Runnable autoUpdateCallback = new Runnable() {
        boolean firstRun = true;

        @Override
        public void run() {
            String updateTime = PreferenceManager.getDefaultSharedPreferences(FragmentBaseActivity.this).getString("AUTO_UPDATE_TIME", "-1");
            Long millis = Long.valueOf(updateTime);

            if (!firstRun && millis != -1) {
                Intent updateIntent = new Intent(Actions.DO_UPDATE);
                updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
                sendBroadcast(updateIntent);

                Log.d(FragmentBaseActivity.class.getName(), "update");
            }

            if (millis == -1) {
                millis = 30 * 1000L;
            }
            autoUpdateHandler.postDelayed(this, millis);

            firstRun = false;
        }
    };
    private boolean saveInstanceStateCalled = false;

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
            intentFilter.addAction(BACK);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String action = intent.getAction();
                        if (Actions.SHOW_FRAGMENT.equals(action)) {
                            String fragmentName = intent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME);
                            switchToFragment(FragmentType.getFragmentFor(fragmentName), intent.getExtras());
                        } else if (action.equals(Actions.DISMISS_UPDATING_DIALOG)) {
                            setShowRefreshProgressIcon(false);
                        } else if (intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false) && action.equals(Actions.DO_UPDATE)) {
                            setShowRefreshProgressIcon(true);
                        } else if (action.equals(SHOW_EXECUTING_DIALOG)) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(BundleExtraKeys.CONTENT, R.string.executing);
                            showDialog(bundle);
                        } else if (action.equals(DISMISS_EXECUTING_DIALOG)) {
                            removeDialog();
                        } else if (action.equals(SHOW_TOAST)) {
                            Toast.makeText(FragmentBaseActivity.this, intent.getIntExtra(BundleExtraKeys.TOAST_STRING_ID, 0), Toast.LENGTH_SHORT).show();
                        } else if (action.equals(BACK)) {
                            onBackPressed(intent.getExtras());
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
        setContentView(R.layout.main_view);

        saveInstanceStateCalled = false;

        autoUpdateHandler = new Handler();
        autoUpdateHandler.postDelayed(autoUpdateCallback, 0);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);

        for (FragmentType fragmentType : FragmentType.getTopLevelFragments()) {
            ActionBar.Tab tab = actionBar.newTab()
                    .setText(fragmentType.getTopLevelTabName())
                    .setTabListener(this)
                    .setTag(fragmentType);
            actionBar.addTab(tab);
        }

        actionBar.setDisplayHomeAsUpEnabled(true);

        broadcastReceiver = new Receiver();
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());

        boolean restoreResult = false;
        if (savedInstanceState != null) {
            restoreResult = restoreSavedInstance(savedInstanceState, actionBar);
        }

        if (savedInstanceState == null || ! restoreResult)  {
            switchToFragment(FragmentType.FAVORITES, new Bundle());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // we're in the wrong lifecycle (activity is not yet resumed)
        // save the intent intermediately and process it in onPostResume();
        waitingIntent = intent;
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        // process the intent received in onNewIntent()
        if (waitingIntent == null) {
            return;
        }
        String fragmentName = waitingIntent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME);
        if (fragmentName != null) {
            try {
                switchToFragment(FragmentType.getFragmentFor(fragmentName), waitingIntent.getExtras());
            } catch (Exception e) {
                Log.e(FragmentBaseActivity.class.getName(), "error while creating fragment", e);
            }
        }

        waitingIntent = null;
    }

    @SuppressWarnings("unchecked")
    private boolean restoreSavedInstance(Bundle savedInstanceState, ActionBar actionBar) {
        try {
            ArrayList<FragmentHistoryStackEntry> previousFragmentStack =
                    (ArrayList<FragmentHistoryStackEntry>) savedInstanceState.getSerializable(BundleExtraKeys.FRAGMENT_HISTORY_STACK);

            if (previousFragmentStack != null) {
                removeLastHistoryFragmentEntry();
                fragmentHistoryStack = previousFragmentStack;
            }

            if (savedInstanceState.containsKey(BundleExtraKeys.CURRENT_TAB)) {
                actionBar.setSelectedNavigationItem(savedInstanceState.getInt(BundleExtraKeys.CURRENT_TAB));
            }

            FragmentHistoryStackEntry historyEntry = (FragmentHistoryStackEntry) savedInstanceState.getSerializable(BundleExtraKeys.CURRENT_FRAGMENT);
            if (historyEntry != null) {
                switchToFragment(historyEntry, false);
                return true;
            } else {
                return false;
            }
        } catch(Exception e) {
            Log.i(FragmentBaseActivity.class.getName(), "error occurred while restoring instance", e);
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveInstanceStateCalled = false;
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        RoomListService.INSTANCE.storeDeviceListMap();
        unregisterReceiver(broadcastReceiver);

        autoUpdateHandler.removeCallbacks(autoUpdateCallback);
        setShowRefreshProgressIcon(false);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        Object tag = tab.getTag();
        if (! (tag instanceof FragmentType)) {
            Log.e(FragmentBaseActivity.class.getName(), "can only switch tabs including a Fragment as tag");
            return;
        }

        FragmentType fragmentTypeTag = (FragmentType) tag;
        Intent intent = new Intent(Actions.SHOW_FRAGMENT);
        intent.putExtra(BundleExtraKeys.FRAGMENT_NAME, fragmentTypeTag.getFragmentClass().getName());
        sendBroadcast(intent);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        onTabSelected(tab, ft);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, com.actionbarsherlock.view.Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
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

            case R.id.menu_premium:
                Intent premiumIntent = new Intent(Actions.SHOW_FRAGMENT);
                premiumIntent.putExtra(BundleExtraKeys.FRAGMENT_NAME, PremiumFragment.class.getName());
                sendBroadcast(premiumIntent);

                return true;

            case R.id.menu_command:
                Intent commandIntent = new Intent(Actions.SHOW_FRAGMENT);
                commandIntent.putExtra(BundleExtraKeys.FRAGMENT_NAME, SendCommandFragment.class.getName());
                sendBroadcast(commandIntent);

                return true;

            case R.id.menu_conversion:
                Intent conversion = new Intent(Actions.SHOW_FRAGMENT);
                conversion.putExtra(BundleExtraKeys.FRAGMENT_NAME, ConversionFragment.class.getName());
                sendBroadcast(conversion);

                return true;

            case R.id.menu_timer:
                if (Build.VERSION.SDK_INT < 11) {
                    String text = String.format(getString(R.string.feature_requires_android_version), 3);
                    DialogUtil.showAlertDialog(this, R.string.android_version, text);
                    return true;
                }
                Intent timer = new Intent(Actions.SHOW_FRAGMENT);
                timer.putExtra(BundleExtraKeys.FRAGMENT_NAME, TimerFragment.class.getName());
                sendBroadcast(timer);

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onBackPressed(null);
    }

    private void onBackPressed(Bundle data) {
        removeDialog();
        FragmentHistoryStackEntry previousEntry = removeLastHistoryFragmentEntry();

        if (previousEntry != null) {
            switchToFragment(previousEntry, false);
            previousEntry.contentFragment.onBackPressResult(data);
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        this.saveInstanceStateCalled = true;
        outState.putSerializable(BundleExtraKeys.CURRENT_FRAGMENT, currentHistoryStackEntry);
        outState.putSerializable(BundleExtraKeys.FRAGMENT_HISTORY_STACK, fragmentHistoryStack);

        if (getSupportActionBar().getSelectedTab() != null) {
            outState.putInt(BundleExtraKeys.CURRENT_TAB, getSupportActionBar().getSelectedTab().getPosition());
        }

        super.onSaveInstanceState(outState);
    }

    private void switchToFragment(FragmentType fragmentType, Bundle data) {
        BaseFragment contentFragment = createContentFragment(fragmentType, data);
        if (contentFragment == null) {
            Log.e(FragmentBaseActivity.class.getName(), "cannot switch to fragment, as createContentFragment() returned null");
        }

        FragmentHistoryStackEntry lastHistoryFragmentEntry = currentHistoryStackEntry;
        BaseFragment navigationFragment = createNavigationFragment(contentFragment, lastHistoryFragmentEntry);

        boolean addToStack = data.getBoolean(BundleExtraKeys.FRAGMENT_ADD_TO_STACK, true);
        switchToFragment(new FragmentHistoryStackEntry(navigationFragment, contentFragment), addToStack);
    }


    private BaseFragment createContentFragment(FragmentType fragmentType, Bundle data) {
        try {
            Constructor<? extends BaseFragment> constructor = fragmentType.getFragmentClass().getConstructor(Bundle.class);
            return (BaseFragment) constructor.newInstance(data);
        } catch (Exception e) {
            Log.e(FragmentBaseActivity.class.getName(), "cannot instantiate fragment", e);
            return null;
        }
    }


    private BaseFragment createNavigationFragment(BaseFragment contentFragment, FragmentHistoryStackEntry lastEntry) {
        if (contentFragment == null) {
            return null;
        }
        View navigationView = findViewById(R.id.navigation);
        boolean isTablet = navigationView != null;

        if (! isTablet) return null;
        FragmentType currentFragmentType = FragmentType.getFragmentFor(contentFragment.getClass());
        Class<? extends BaseFragment> navigationFragmentClass = currentFragmentType.getNavigationFragment();
        if (navigationFragmentClass == null) {
            return null;
        }

        Bundle bundle = new Bundle();
        if (lastEntry != null && ! (contentFragment instanceof TopLevelFragment)) {
            BaseFragment lastContent = lastEntry.contentFragment;
            BaseFragment lastNavigation = lastEntry.navigationFragment;

            if (lastContent != null) {
                Bundle creationAttributes = lastContent.getCreationAttributesAsBundle();
                if (creationAttributes != null) bundle.putAll(creationAttributes);
            }
            if (lastNavigation != null) {
                Bundle creationAttributes = lastNavigation.getCreationAttributesAsBundle();
                if (creationAttributes != null) bundle.putAll(creationAttributes);
            }
        }

        navigationView.setVisibility(View.VISIBLE);
        try {
            Constructor<? extends BaseFragment> constructor = navigationFragmentClass.getConstructor(Bundle.class);
            return constructor.newInstance(bundle);
        } catch (Exception e) {
            Log.e(FragmentBaseActivity.class.getName(), "cannot instantiate navigation fragment", e);
            return null;
        }
    }

    private void switchToFragment(FragmentHistoryStackEntry toSwitchToEntry, boolean putToStack) {
        removeDialog();

        if (currentHistoryStackEntry != null && currentHistoryStackEntry.contentFragment.getClass().equals(toSwitchToEntry.contentFragment.getClass()) &&
                currentHistoryStackEntry.contentFragment.getCreationAttributesAsBundle().equals(toSwitchToEntry.contentFragment.getCreationAttributesAsBundle())) {
            return;
        }

        if (toSwitchToEntry.contentFragment instanceof TopLevelFragment) {
            fragmentHistoryStack.clear();
        }

        if (putToStack) {
            if (fragmentHistoryStack.size() > 10) fragmentHistoryStack.remove(0);
            fragmentHistoryStack.add(currentHistoryStackEntry);
        }
        currentHistoryStackEntry = toSwitchToEntry;

        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, toSwitchToEntry.contentFragment)
                    .commitAllowingStateLoss();
            setNavigationFragment(toSwitchToEntry);
            handleNavigationChanges(toSwitchToEntry);
        } catch (IllegalStateException e) {
            Log.e(FragmentBaseActivity.class.getName(), "error while switching to fragment " + toSwitchToEntry.contentFragment.getClass().getName(), e);
        }
    }

    private void setNavigationFragment(FragmentHistoryStackEntry entry) {
        View navigationView = findViewById(R.id.navigation);
        if (navigationView == null) return;

        BaseFragment navigationFragment = entry.navigationFragment;
        if (navigationFragment == null) {
            navigationView.setVisibility(View.GONE);
            return;
        }
        navigationView.setVisibility(View.VISIBLE);

        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.navigation, navigationFragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(FragmentBaseActivity.class.getName(), "cannot instantiate navigation fragment", e);
        }
    }

    private void handleNavigationChanges(FragmentHistoryStackEntry entry) {
        boolean isTablet = findViewById(R.id.navigation) != null;
        int navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
        if ((entry.contentFragment instanceof ActionBarShowTabs) || isTablet) {
            navigationMode = ActionBar.NAVIGATION_MODE_TABS;
        }

        if (getSupportActionBar().getNavigationMode() != navigationMode) {
            getSupportActionBar().setNavigationMode(navigationMode);
        }

        FragmentType currentFragmentType = FragmentType.getFragmentFor(entry.contentFragment.getClass());
        if (currentFragmentType == null) return;

        if (currentFragmentType.isTopLevelFragment() && currentFragmentType.getTopLevelPosition() != -1) {
            int topLevelPosition = currentFragmentType.getTopLevelPosition();
            ActionBar.Tab tab = getSupportActionBar().getTabAt(topLevelPosition);
            if (tab != null) tab.select();
        }
    }

    private void showDialog(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        DialogFragment newFragment = new ProgressFragment(bundle);
        newFragment.show(fragmentTransaction, "dialog");
    }

    private void removeDialog() {
        if (saveInstanceStateCalled) return;
        try {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } catch (Exception e) {
            Log.e(FragmentBaseActivity.class.getName(), "error while removing dialog", e);
        }
    }

    private void setShowRefreshProgressIcon(boolean show) {
        if (optionsMenu == null) return;
        optionsMenu.findItem(R.id.menu_refresh).setVisible(! show);
        optionsMenu.findItem(R.id.menu_refresh_progress).setVisible(show);
    }

    private FragmentHistoryStackEntry removeLastHistoryFragmentEntry() {
        if (! fragmentHistoryStack.isEmpty()) {
            return fragmentHistoryStack.remove(fragmentHistoryStack.size() - 1);
        }
        return null;
    }
    private FragmentHistoryStackEntry getLastHistoryFragmentEntry() {
        if (! fragmentHistoryStack.isEmpty()) {
            return fragmentHistoryStack.get(fragmentHistoryStack.size() - 1);
        }
        return null;
    }
}
