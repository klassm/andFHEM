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

package li.klass.fhem.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.RepairedDrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.Toast
import com.google.common.base.Optional
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.ApplicationUrls
import li.klass.fhem.R
import li.klass.fhem.activities.core.AvailableConnectionDataAdapter
import li.klass.fhem.activities.core.UpdateTimerTask
import li.klass.fhem.billing.BillingService
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.Actions.IS_PREMIUM
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH
import li.klass.fhem.constants.BundleExtraKeys.FRAGMENT
import li.klass.fhem.constants.BundleExtraKeys.FRAGMENT_NAME
import li.klass.fhem.fcm.GCMSendDeviceService
import li.klass.fhem.fragments.FragmentType
import li.klass.fhem.fragments.FragmentType.*
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.login.LoginUIService
import li.klass.fhem.service.connection.ConnectionService
import li.klass.fhem.service.intent.LicenseIntentService
import li.klass.fhem.settings.SettingsActivity
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.SettingsKeys.STARTUP_VIEW
import li.klass.fhem.update.UpdateHandler
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.widget.SwipeRefreshLayout
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class AndFHEMMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener, android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener, SwipeRefreshLayout.ChildScrollDelegate {

    inner class Receiver : BroadcastReceiver() {

        val intentFilter: IntentFilter

        init {
            intentFilter = IntentFilter()
            intentFilter.addAction(SHOW_FRAGMENT)
            intentFilter.addAction(DO_UPDATE)
            intentFilter.addAction(UPDATE_NAVIGATION)
            intentFilter.addAction(SHOW_EXECUTING_DIALOG)
            intentFilter.addAction(DISMISS_EXECUTING_DIALOG)
            intentFilter.addAction(SHOW_TOAST)
            intentFilter.addAction(SHOW_ALERT)
            intentFilter.addAction(BACK)
            intentFilter.addAction(CONNECTIONS_CHANGED)
            intentFilter.addAction(REDRAW)
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (!saveInstanceStateCalled) {
                runOnUiThread(Runnable {
                    try {
                        val action = intent.action ?: return@Runnable

                        if (SHOW_FRAGMENT == action) {
                            val bundle = intent.extras ?: throw IllegalArgumentException("need a content fragment")
                            val fragmentType: FragmentType?
                            if (bundle.containsKey(FRAGMENT)) {
                                fragmentType = bundle.getSerializable(FRAGMENT) as FragmentType
                            } else {
                                val fragmentName = bundle.getString(FRAGMENT_NAME)
                                fragmentType = getFragmentFor(fragmentName)
                            }
                            drawerLayout!!.closeDrawer(GravityCompat.START)
                            switchToFragment(fragmentType, intent.extras)
                        } else if (action == DO_UPDATE) {
                            updateShowRefreshProgressIcon()
                            refreshFragments(intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false))
                        } else if (action == UPDATE_NAVIGATION) {
                            refreshNavigation()
                        } else if (action == SHOW_EXECUTING_DIALOG) {
                            updateShowRefreshProgressIcon()
                            refreshLayout!!.isRefreshing = true
                        } else if (action == DISMISS_EXECUTING_DIALOG) {
                            updateShowRefreshProgressIcon()
                            refreshLayout!!.isRefreshing = false
                        } else if (action == SHOW_TOAST) {
                            var content: String? = intent.getStringExtra(BundleExtraKeys.CONTENT)
                            if (content == null) {
                                content = getString(intent.getIntExtra(BundleExtraKeys.STRING_ID, 0))
                            }
                            Toast.makeText(this@AndFHEMMainActivity, content, Toast.LENGTH_SHORT).show()
                        } else if (action == SHOW_ALERT) {
                            DialogUtil.showAlertDialog(this@AndFHEMMainActivity,
                                    intent.getIntExtra(BundleExtraKeys.ALERT_TITLE_ID, R.string.blank),
                                    intent.getIntExtra(BundleExtraKeys.ALERT_CONTENT_ID, R.string.blank))
                        } else if (action == BACK) {
                            onBackPressed(intent.getSerializableExtra(FRAGMENT) as FragmentType?)
                        } else if (CONNECTIONS_CHANGED == action) {
                            if (availableConnectionDataAdapter != null) {
                                availableConnectionDataAdapter!!.doLoad()
                            }
                        } else if (REDRAW == action) {
                            redrawContent()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "exception occurred while receiving broadcast", e)
                        Log.e(TAG, "exception occurred while receiving broadcast", e)
                    }
                })
            }
        }
    }

    @Inject
    lateinit var applicationProperties: ApplicationProperties
    @Inject
    lateinit var billingService: BillingService
    @Inject
    lateinit var updateHandler: UpdateHandler
    @Inject
    lateinit var gcmSendDeviceService: GCMSendDeviceService
    @Inject
    lateinit var loginUiService: LoginUIService
    @Inject
    lateinit var connectionService: ConnectionService
    @Inject
    lateinit var licenseService: LicenseService

    private var broadcastReceiver: Receiver? = null

    protected var optionsMenu: Menu? = null

    private var timer: Timer? = null
    private var drawerLayout: RepairedDrawerLayout? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var navigationView: NavigationView? = null

    private var saveInstanceStateCalled: Boolean = false
    private var mSelectedDrawerId = -1

    private var availableConnectionDataAdapter: AvailableConnectionDataAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            initialize(savedInstanceState)
        } catch (e: Throwable) {
            Log.e(TAG, "onCreate() : error during initialization", e)
        }

        updateHandler.onApplicationUpdate()
    }

    private fun initialize(savedInstanceState: Bundle?) {
        val application = application as AndFHEMApplication
        application.daggerComponent.inject(this)

        saveInstanceStateCalled = false

        setContentView(R.layout.main_view)

        broadcastReceiver = Receiver()
        registerReceiver(broadcastReceiver, broadcastReceiver!!.intentFilter)

        supportFragmentManager.addOnBackStackChangedListener(this)

        initSwipeRefreshLayout()
        initDrawerLayout()

        val hasFavorites = intent.getBooleanExtra(BundleExtraKeys.HAS_FAVORITES, true)
        if (savedInstanceState == null && !saveInstanceStateCalled) {
            handleStartupFragment(hasFavorites)
        }
        showDrawerToggle(supportFragmentManager.backStackEntryCount == 0)
    }


    private fun handleStartupFragment(hasFavorites: Boolean) {
        val startupView = applicationProperties.getStringSharedPreference(STARTUP_VIEW,
                FragmentType.FAVORITES.name, this)
        var preferencesStartupFragment: FragmentType? = FragmentType.forEnumName(startupView)
        Log.d(TAG, "handleStartupFragment() : startup view is " + preferencesStartupFragment)

        if (preferencesStartupFragment == null) {
            preferencesStartupFragment = ALL_DEVICES
        }

        var fragmentType: FragmentType = preferencesStartupFragment
        if (fragmentType == FAVORITES && !hasFavorites) {
            fragmentType = ALL_DEVICES
        }


        val startupBundle = Bundle()
        Log.i(TAG, "handleStartupFragment () : startup with $fragmentType (extras: $startupBundle)")
        switchToFragment(fragmentType, startupBundle)
    }

    private fun initConnectionSpinner(spinner: View, onConnectionChanged: Runnable) {
        val connectionSpinner = spinner as Spinner
        availableConnectionDataAdapter = AvailableConnectionDataAdapter(connectionSpinner, onConnectionChanged, connectionService)
        connectionSpinner.adapter = availableConnectionDataAdapter
        connectionSpinner.onItemSelectedListener = availableConnectionDataAdapter
        availableConnectionDataAdapter!!.doLoad()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveInstanceStateCalled = true
        outState.putInt(STATE_DRAWER_ID, mSelectedDrawerId)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mSelectedDrawerId = savedInstanceState.getInt(STATE_DRAWER_ID, -1)
        if (mSelectedDrawerId > 0) {
            navigationView!!.menu.findItem(mSelectedDrawerId).isChecked = true
        }
    }

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            // We are at the topmost fragment, re-enable the drawer indicator
            showDrawerToggle(true)
        }
        updateTitle()
        updateNavigationVisibility()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        drawerLayout!!.closeDrawer(GravityCompat.START)

        when (menuItem.itemId) {
            R.id.menu_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(settingsIntent, Activity.RESULT_OK)
                return true
            }
            R.id.menu_help -> {
                val helpUri = Uri.parse(ApplicationUrls.HELP_PAGE)
                val helpIntent = Intent(Intent.ACTION_VIEW, helpUri)
                startActivity(helpIntent)
                return true
            }
            R.id.menu_premium -> {
                val premiumIntent = Intent(this, PremiumActivity::class.java)
                startActivity(premiumIntent)
                return true
            }
            R.id.menu_about -> {
                var version: String
                try {
                    val pkg = packageName
                    version = packageManager.getPackageInfo(pkg, 0).versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    version = "?"
                }

                DialogUtil.showAlertDialog(this, R.string.about,
                        "Matthias Klass\r\nVersion: " + version + "\r\n" +
                                "andFHEM.klass.li\r\nandFHEM@klass.li\r\n" + packageName)
                return true
            }
        }

        val fragmentType = FragmentType.getFragmentFor(menuItem.itemId) ?: return false

        switchToFragment(fragmentType, Bundle())

        return true
    }

    private fun showDrawerToggle(enable: Boolean) {
        actionBarDrawerToggle!!.isDrawerIndicatorEnabled = enable
    }

    private fun initDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)

        navigationView = findViewById(R.id.nav_drawer)
        navigationView!!.setNavigationItemSelectedListener(this)
        if (packageName == AndFHEMApplication.Companion.PREMIUM_PACKAGE) {
            navigationView!!.menu.removeItem(R.id.menu_premium)
        }

        licenseService.isPremium({ isPremium ->
            if (!isPremium) {
                navigationView!!.menu.removeItem(R.id.fcm_history)
            }
        }, this)

        initConnectionSpinner(navigationView!!.getHeaderView(0).findViewById(R.id.connection_spinner),
                Runnable {
                    if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout!!.closeDrawer(GravityCompat.START)
                    }
                })

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        actionBarDrawerToggle = object : ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawerOpen, R.string.drawerClose) {
            override fun onDrawerClosed(view: View?) {
                supportInvalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View?) {
                supportInvalidateOptionsMenu()
            }
        }
        drawerLayout!!.setDrawerListener(actionBarDrawerToggle)
    }

    private fun initSwipeRefreshLayout() {
        refreshLayout = findViewById(R.id.refresh_layout)
        assert(refreshLayout != null)
        refreshLayout!!.setOnRefreshListener(this)
        refreshLayout!!.setChildScrollDelegate(this)
        refreshLayout!!.setColorSchemeColors(
                resources.getColor(R.color.primary), 0,
                resources.getColor(R.color.accent), 0)
    }

    override fun canChildScrollUp(): Boolean {
        val content = contentFragment
        return content != null && content.canChildScrollUp()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (actionBarDrawerToggle != null) actionBarDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle!!.onConfigurationChanged(newConfig)
        updateNavigationVisibility()
        contentFragment!!.invalidate()
        if (navigationFragment != null) {
            navigationFragment!!.invalidate()
        }
    }

    override fun onRefresh() {
        refreshLayout!!.isRefreshing = true
        val refreshIntent = Intent(DO_UPDATE)
        refreshIntent.putExtra(DO_REFRESH, true)
        sendBroadcast(refreshIntent)
    }

    private fun updateNavigationVisibility(): Boolean {
        val navigationFragment = navigationFragment
        val contentFragment = contentFragment

        return updateNavigationVisibility(navigationFragment, contentFragment)
    }

    private val navigationFragment: BaseFragment?
        get() {
            val supportFragmentManager = supportFragmentManager ?: return null
            return supportFragmentManager
                    .findFragmentByTag(NAVIGATION_TAG) as BaseFragment?
        }

    private val contentFragment: BaseFragment?
        get() = supportFragmentManager.findFragmentByTag(CONTENT_TAG) as BaseFragment?

    private fun updateNavigationVisibility(navigationFragment: BaseFragment?, contentFragment: BaseFragment?): Boolean {
        if (contentFragment == null) return false

        val fragmentType = getFragmentFor(contentFragment.javaClass)
        if (fragmentType == null) {
            LOGGER.error("hasNavigation - cannot find fragment type for {}", contentFragment.javaClass.name)
            return false
        }

        val hasNavigation = hasNavigation(navigationFragment, contentFragment)
        val navigationView = findViewById<FrameLayout>(R.id.navigation)
        if (navigationView != null) {
            if (navigationFragment == null || fragmentType.navigationClass == null) {
                navigationView.visibility = View.GONE
            } else {
                navigationView.visibility = View.VISIBLE
            }
        }
        return hasNavigation
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        broadcastReceiver?.onReceive(this, data)
    }

    private fun hasNavigation(navigationFragment: BaseFragment?, contentFragment: BaseFragment?): Boolean {
        val fragmentType = getFragmentFor(contentFragment!!.javaClass)
        if (fragmentType == null) {
            LOGGER.error("hasNavigation - cannot find fragment type for {}", contentFragment.javaClass.name)
            return false
        }
        val navigationView = findViewById<FrameLayout>(R.id.navigation)
        return navigationView != null && !(navigationFragment == null || fragmentType.navigationClass == null)
    }

    override fun onResume() {
        super.onResume()

        Log.i(TAG, "onResume() : resuming")

        loginUiService.doLoginIfRequired(this, object : LoginUIService.LoginStrategy {
            override fun requireLogin(context: Context, checkLogin: Function1<String, Unit>) {
                val view = layoutInflater.inflate(R.layout.login, null)
                AlertDialog.Builder(context)
                        .setView(view)
                        .setTitle(R.string.login)
                        .setOnCancelListener { finish() }
                        .setPositiveButton(R.string.okButton, { _, _ -> checkLogin.invoke((view.findViewById<EditText>(R.id.password)).text.toString()) })
                        .show()
            }

            override fun onLoginSuccess() {
                saveInstanceStateCalled = false

                if (broadcastReceiver != null) {
                    registerReceiver(broadcastReceiver, broadcastReceiver!!.intentFilter)
                }

                if (availableConnectionDataAdapter != null) {
                    availableConnectionDataAdapter!!.doLoad()
                }
                updateNavigationVisibility()

                handleTimerUpdates()
                handleOpenIntent()
                updateTitle()
            }

            override fun onLoginFailure() {
                finish()
            }
        })
    }

    override fun onRestart() {
        super.onRestart()
        startService(Intent(IS_PREMIUM).setClass(this, LicenseIntentService::class.java))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleOpenIntent() {
        val intentFragment = fragmentTypeFromStartupIntent
        if (intentFragment.isPresent) {
            switchToFragment(intentFragment.get(), intent.extras)
            intent = null
        }
    }

    private val fragmentTypeFromStartupIntent: Optional<FragmentType>
        get() {
            var toReturn = Optional.absent<FragmentType>()

            val intent = intent
            if (intent != null) {
                if (intent.hasExtra(FRAGMENT)) {
                    toReturn = Optional.of(intent.getSerializableExtra(BundleExtraKeys.FRAGMENT) as FragmentType)
                } else if (intent.hasExtra(FRAGMENT_NAME)) {
                    val fragmentName = intent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME)
                    toReturn = Optional.of(FragmentType.valueOf(fragmentName))
                }
            }
            return toReturn
        }

    private fun refreshFragments(doUpdate: Boolean) {
        refreshContent(doUpdate)
        refreshNavigation()
    }

    private fun refreshNavigation() {
        val nav = navigationFragment
        nav?.update(false)
    }

    private fun refreshContent(doUpdate: Boolean) {
        val content = contentFragment
        content?.update(doUpdate)
    }

    private fun handleTimerUpdates() {
        // We post this delayed, as otherwise we will block the application startup (causing
        // ugly ANRs).
        Handler().post {
            val updateInterval = Integer.valueOf(applicationProperties.getStringSharedPreference(SettingsKeys.AUTO_UPDATE_TIME,
                    "-1", this@AndFHEMMainActivity))!!

            if (timer == null && updateInterval != -1) {
                timer = Timer()
            }

            if (updateInterval != -1) {
                timer!!.scheduleAtFixedRate(UpdateTimerTask(this@AndFHEMMainActivity), updateInterval.toLong(), updateInterval.toLong())
                LOGGER.info("handleTimerUpdates() - scheduling update every {} minutes", updateInterval / 1000 / 60)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

    override fun onStop() {
        super.onStop()

        billingService.stop()

        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.i(TAG, "onStop() : receiver was not registered, ignore ...")
        }

        refreshLayout!!.isRefreshing = false
        updateShowRefreshProgressIcon()
    }

    private fun updateShowRefreshProgressIcon() {
        if (optionsMenu == null) return

        this.invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            val contentFragment = contentFragment
            println(contentFragment)
        }
    }

    fun onBackPressed(fragmentType: FragmentType?) {
        if (fragmentType == null) {
            onBackPressed()
            return
        }
        val manager = supportFragmentManager
        while (manager.backStackEntryCount > 0 && contentFragment!!.javaClass != fragmentType.contentClass) {
            manager.popBackStackImmediate()
        }
    }

    private fun redrawContent() {

        val contentFragment = contentFragment
        contentFragment?.invalidate()

        val navigationFragment = navigationFragment
        navigationFragment?.invalidate()
    }

    private fun switchToFragment(fragmentType: FragmentType?, data: Bundle?) {
        var data = data
        if (!saveInstanceStateCalled) {
            if (data == null) data = Bundle()

            Log.i(TAG, "switch to " + fragmentType!!.name + " with " + data.toString())
            if (fragmentType.isTopLevelFragment) {
                clearBackStack()
            }

            val drawerId = fragmentType.drawerMenuId
            if (drawerId > 0) {
                val item = navigationView!!.menu.findItem(drawerId)
                if (item != null) {
                    item.isChecked = true
                    mSelectedDrawerId = drawerId
                }
            }

            val contentFragment = createContentFragment(fragmentType, data)
            val navigationFragment = createNavigationFragment(fragmentType, data)

            setContent(navigationFragment, contentFragment, !fragmentType.isTopLevelFragment)
        }
    }

    private fun updateTitle() {
        val actionBar = supportActionBar ?: return

        val fm = supportFragmentManager
        var title: CharSequence? = null
        val backstackCount = fm.backStackEntryCount

        if (backstackCount > 0) {
            title = fm.getBackStackEntryAt(backstackCount - 1).breadCrumbTitle
        }
        if (title == null && mSelectedDrawerId > 0) {
            title = navigationView!!.menu.findItem(mSelectedDrawerId).title
        }
        if (title == null) {
            title = getTitle()
        }

        actionBar.setTitle(title)
    }

    private fun createContentFragment(fragmentType: FragmentType?, data: Bundle): BaseFragment? {
        if (fragmentType == null) {
            sendBroadcast(Intent(REDRAW))
            return null
        }
        try {
            val fragmentClass = fragmentType.contentClass
            return createFragmentForClass(data, fragmentClass)
        } catch (e: Exception) {
            Log.e(TAG, "cannot instantiate fragment", e)
            return null
        }

    }

    private fun createNavigationFragment(fragmentType: FragmentType, data: Bundle): BaseFragment? {
        val navigationView = findViewById<FrameLayout?>(R.id.navigation) ?: return null

        try {
            val navigationClass = fragmentType.navigationClass
            if (navigationClass == null) {
                navigationView.visibility = View.GONE
                return null
            }
            navigationView.visibility = View.VISIBLE
            val fragment = createFragmentForClass(data, navigationClass)
            fragment!!.isNavigation = true
            return fragment
        } catch (e: Exception) {
            Log.e(TAG, "cannot instantiate fragment", e)
            return null
        }

    }

    private fun setContent(navigationFragment: BaseFragment?, contentFragment: BaseFragment?, addToBackStack: Boolean) {
        if (saveInstanceStateCalled) return

        val hasNavigation = hasNavigation(navigationFragment, contentFragment)

        val fragmentManager = supportFragmentManager
        if (fragmentManager == null) {
            Log.e(TAG, "fragment manager is null in #setContent")
            return
        }

        // We commit later on. Static code analysis won't notice the call...
        @SuppressLint("CommitTransaction")
        val transaction = fragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.content, contentFragment, CONTENT_TAG)

        if (hasNavigation) {
            transaction.replace(R.id.navigation, navigationFragment, NAVIGATION_TAG)
        }
        if (addToBackStack) {
            transaction.addToBackStack(contentFragment!!.javaClass.name)
            showDrawerToggle(false)
        }

        transaction.setBreadCrumbTitle(contentFragment!!.getTitle(this))
        transaction.commit()

        updateNavigationVisibility(navigationFragment, contentFragment)
        updateTitle()
    }

    @Throws(Exception::class)
    private fun createFragmentForClass(data: Bundle, fragmentClass: Class<out BaseFragment>?): BaseFragment? {
        if (fragmentClass == null) return null

        val fragment = fragmentClass.newInstance()
        fragment.arguments = data
        return fragment
    }

    private fun clearBackStack() {
        val entryCount = supportFragmentManager.backStackEntryCount
        for (i in 0..entryCount - 1) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        if (item.itemId == android.R.id.home) {
            // if the drawer toggle didn't consume the home menu item, this means
            // we disabled it and hence are showing the back button - act accordingly
            onBackPressed()
            return true
        } else if (item.itemId == R.id.menu_refresh) {
            sendBroadcast(Intent(DO_UPDATE).putExtra(DO_REFRESH, true))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NewApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if (packageName == AndFHEMApplication.Companion.PREMIUM_PACKAGE) {
            menu.removeItem(R.id.menu_premium)
        }

        val refreshItem = menu.findItem(R.id.menu_refresh)

        if (refreshLayout!!.isRefreshing) {
            refreshItem.setActionView(R.layout.actionbar_indeterminate_progress)
        } else {
            refreshItem.actionView = null
        }

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.menu_search)?.actionView as SearchView?
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        this.optionsMenu = menu

        return super.onCreateOptionsMenu(menu)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(AndFHEMMainActivity::class.java)

        val TAG = AndFHEMMainActivity::class.java.name
        val NAVIGATION_TAG = "NAVIGATION_TAG"
        val CONTENT_TAG = "CONTENT_TAG"
        private val STATE_DRAWER_ID = "drawer_id"
    }
}
