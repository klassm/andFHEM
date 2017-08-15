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

package li.klass.fhem.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.RepairedDrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.ApplicationUrls;
import li.klass.fhem.R;
import li.klass.fhem.activities.core.AvailableConnectionDataAdapter;
import li.klass.fhem.activities.core.UpdateTimerTask;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.login.LoginUIService;
import li.klass.fhem.service.device.GCMSendDeviceService;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.update.UpdateHandler;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.widget.SwipeRefreshLayout;

import static li.klass.fhem.AndFHEMApplication.PREMIUM_PACKAGE;
import static li.klass.fhem.constants.Actions.BACK;
import static li.klass.fhem.constants.Actions.CONNECTIONS_CHANGED;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.DO_UPDATE;
import static li.klass.fhem.constants.Actions.IS_PREMIUM;
import static li.klass.fhem.constants.Actions.REDRAW;
import static li.klass.fhem.constants.Actions.SHOW_ALERT;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.SHOW_FRAGMENT;
import static li.klass.fhem.constants.Actions.SHOW_TOAST;
import static li.klass.fhem.constants.Actions.UPDATE_NAVIGATION;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT_NAME;
import static li.klass.fhem.constants.PreferenceKeys.STARTUP_VIEW;
import static li.klass.fhem.fragments.FragmentType.ALL_DEVICES;
import static li.klass.fhem.fragments.FragmentType.FAVORITES;
import static li.klass.fhem.fragments.FragmentType.getFragmentFor;

public class AndFHEMMainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener,
        SwipeRefreshLayout.OnRefreshListener, SwipeRefreshLayout.ChildScrollDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndFHEMMainActivity.class);

    private class Receiver extends BroadcastReceiver {

        private final IntentFilter intentFilter;

        private Receiver() {
            intentFilter = new IntentFilter();
            intentFilter.addAction(SHOW_FRAGMENT);
            intentFilter.addAction(DO_UPDATE);
            intentFilter.addAction(UPDATE_NAVIGATION);
            intentFilter.addAction(SHOW_EXECUTING_DIALOG);
            intentFilter.addAction(DISMISS_EXECUTING_DIALOG);
            intentFilter.addAction(SHOW_TOAST);
            intentFilter.addAction(SHOW_ALERT);
            intentFilter.addAction(BACK);
            intentFilter.addAction(CONNECTIONS_CHANGED);
            intentFilter.addAction(REDRAW);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (!saveInstanceStateCalled) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String action = intent.getAction();
                            if (action == null) return;

                            if (SHOW_FRAGMENT.equals(action)) {
                                Bundle bundle = intent.getExtras();
                                if (bundle == null)
                                    throw new IllegalArgumentException("need a content fragment");
                                FragmentType fragmentType;
                                if (bundle.containsKey(FRAGMENT)) {
                                    fragmentType = (FragmentType) bundle.getSerializable(FRAGMENT);
                                } else {
                                    String fragmentName = bundle.getString(FRAGMENT_NAME);
                                    fragmentType = getFragmentFor(fragmentName);
                                }
                                drawerLayout.closeDrawer(GravityCompat.START);
                                switchToFragment(fragmentType, intent.getExtras());
                            } else if (action.equals(DO_UPDATE)) {
                                updateShowRefreshProgressIcon();
                                refreshFragments(intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false));
                            } else if (action.equals(UPDATE_NAVIGATION)) {
                                refreshNavigation();
                            } else if (action.equals(SHOW_EXECUTING_DIALOG)) {
                                updateShowRefreshProgressIcon();
                                refreshLayout.setRefreshing(true);
                            } else if (action.equals(DISMISS_EXECUTING_DIALOG)) {
                                updateShowRefreshProgressIcon();
                                refreshLayout.setRefreshing(false);
                            } else if (action.equals(SHOW_TOAST)) {
                                String content = intent.getStringExtra(BundleExtraKeys.CONTENT);
                                if (content == null) {
                                    content = getString(intent.getIntExtra(BundleExtraKeys.STRING_ID, 0));
                                }
                                Toast.makeText(AndFHEMMainActivity.this, content, Toast.LENGTH_SHORT).show();
                            } else if (action.equals(SHOW_ALERT)) {
                                DialogUtil.showAlertDialog(AndFHEMMainActivity.this,
                                        intent.getIntExtra(BundleExtraKeys.ALERT_TITLE_ID, R.string.blank),
                                        intent.getIntExtra(BundleExtraKeys.ALERT_CONTENT_ID, R.string.blank));
                            } else if (action.equals(BACK)) {
                                onBackPressed((FragmentType) intent.getSerializableExtra(FRAGMENT));
                            } else if (CONNECTIONS_CHANGED.equals(action)) {
                                if (availableConnectionDataAdapter != null) {
                                    availableConnectionDataAdapter.doLoad();
                                }
                            } else if (REDRAW.equals(action)) {
                                redrawContent();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "exception occurred while receiving broadcast", e);
                            Log.e(TAG, "exception occurred while receiving broadcast", e);
                        }
                    }
                });
            }
        }

        public IntentFilter getIntentFilter() {
            return intentFilter;
        }
    }

    public static final String TAG = AndFHEMMainActivity.class.getName();
    public static final String NAVIGATION_TAG = "NAVIGATION_TAG";
    public static final String CONTENT_TAG = "CONTENT_TAG";
    private static final String STATE_DRAWER_ID = "drawer_id";

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    BillingService billingService;

    @Inject
    UpdateHandler updateHandler;

    @Inject
    GCMSendDeviceService gcmSendDeviceService;

    @Inject
    LoginUIService loginUiService;

    private Receiver broadcastReceiver;

    protected Menu optionsMenu;

    private Timer timer;
    private RepairedDrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private SwipeRefreshLayout refreshLayout;
    private NavigationView navigationView;

    private boolean saveInstanceStateCalled;
    private int mSelectedDrawerId = -1;

    private AvailableConnectionDataAdapter availableConnectionDataAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            initialize(savedInstanceState);
        } catch (Throwable e) {
            Log.e(TAG, "onCreate() : error during initialization", e);
        }

        updateHandler.onApplicationUpdate();
        gcmSendDeviceService.registerWithGCM(this);
    }

    private void initialize(final Bundle savedInstanceState) {
        AndFHEMApplication application = (AndFHEMApplication) getApplication();
        application.getDaggerComponent().inject(this);

        saveInstanceStateCalled = false;

        setContentView(R.layout.main_view);

        broadcastReceiver = new Receiver();
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        initSwipeRefreshLayout();
        initDrawerLayout();

        boolean hasFavorites = getIntent().getBooleanExtra(BundleExtraKeys.HAS_FAVORITES, true);
        if (savedInstanceState == null && !saveInstanceStateCalled) {
            handleStartupFragment(hasFavorites);
        }
        showDrawerToggle(getSupportFragmentManager().getBackStackEntryCount() == 0);
    }


    private void handleStartupFragment(boolean hasFavorites) {
        String startupView = applicationProperties.getStringSharedPreference(STARTUP_VIEW,
                FragmentType.FAVORITES.name(), this);
        FragmentType preferencesStartupFragment = FragmentType.forEnumName(startupView);
        Log.d(TAG, "handleStartupFragment() : startup view is " + preferencesStartupFragment);

        if (preferencesStartupFragment == null) {
            preferencesStartupFragment = ALL_DEVICES;
        }

        FragmentType fragmentType = preferencesStartupFragment;
        if (fragmentType == FAVORITES && !hasFavorites) {
            fragmentType = ALL_DEVICES;
        }


        Bundle startupBundle = new Bundle();
        Log.i(TAG, "handleStartupFragment () : startup with " + fragmentType + " (extras: " + startupBundle + ")");
        switchToFragment(fragmentType, startupBundle);
    }

    private void initConnectionSpinner(View spinner, Runnable onConnectionChanged) {
        Spinner connectionSpinner = (Spinner) spinner;
        availableConnectionDataAdapter = new AvailableConnectionDataAdapter(connectionSpinner, onConnectionChanged);
        connectionSpinner.setAdapter(availableConnectionDataAdapter);
        connectionSpinner.setOnItemSelectedListener(availableConnectionDataAdapter);
        availableConnectionDataAdapter.doLoad();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveInstanceStateCalled = true;
        outState.putInt(STATE_DRAWER_ID, mSelectedDrawerId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedDrawerId = savedInstanceState.getInt(STATE_DRAWER_ID, -1);
        if (mSelectedDrawerId > 0) {
            navigationView.getMenu().findItem(mSelectedDrawerId).setChecked(true);
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            // We are at the topmost fragment, re-enable the drawer indicator
            showDrawerToggle(true);
        }
        updateTitle();
        updateNavigationVisibility();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (menuItem.getItemId()) {
            case R.id.menu_settings: {
                Intent settingsIntent = new Intent(this, PreferencesActivity.class);
                startActivityForResult(settingsIntent, RESULT_OK);
                return true;
            }
            case R.id.menu_help: {
                Uri helpUri = Uri.parse(ApplicationUrls.HELP_PAGE);
                Intent helpIntent = new Intent(Intent.ACTION_VIEW, helpUri);
                startActivity(helpIntent);
                return true;
            }
            case R.id.menu_premium: {
                Intent premiumIntent = new Intent(this, PremiumActivity.class);
                startActivity(premiumIntent);
                return true;
            }
            case R.id.menu_about: {
                String version;
                try {
                    String pkg = getPackageName();
                    version = getPackageManager().getPackageInfo(pkg, 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    version = "?";
                }
                DialogUtil.showAlertDialog(this, R.string.about,
                        "Matthias Klass\r\nVersion: " + version + "\r\n" +
                                "andFHEM.klass.li\r\nandFHEM@klass.li\r\n" + getPackageName());
                return true;
            }
        }

        FragmentType fragmentType = FragmentType.getFragmentFor(menuItem.getItemId());
        if (fragmentType == null) {
            return false;
        }

        switchToFragment(fragmentType, new Bundle());

        return true;
    }

    private void showDrawerToggle(boolean enable) {
        actionBarDrawerToggle.setDrawerIndicatorEnabled(enable);
    }

    private void initDrawerLayout() {
        drawerLayout = (RepairedDrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        navigationView = (NavigationView) findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);
        if (getPackageName().equals(PREMIUM_PACKAGE)) {
            navigationView.getMenu().removeItem(R.id.menu_premium);
        }

        initConnectionSpinner(navigationView.getHeaderView(0).findViewById(R.id.connection_spinner),
                new Runnable() {

                    @Override
                    public void run() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                    }
                });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawerOpen, R.string.drawerClose) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    private void initSwipeRefreshLayout() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        assert refreshLayout != null;
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setChildScrollDelegate(this);
        refreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.primary), 0,
                getResources().getColor(R.color.accent), 0);
    }

    @Override
    public boolean canChildScrollUp() {
        BaseFragment content = getContentFragment();
        return content != null && content.canChildScrollUp();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (actionBarDrawerToggle != null) actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
        updateNavigationVisibility();
        getContentFragment().invalidate();
        if (getNavigationFragment() != null) {
            getNavigationFragment().invalidate();
        }
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);
        Intent refreshIntent = new Intent(DO_UPDATE);
        refreshIntent.putExtra(DO_REFRESH, true);
        sendBroadcast(refreshIntent);
    }

    private boolean updateNavigationVisibility() {
        BaseFragment navigationFragment = getNavigationFragment();
        BaseFragment contentFragment = getContentFragment();

        return updateNavigationVisibility(navigationFragment, contentFragment);
    }

    private BaseFragment getNavigationFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager == null) return null;
        return (BaseFragment) supportFragmentManager
                .findFragmentByTag(NAVIGATION_TAG);
    }

    private BaseFragment getContentFragment() {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(CONTENT_TAG);
    }

    private boolean updateNavigationVisibility(BaseFragment navigationFragment, BaseFragment contentFragment) {
        if (contentFragment == null) return false;

        FragmentType fragmentType = getFragmentFor(contentFragment.getClass());
        if (fragmentType == null) {
            LOGGER.error("hasNavigation - cannot find fragment type for {}", contentFragment.getClass().getName());
            return false;
        }

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

    private boolean hasNavigation(BaseFragment navigationFragment, BaseFragment contentFragment) {
        FragmentType fragmentType = getFragmentFor(contentFragment.getClass());
        if (fragmentType == null) {
            LOGGER.error("hasNavigation - cannot find fragment type for {}", contentFragment.getClass().getName());
            return false;
        }
        View navigationView = findViewById(R.id.navigation);
        return navigationView != null && !(navigationFragment == null || fragmentType.getNavigationClass() == null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume() : resuming");

        loginUiService.doLoginIfRequired(this, new LoginUIService.LoginStrategy() {
            @Override
            public void requireLogin(@NonNull Context context, @NonNull final Function1<? super String, Unit> checkLogin) {
                final View view = getLayoutInflater().inflate(R.layout.login, null);
                DialogUtil.showContentDialog(context, getString(R.string.login), view, new DialogUtil.AlertOnClickListener() {
                    @Override
                    public void onClick() {
                        checkLogin.invoke(((EditText) view.findViewById(R.id.password)).getText().toString());
                    }
                });
            }

            @Override
            public void onLoginSuccess() {
                saveInstanceStateCalled = false;

                if (broadcastReceiver != null) {
                    registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
                }

                if (availableConnectionDataAdapter != null) {
                    availableConnectionDataAdapter.doLoad();
                }
                updateNavigationVisibility();

                handleTimerUpdates();
                handleOpenIntent();
                updateTitle();
            }

            @Override
            public void onLoginFailure() {
                finish();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startService(new Intent(IS_PREMIUM).setClass(this, LicenseIntentService.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void handleOpenIntent() {
        final Optional<FragmentType> intentFragment = getFragmentTypeFromStartupIntent();
        if (intentFragment.isPresent()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switchToFragment(intentFragment.get(), getIntent().getExtras());
                    setIntent(null);
                }
            }, 500);
        }
    }

    private Optional<FragmentType> getFragmentTypeFromStartupIntent() {
        Optional<FragmentType> toReturn = Optional.absent();

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(FRAGMENT)) {
                toReturn = Optional.of((FragmentType) intent.getSerializableExtra(BundleExtraKeys.FRAGMENT));
            } else if (intent.hasExtra(FRAGMENT_NAME)) {
                String fragmentName = intent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME);
                toReturn = Optional.of(FragmentType.valueOf(fragmentName));
            }
        }
        return toReturn;
    }

    private void refreshFragments(boolean doUpdate) {
        refreshContent(doUpdate);
        refreshNavigation();
    }

    private void refreshNavigation() {
        BaseFragment nav = getNavigationFragment();
        if (nav != null) {
            nav.update(false);
        }
    }

    private void refreshContent(boolean doUpdate) {
        BaseFragment content = getContentFragment();
        if (content != null) {
            content.update(doUpdate);
        }
    }

    private void handleTimerUpdates() {
        // We post this delayed, as otherwise we will block the application startup (causing
        // ugly ANRs).
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int updateInterval = Integer.valueOf(applicationProperties.getStringSharedPreference(PreferenceKeys.AUTO_UPDATE_TIME,
                        "-1", AndFHEMMainActivity.this));

                if (timer == null && updateInterval != -1) {
                    timer = new Timer();
                }

                if (updateInterval != -1) {
                    timer.scheduleAtFixedRate(new UpdateTimerTask(AndFHEMMainActivity.this), updateInterval, updateInterval);
                    LOGGER.info("handleTimerUpdates() - scheduling update every {} minutes", (updateInterval / 1000 / 60));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        billingService.stop();

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "onStop() : receiver was not registered, ignore ...");
        }

        refreshLayout.setRefreshing(false);
        updateShowRefreshProgressIcon();
    }

    private void updateShowRefreshProgressIcon() {
        if (optionsMenu == null) return;

        this.invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            BaseFragment contentFragment = getContentFragment();
            System.out.println(contentFragment);
        }
    }

    public void onBackPressed(FragmentType fragmentType) {
        if (fragmentType == null) {
            onBackPressed();
            return;
        }
        FragmentManager manager = getSupportFragmentManager();
        while (manager.getBackStackEntryCount() > 0 && !getContentFragment().getClass().equals(fragmentType.getContentClass())) {
            manager.popBackStackImmediate();
        }
    }

    private void redrawContent() {

        BaseFragment contentFragment = getContentFragment();
        if (contentFragment != null) contentFragment.invalidate();

        BaseFragment navigationFragment = getNavigationFragment();
        if (navigationFragment != null) navigationFragment.invalidate();
    }

    private void switchToFragment(FragmentType fragmentType, Bundle data) {
        if (!saveInstanceStateCalled) {
            if (data == null) data = new Bundle();

            Log.i(TAG, "switch to " + fragmentType.name() + " with " + data.toString());
            if (fragmentType.isTopLevelFragment()) {
                clearBackStack();
            }

            int drawerId = fragmentType.getDrawerMenuId();
            if (drawerId > 0) {
                MenuItem item = navigationView.getMenu().findItem(drawerId);
                if (item != null) {
                    item.setChecked(true);
                    mSelectedDrawerId = drawerId;
                }
            }

            BaseFragment contentFragment = createContentFragment(fragmentType, data);
            BaseFragment navigationFragment = createNavigationFragment(fragmentType, data);

            setContent(navigationFragment, contentFragment, !fragmentType.isTopLevelFragment());
        }
    }

    private void updateTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        CharSequence title = null;
        int backstackCount = fm.getBackStackEntryCount();

        if (backstackCount > 0) {
            title = fm.getBackStackEntryAt(backstackCount - 1).getBreadCrumbTitle();
        }
        if (title == null && mSelectedDrawerId > 0) {
            title = navigationView.getMenu().findItem(mSelectedDrawerId).getTitle();
        }
        if (title == null) {
            title = getTitle();
        }

        actionBar.setTitle(title);
    }

    private BaseFragment createContentFragment(FragmentType fragmentType, Bundle data) {
        if (fragmentType == null) {
            sendBroadcast(new Intent(REDRAW));
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

    private void setContent(BaseFragment navigationFragment, BaseFragment contentFragment, boolean addToBackStack) {
        if (saveInstanceStateCalled) return;

        boolean hasNavigation = hasNavigation(navigationFragment, contentFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null) {
            Log.e(TAG, "fragment manager is null in #setContent");
            return;
        }

        // We commit later on. Static code analysis won't notice the call...
        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = fragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.content, contentFragment, CONTENT_TAG);

        if (hasNavigation) {
            transaction.replace(R.id.navigation, navigationFragment, NAVIGATION_TAG);
        }
        if (addToBackStack) {
            transaction.addToBackStack(contentFragment.getClass().getName());
            showDrawerToggle(false);
        }

        transaction.setBreadCrumbTitle(contentFragment.getTitle(this));
        transaction.commit();

        updateNavigationVisibility(navigationFragment, contentFragment);
        updateTitle();
    }

    private BaseFragment createFragmentForClass(Bundle data, Class<? extends BaseFragment> fragmentClass) throws Exception {
        if (fragmentClass == null) return null;

        BaseFragment fragment = fragmentClass.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    private void clearBackStack() {
        int entryCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < entryCount; i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            // if the drawer toggle didn't consume the home menu item, this means
            // we disabled it and hence are showing the back button - act accordingly
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            sendBroadcast(new Intent(DO_UPDATE)
                    .putExtra(DO_REFRESH, true));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (getPackageName().equals(PREMIUM_PACKAGE)) {
            menu.removeItem(R.id.menu_premium);
        }
        this.optionsMenu = menu;

        MenuItem refreshItem = optionsMenu.findItem(R.id.menu_refresh);

        if (refreshLayout.isRefreshing()) {
            refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            refreshItem.setActionView(null);
        }

        return super.onCreateOptionsMenu(menu);
    }
}
