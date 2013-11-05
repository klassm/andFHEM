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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.RepairedDrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.lang.reflect.Constructor;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.constants.Actions.BACK;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.DO_UPDATE;
import static li.klass.fhem.constants.Actions.RELOAD;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.SHOW_TOAST;
import static li.klass.fhem.constants.BundleExtraKeys.HAS_FAVORITES;

public abstract class FragmentBaseActivity extends SherlockFragmentActivity implements Updateable {

    public static final String TAG = FragmentBaseActivity.class.getName();
    public static final String NAVIGATION_TAG = "NAVIGATION_TAG";
    public static final String CONTENT_TAG = "CONTENT_TAG";

    ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
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

    private RepairedDrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean saveInstanceStateCalled;

    protected FragmentBaseActivity() {
    }

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
                        if (action == null) return;

                        if (Actions.SHOW_FRAGMENT.equals(action)) {
                            Bundle bundle = intent.getExtras();
                            if (bundle == null) throw new IllegalArgumentException("need a content fragment");
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
                            setShowRefreshProgressIcon(true);
                        } else if (action.equals(DISMISS_EXECUTING_DIALOG)) {
                            setShowRefreshProgressIcon(false);
                        } else if (action.equals(SHOW_TOAST)) {
                            String content = intent.getStringExtra(BundleExtraKeys.CONTENT);
                            if (content == null) {
                                content = getString(intent.getIntExtra(BundleExtraKeys.TOAST_STRING_ID, 0));
                            }
                            Toast.makeText(FragmentBaseActivity.this, content, Toast.LENGTH_SHORT).show();
                        } else if (action.equals(BACK)) {
                            onBackPressed();
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
        saveInstanceStateCalled = false;
        isActivityStart = true;

        if (getIntent() != null) {
            waitingIntent = getIntent();
        }

        setContentView(R.layout.main_view);
        if (findViewById(R.id.tabletIndicator) != null) {
            AndFHEMApplication.INSTANCE.setIsTablet(true);
        }

        autoUpdateHandler = new Handler();
        autoUpdateHandler.postDelayed(autoUpdateCallback, 0);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.setDisplayHomeAsUpEnabled(true);

        broadcastReceiver = new Receiver();
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());

        initDrawerLayout();

        if (savedInstanceState == null) {
            Intent intent = new Intent(Actions.FAVORITES_PRESENT);
            intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    super.onReceiveResult(resultCode, resultData);

                    handleHasFavoritesResponse(resultCode, resultData);
                }
            });
            startService(intent);
        }
    }

    private void handleHasFavoritesResponse(int resultCode, Bundle resultData) {
        if (resultCode == ResultCodes.SUCCESS && resultData.containsKey(HAS_FAVORITES)) {
            boolean hasFavorites = resultData.getBoolean(HAS_FAVORITES, false);
            if (hasFavorites) {
                switchToFragment(FragmentType.FAVORITES, null);
                return;
            }
        }
        switchToFragment(FragmentType.ALL_DEVICES, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveInstanceStateCalled = true;
        super.onSaveInstanceState(outState);
    }

    private void initDrawerLayout() {
        mDrawerLayout = (RepairedDrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new NavigationDrawerAdapter(this));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentType fragmentType = (FragmentType) view.getTag();

                if (fragmentType == FragmentType.TIMER_OVERVIEW && Build.VERSION.SDK_INT < 11) {
                    String text = String.format(getString(R.string.feature_requires_android_version), 3);
                    DialogUtil.showAlertDialog(FragmentBaseActivity.this, R.string.android_version, text);
                    return;
                }

                switchToFragment(fragmentType, new Bundle());
                mDrawerLayout.closeDrawers();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawerOpen, R.string.drawerClose) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        updateNavigationVisibility();
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

                if (extras != null) intent.putExtras(extras);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.getFragmentFor(fragmentName));
                sendBroadcast(intent);
            }
        }, delay);


        waitingIntent = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        saveInstanceStateCalled = false;
        BillingService.INSTANCE.bindActivity(this);

        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();

        BillingService.INSTANCE.unbindActivity(this);

        RoomListService.INSTANCE.storeDeviceListMap();

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "receiver was not registered, ignore ...");
        }

        autoUpdateHandler.removeCallbacks(autoUpdateCallback);
        setShowRefreshProgressIcon(false);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, com.actionbarsherlock.view.Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        this.optionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        // We pop fragments as long as:
        // - there are more fragments within the back stack
        // - the popped fragment type is not equals to the current fragment type

        BaseFragment contentFragment = getContentFragment();
        FragmentType contentFragmentType = FragmentType.getFragmentFor(contentFragment.getClass());
        while (true) {
            if (! getSupportFragmentManager().popBackStackImmediate()) {
                finish();
                return;
            }

            BaseFragment current = getContentFragment();
            if (current == null) {
                finish();
                return;
            }

            FragmentType currentFragmentType = FragmentType.getFragmentFor(current.getClass());
            if (currentFragmentType != contentFragmentType) {
                break;
            }
        }

        updateNavigationVisibility();
    }

    private BaseFragment getContentFragment() {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(CONTENT_TAG);
    }

    private void switchToFragment(FragmentType fragmentType, Bundle data) {
        if (data == null) data = new Bundle();

        Log.i(TAG, "switch to " + fragmentType.name() + " with " + data.toString());
        if (fragmentType.isTopLevelFragment()) {
            clearBackStack();
        }

        BaseFragment contentFragment = createContentFragment(fragmentType, data);
        BaseFragment navigationFragment = createNavigationFragment(fragmentType, data);

        setContent(navigationFragment, contentFragment);
    }


    private BaseFragment createContentFragment(FragmentType fragmentType, Bundle data) {
        if (fragmentType == null) {
            sendBroadcast(new Intent(Actions.RELOAD));
            return null;
        }
        try {
            Class<? extends BaseFragment> fragmentClass = fragmentType.getContentClass();
            return createFragmentForClass(data, fragmentClass);
        } catch (Exception e) {
            Log.e(TAG, "cannot instantiate fragment", e);
            return null;
        }
    }

    private BaseFragment createNavigationFragment(FragmentType fragmentType, Bundle data) {
        View navigationView = findViewById(R.id.navigation);
        if (navigationView == null) {
            return null;
        }

        try {
            Class<? extends BaseFragment> navigationClass = fragmentType.getNavigationClass();
            if (navigationClass == null) {
                navigationView.setVisibility(View.GONE);
                return null;
            }
            navigationView.setVisibility(View.VISIBLE);
            BaseFragment fragment = createFragmentForClass(data, navigationClass);
            fragment.setNavigation(true);
            return fragment;
        } catch (Exception e) {
            Log.e(TAG, "cannot instantiate fragment", e);
            return null;
        }
    }


    private void setContent(BaseFragment navigationFragment, BaseFragment contentFragment) {
        if (saveInstanceStateCalled) return;

        boolean hasNavigation = hasNavigation(navigationFragment, contentFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null) {
            Log.e(TAG, "fragment manager is null in #setContent");
            return;
        }

        FragmentTransaction transaction = fragmentManager
                .beginTransaction()
                .addToBackStack(contentFragment.getClass().getName())
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        if (hasNavigation) {
            transaction.replace(R.id.navigation, navigationFragment, NAVIGATION_TAG);
        }
        transaction.replace(R.id.content, contentFragment, CONTENT_TAG);

        transaction.commit();

        updateNavigationVisibility(navigationFragment, contentFragment);
    }

    private BaseFragment getNavigationFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager == null) return null;
        return (BaseFragment) supportFragmentManager
                .findFragmentByTag(NAVIGATION_TAG);
    }

    private boolean updateNavigationVisibility() {
        BaseFragment navigationFragment = getNavigationFragment();
        BaseFragment contentFragment = getContentFragment();

        return updateNavigationVisibility(navigationFragment , contentFragment);
    }

    private boolean hasNavigation(BaseFragment navigationFragment, BaseFragment contentFragment) {
        FragmentType fragmentType = FragmentType.getFragmentFor(contentFragment.getClass());
        View navigationView = findViewById(R.id.navigation);
        return navigationView != null && !(navigationFragment == null || fragmentType.getNavigationClass() == null);
    }

    private boolean updateNavigationVisibility(BaseFragment navigationFragment, BaseFragment contentFragment) {
        FragmentType fragmentType = FragmentType.getFragmentFor(contentFragment.getClass());

        boolean hasNavigation = hasNavigation(navigationFragment, contentFragment);
        View navigationView = findViewById(R.id.navigation);
        if (navigationView != null) {
            if (navigationFragment == null || fragmentType.getNavigationClass() == null) {
                navigationView.setVisibility(View.GONE);
            } else {
                navigationView.setVisibility(View.VISIBLE);
            }
        }
        return hasNavigation;
    }


    private BaseFragment createFragmentForClass(Bundle data, Class<? extends BaseFragment> fragmentClass) throws Exception {
        if (fragmentClass == null) return null;

        Constructor<? extends BaseFragment> constructor = fragmentClass.getConstructor(Bundle.class);
        return constructor.newInstance(data);
    }

    private void clearBackStack() {
        int entryCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < entryCount; i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void setShowRefreshProgressIcon(boolean show) {
        if (optionsMenu == null) return;
        optionsMenu.findItem(R.id.menu_refresh).setVisible(!show);
        optionsMenu.findItem(R.id.menu_refresh_progress).setVisible(show);
    }

}
