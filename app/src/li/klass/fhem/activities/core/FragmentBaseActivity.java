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
import android.support.v4.view.ViewPager;
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

import static li.klass.fhem.constants.Actions.*;

public abstract class FragmentBaseActivity extends SherlockFragmentActivity implements ActionBar.TabListener, Updateable {

    public static final String TAG = FragmentBaseActivity.class.getName();

    ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
    private ProgressDialog progressDialog;
    private boolean ignoreTabSelection;
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
    private ViewPager viewPager;
    private TabsAdapter viewPagerAdapter;
    private boolean restoring = false;

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
            intentFilter.addAction(RELOAD);
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
                        } else if (action.equals(RELOAD)) {
//                            onRestoreInstanceState(null);
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        restoring = true;
        if (savedInstanceState != null) {
            viewPager.setCurrentItem(savedInstanceState.getInt("currentTab"));
        }
        handleCurrentFragmentNavigationChanges();
        restoring = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentTab", viewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
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

        viewPager = (ViewPager) findViewById(R.id.mainContent);
        viewPagerAdapter = new TabsAdapter(getSupportFragmentManager(), new TabsAdapter.PageChangeListener() {
            @Override
            public void onPageChanged(int newPage) {
                ignoreTabSelection = true;

                handleNavigationChanges(viewPagerAdapter.getFragmentTypeAt(viewPager.getCurrentItem()));

                ignoreTabSelection = true;
                getSupportActionBar().setSelectedNavigationItem(newPage);
                ignoreTabSelection = false;

                // if restore is in progress, we do not need to switch to the initial page.
                // the top level fragment will restore the currently set fragment
                // especially valid for screen rotation!
                if (!restoring) {
                    switchToInitialFragmentOnPage(newPage);
                }
            }
        });
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(viewPagerAdapter);

        viewPager.setCurrentItem(0);

        ignoreTabSelection = true;
        getSupportActionBar().setSelectedNavigationItem(0);
    }

    private void switchToInitialFragmentOnPage(int newPage) {
        Intent intent = new Intent(Actions.SWITCH_TO_INITIAL_FRAGMENT);
        intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.getTopLevelFragments().get(newPage).name());
        sendBroadcast(intent);
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
        if (intent != null) {
            waitingIntent = intent;
            Log.e(TAG, "waiting intent: " + intent);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.e(TAG, "onPostResume()");
        // process the intent received in onNewIntent()
        if (waitingIntent == null || !waitingIntent.hasExtra(BundleExtraKeys.FRAGMENT_NAME)) {
            return;
        }

        final String fragmentName = waitingIntent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME);
        Log.e(TAG, "resume waiting intent " + fragmentName);

        int delay = 0;
        if (isActivityStart) {
            delay = 1000;
        }

        final Bundle extras = waitingIntent.getExtras();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Actions.SHOW_FRAGMENT);

                intent.putExtras(extras);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.getFragmentFor(fragmentName));
                sendBroadcast(intent);
            }
        }, delay);


        waitingIntent = null;
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
        if (ignoreTabSelection) {
            ignoreTabSelection = false;
            return;
        }

        if (viewPager == null || tab == null) {
            sendBroadcast(new Intent(Actions.RELOAD));
            return;
        }

        ignoreTabSelection = true;
        viewPager.setCurrentItem(tab.getPosition());
        ignoreTabSelection = false;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (ignoreTabSelection) return;

        switchToInitialFragmentOnPage(viewPager.getCurrentItem());
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
        Intent intent = new Intent(Actions.TOP_LEVEL_BACK);
        if (data != null) {
            intent.putExtras(data);
        }
        FragmentType fragmentType = FragmentType.getTopLevelFragments().get(viewPager.getCurrentItem());
        intent.putExtra(BundleExtraKeys.FRAGMENT, fragmentType.name());
        sendBroadcast(intent);

        handleCurrentFragmentNavigationChanges();
    }

    private void handleCurrentFragmentNavigationChanges() {
        FragmentType fragmentType = FragmentType.getTopLevelFragments().get(viewPager.getCurrentItem());
        ignoreTabSelection = true;
        handleNavigationChanges(fragmentType);
        ignoreTabSelection = false;
    }

    private void switchToFragment(FragmentType fragmentType, Bundle data) {
        Log.i(TAG, "switch to fragment " + fragmentType.name());

        handleNavigationChanges(fragmentType);

        removeDialog();
    }


    private void handleNavigationChanges(FragmentType fragmentType) {
        int navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;
        boolean isTablet = findViewById(R.id.tabletIndicator) != null;
        if ((fragmentType.isShowTabs()) || isTablet) {
            navigationMode = ActionBar.NAVIGATION_MODE_TABS;
        }

        if (getSupportActionBar().getNavigationMode() != navigationMode) {
            getSupportActionBar().setNavigationMode(navigationMode);
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

}
