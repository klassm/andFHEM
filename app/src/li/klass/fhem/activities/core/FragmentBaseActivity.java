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

package li.klass.fhem.activities.core;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.ApplicationProperties;

import java.util.ArrayList;

import static li.klass.fhem.constants.Actions.*;

public abstract class FragmentBaseActivity extends SherlockFragmentActivity implements ActionBar.TabListener, Updateable {

    public static final String TAG = FragmentBaseActivity.class.getName();

    ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
    private ProgressDialog progressDialog;
    private boolean backPressed;

    private enum FragmentAction {
        CREATE_NEW, UPDATE, NOTHING
    }

    private ContentHolderFragment currentHistoryStackEntry = null;
    private ArrayList<ContentHolderFragment> fragmentHistoryStack = new ArrayList<ContentHolderFragment>();

    private Receiver broadcastReceiver;
    private Menu optionsMenu;

    /**
     * an intent waiting to be processed, but received in the wrong activity state (widget problem ..)
     */
    private Intent waitingIntent;

    /**
     * Attribute is true if the activity has been restarted instead of being newly created
     */
    boolean isActivityStart = true;

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

                Log.d(TAG, "update");
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
                            Bundle bundle = intent.getExtras();
                            FragmentType fragmentType;
                            if (bundle.containsKey(BundleExtraKeys.FRAGMENT)) {
                                fragmentType = (FragmentType) bundle.getSerializable(BundleExtraKeys.FRAGMENT);
                            } else {
                                String fragmentName = bundle.getString(BundleExtraKeys.FRAGMENT_NAME);
                                fragmentType = FragmentType.getFragmentFor(fragmentName);
                            }
                            switchToFragment(fragmentType, intent.getExtras());
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
                            String content = intent.getStringExtra(BundleExtraKeys.CONTENT);
                            if (content == null) {
                                content = getString(intent.getIntExtra(BundleExtraKeys.TOAST_STRING_ID, 0));
                            }
                            Toast.makeText(FragmentBaseActivity.this, content, Toast.LENGTH_SHORT).show();
                        } else if (action.equals(BACK)) {
                            onBackPressed(intent.getExtras());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "exception occurred while receiving broadcast", e);
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
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
            Log.e(TAG, "error while creating activity", e);
        }

        if (getIntent() != null) {
            waitingIntent = getIntent();
        }

        setContentView(R.layout.main_view);
        if (findViewById(R.id.tabletIndicator) != null) {
            AndFHEMApplication.INSTANCE.setIsTablet(true);
        }

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
            actionBar.addTab(tab, false);
        }

        actionBar.setDisplayHomeAsUpEnabled(true);

        broadcastReceiver = new Receiver();
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());

        boolean restoreResult = false;
        if (savedInstanceState != null) {
            restoreResult = restoreSavedInstance(savedInstanceState);
        }

        if (!getIntent().hasExtra(BundleExtraKeys.FRAGMENT_NAME) && (savedInstanceState == null || !restoreResult)) {
            Log.i(TAG, "create a new favorites fragment");
            switchToFragment(FragmentType.FAVORITES, new Bundle());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
        sendBroadcast(new Intent(Actions.BACK));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        boolean updateOnApplicationStart = applicationProperties
                .getBooleanSharedPreference(PreferenceKeys.UPDATE_ON_APPLICATION_START, false);

        if (hasFocus && isActivityStart) {
            Log.i(TAG, "request update on application start preference");
            Intent intent = new Intent(Actions.DO_UPDATE);
            intent.putExtra(BundleExtraKeys.DO_REFRESH, updateOnApplicationStart);
            sendBroadcast(intent);
        }

        // reset the attribute!
        isActivityStart = false;
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
        Log.d(TAG, "onPostResume()");
        // process the intent received in onNewIntent()
        if (waitingIntent == null || !waitingIntent.hasExtra(BundleExtraKeys.FRAGMENT_NAME)) {
            return;
        }
        String fragmentName = waitingIntent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME);
        Log.i(TAG, "switching to fragment " + fragmentName);
        if (fragmentName != null) {
            try {
                switchToFragment(FragmentType.getFragmentFor(fragmentName), waitingIntent.getExtras());
            } catch (Exception e) {
                Log.e(TAG, "error while creating fragment", e);
            }
        }

        waitingIntent = null;

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

    @SuppressWarnings("unchecked")
    private boolean restoreSavedInstance(Bundle savedInstanceState) {
        try {
            ArrayList<ContentHolderFragment> previousFragmentStack =
                    (ArrayList<ContentHolderFragment>) savedInstanceState.getSerializable(BundleExtraKeys.FRAGMENT_HISTORY_STACK);

            if (previousFragmentStack != null) {
                fragmentHistoryStack = previousFragmentStack;
            }

            ContentHolderFragment currentEntry = (ContentHolderFragment) savedInstanceState.getSerializable(BundleExtraKeys.CURRENT_FRAGMENT);
            if (currentEntry != null) {
                switchToFragment(currentEntry, false);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.i(TAG, "error occurred while restoring instance", e);
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BillingService.INSTANCE.bindActivity(this);

        saveInstanceStateCalled = false;
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();

        BillingService.INSTANCE.unbindActivity(this);

        RoomListService.INSTANCE.storeDeviceListMap();
        unregisterReceiver(broadcastReceiver);

        autoUpdateHandler.removeCallbacks(autoUpdateCallback);
        setShowRefreshProgressIcon(false);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (backPressed) return;

        Object tag = tab.getTag();
        Log.d(TAG, "selected tab with target " + tag);

        if (!(tag instanceof FragmentType)) {
            Log.e(TAG, "can only switch tabs including a FragmentType as tag");
            return;
        }

        FragmentType fragmentTypeTag = (FragmentType) tag;
        if (fragmentTypeTag == currentHistoryStackEntry.getFragmentType()) return;

        Intent intent = new Intent(Actions.SHOW_FRAGMENT);
        intent.putExtra(BundleExtraKeys.FRAGMENT, fragmentTypeTag);
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
    public void onBackPressed() {
        onBackPressed(null);
    }

    private void onBackPressed(Bundle data) {
        backPressed = true;

        removeDialog();
        ContentHolderFragment previousEntry = removeLastHistoryFragmentEntry();

        if (previousEntry != null) {
            Log.d(TAG, "back pressed, switching to previous fragment");
            switchToFragment(previousEntry, false);
            previousEntry.onBackPressResult(data);
        } else {
            Log.d(TAG, "back pressed, no more previous fragments on the stack");
            finish();
        }
    }

    private void switchToFragment(FragmentType fragmentType, Bundle data) {
        Log.e(TAG, "switch to fragment " + fragmentType.name());

        FragmentAction requiredAction = findOutRequiredFragmentAction(fragmentType, data);
        if (requiredAction == FragmentAction.NOTHING) {
            return;
        }

        if (requiredAction == FragmentAction.UPDATE) {
            currentHistoryStackEntry.onHolderContentChanged(data);

            return;
        }

        ContentHolderFragment contentFragment = new ContentHolderFragment(fragmentType, data);

        boolean addToStack = data.getBoolean(BundleExtraKeys.FRAGMENT_ADD_TO_STACK, true);
        switchToFragment(contentFragment, addToStack);
    }

    private FragmentAction findOutRequiredFragmentAction(FragmentType fragmentType, Bundle data) {
        if (currentHistoryStackEntry == null || fragmentType != currentHistoryStackEntry.getFragmentType()) {
            return FragmentAction.CREATE_NEW;
        }
        if (currentHistoryStackEntry.getCreationAttributesAsBundle().equals(data)) {
            return FragmentAction.NOTHING;
        }
        return FragmentAction.UPDATE;
    }

    private void switchToFragment(ContentHolderFragment toSwitchToEntry, boolean putToStack) {
        removeDialog();

        Log.d(TAG, "switch to " + toSwitchToEntry.getFragmentType().name());

        if (putToStack) {
            if (fragmentHistoryStack.size() > 10) fragmentHistoryStack.remove(0);
            fragmentHistoryStack.add(currentHistoryStackEntry);
        }

        if (toSwitchToEntry.getFragmentType().isTopLevelFragment()) {
            fragmentHistoryStack.clear();
        }

        currentHistoryStackEntry = toSwitchToEntry;

        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContent, toSwitchToEntry)
                    .commitAllowingStateLoss();
            handleNavigationChanges(toSwitchToEntry);

        } catch (IllegalStateException e) {
            Log.e(TAG, "error while switching to fragment " + toSwitchToEntry.getFragmentType().name(), e);
        }

        backPressed = false;
    }

    private void handleNavigationChanges(ContentHolderFragment entry) {
        int navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
        boolean isTablet = findViewById(R.id.tabletIndicator) != null;
        if ((entry.getFragmentType().isShowTabs()) || isTablet) {
            navigationMode = ActionBar.NAVIGATION_MODE_TABS;
        }

        if (getSupportActionBar().getNavigationMode() != navigationMode) {
            getSupportActionBar().setNavigationMode(navigationMode);
        }

        FragmentType currentFragmentType = entry.getFragmentType();
        if (currentFragmentType.isTopLevelFragment() && currentFragmentType.getTopLevelPosition() != -1) {
            int topLevelPosition = currentFragmentType.getTopLevelPosition();
            ActionBar.Tab tab = getSupportActionBar().getTabAt(topLevelPosition);
            if (tab != null) tab.select();
        }
    }

    private void showDialog(Bundle bundle) {
        String message = getString(bundle.getInt(BundleExtraKeys.CONTENT));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);

        if (bundle.containsKey(BundleExtraKeys.TITLE)) {
            progressDialog.setTitle(bundle.getInt(BundleExtraKeys.TITLE));
        }
        progressDialog.setCancelable(true);

        progressDialog.show();
    }

    private void removeDialog() {
        if (saveInstanceStateCalled) return;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void setShowRefreshProgressIcon(boolean show) {
        if (optionsMenu == null) return;
        optionsMenu.findItem(R.id.menu_refresh).setVisible(!show);
        optionsMenu.findItem(R.id.menu_refresh_progress).setVisible(show);
    }

    private ContentHolderFragment removeLastHistoryFragmentEntry() {
        if (!fragmentHistoryStack.isEmpty()) {
            return fragmentHistoryStack.remove(fragmentHistoryStack.size() - 1);
        }
        return null;
    }
}
