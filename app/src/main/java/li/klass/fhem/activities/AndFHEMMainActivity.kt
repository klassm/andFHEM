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
import android.app.AlertDialog
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.Toast
import com.google.common.base.Optional
import kotlinx.android.synthetic.main.main_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.core.UpdateTimerTask
import li.klass.fhem.activities.drawer.actions.DrawerActions
import li.klass.fhem.billing.BillingService
import li.klass.fhem.billing.IsPremiumListener
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.ServerType
import li.klass.fhem.connection.ui.AvailableConnectionDataAdapter
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.Actions.IS_PREMIUM
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH
import li.klass.fhem.constants.BundleExtraKeys.FRAGMENT
import li.klass.fhem.constants.BundleExtraKeys.FRAGMENT_NAME
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.login.LoginUIService
import li.klass.fhem.service.intent.LicenseIntentService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.SettingsKeys.STARTUP_VIEW
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.ui.FragmentType.*
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.widget.SwipeRefreshLayout
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

open class AndFHEMMainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener,
        android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshLayout.ChildScrollDelegate {

    inner class Receiver : BroadcastReceiver() {

        val intentFilter = IntentFilter().apply {
            addAction(SHOW_FRAGMENT)
            addAction(DO_UPDATE)
            addAction(UPDATE_NAVIGATION)
            addAction(SHOW_EXECUTING_DIALOG)
            addAction(DISMISS_EXECUTING_DIALOG)
            addAction(SHOW_TOAST)
            addAction(SHOW_ALERT)
            addAction(BACK)
            addAction(CONNECTIONS_CHANGED)
            addAction(REDRAW)
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (!saveInstanceStateCalled) {
                runOnUiThread(Runnable {
                    try {
                        val action = intent.action ?: return@Runnable

                        if (SHOW_FRAGMENT == action) {
                            val bundle = intent.extras
                                    ?: throw IllegalArgumentException("need a content fragment")
                            val fragmentType: FragmentType?
                            fragmentType = if (bundle.containsKey(FRAGMENT)) {
                                bundle.getSerializable(FRAGMENT) as FragmentType
                            } else {
                                val fragmentName = bundle.getString(FRAGMENT_NAME)
                                getFragmentFor(fragmentName)
                            }
                            drawer_layout.closeDrawer(GravityCompat.START)
                            switchToFragment(fragmentType, intent.extras)
                        } else if (action == DO_UPDATE) {
                            updateShowRefreshProgressIcon()
                            refreshFragments(intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false))
                        } else if (action == UPDATE_NAVIGATION) {
                            GlobalScope.launch {
                                refreshNavigation()
                            }
                        } else if (action == SHOW_EXECUTING_DIALOG) {
                            updateShowRefreshProgressIcon()
                            refresh_layout?.isRefreshing = true
                        } else if (action == DISMISS_EXECUTING_DIALOG) {
                            updateShowRefreshProgressIcon()
                            refresh_layout?.isRefreshing = false
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
                        LOGGER.error("exception occurred while receiving broadcast", e)
                        LOGGER.error("exception occurred while receiving broadcast", e)
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
    lateinit var loginUiService: LoginUIService
    @Inject
    lateinit var connectionService: ConnectionService
    @Inject
    lateinit var licenseService: LicenseService
    @Inject
    lateinit var themeInitializer: ThemeInitializer
    @Inject
    lateinit var drawerActions: DrawerActions

    private var broadcastReceiver: Receiver? = null

    private var optionsMenu: Menu? = null

    private var timer: Timer? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private var saveInstanceStateCalled: Boolean = false
    private var mSelectedDrawerId = -1

    private var availableConnectionDataAdapter: AvailableConnectionDataAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val application = application as AndFHEMApplication
        application.daggerComponent.inject(this)
        themeInitializer.init()

        super.onCreate(savedInstanceState)

        try {
            initialize(savedInstanceState)
        } catch (e: Throwable) {
            LOGGER.error("onCreate() : error during initialization", e)
        }
    }

    private fun initialize(savedInstanceState: Bundle?) {
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
                FragmentType.FAVORITES.name)
        var preferencesStartupFragment: FragmentType? = FragmentType.forEnumName(startupView)
        LOGGER.debug("handleStartupFragment() : startup view is $preferencesStartupFragment")

        if (preferencesStartupFragment == null) {
            preferencesStartupFragment = ALL_DEVICES
        }

        var fragmentType: FragmentType = preferencesStartupFragment
        if (fragmentType == FAVORITES && !hasFavorites) {
            fragmentType = ALL_DEVICES
        }


        val startupBundle = Bundle()
        LOGGER.info("handleStartupFragment () : startup with $fragmentType (extras: $startupBundle)")
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
            nav_drawer.menu.findItem(mSelectedDrawerId).isChecked = true
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
        drawer_layout.closeDrawer(GravityCompat.START)

        if (drawerActions.handle(this, menuItem.itemId)) {
            return true
        }

        val fragmentType = FragmentType.getFragmentFor(menuItem.itemId) ?: return false
        switchToFragment(fragmentType, Bundle())

        return true
    }

    private fun showDrawerToggle(enable: Boolean) {
        actionBarDrawerToggle.isDrawerIndicatorEnabled = enable
    }

    private fun initDrawerLayout() {
        drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)

        nav_drawer.setNavigationItemSelectedListener(this)
        if (packageName == AndFHEMApplication.PREMIUM_PACKAGE) {
            nav_drawer.menu.removeItem(R.id.menu_premium)
        }

        if (connectionService.getCurrentServer()?.serverType != ServerType.FHEMWEB) {
            nav_drawer.menu.removeItem(R.id.fhem_log)
        }

        licenseService.isPremium(object : IsPremiumListener {
            override fun isPremium(isPremium: Boolean) {
                if (!isPremium) {
                    nav_drawer.menu.removeItem(R.id.fcm_history)
                }
            }
        })

        initConnectionSpinner(nav_drawer.getHeaderView(0).findViewById(R.id.connection_spinner),
                Runnable {
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                })

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        actionBarDrawerToggle = object : ActionBarDrawerToggle(this, drawer_layout,
                R.string.drawerOpen, R.string.drawerClose) {
            override fun onDrawerClosed(view: View) {
                invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                invalidateOptionsMenu()
            }
        }
        drawer_layout.addDrawerListener(actionBarDrawerToggle)
    }

    private fun initSwipeRefreshLayout() {
        val activity = this
        refresh_layout?.apply {
            setOnRefreshListener(activity)
            setChildScrollDelegate(activity)
            setColorSchemeColors(
                    ContextCompat.getColor(activity, R.color.primary), 0,
                    ContextCompat.getColor(activity, R.color.accent), 0)
        }
    }

    override fun canChildScrollUp(): Boolean {
        val content = contentFragment
        return content != null && content.canChildScrollUp()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
        updateNavigationVisibility()
        contentFragment!!.invalidate()
        if (navigationFragment != null) {
            navigationFragment!!.invalidate()
        }
    }

    override fun onRefresh() {
        refresh_layout?.isRefreshing = true
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

        LOGGER.info("onResume() : resuming")
        val activity = this
        GlobalScope.launch(Dispatchers.Main) {
            loginUiService.doLoginIfRequired(activity, object : LoginUIService.LoginStrategy {
                override suspend fun requireLogin(context: Context, checkLogin: suspend (String) -> Unit) {
                    coroutineScope {
                        val loginView = layoutInflater.inflate(R.layout.login, null)
                        AlertDialog.Builder(context)
                                .setView(loginView)
                                .setTitle(R.string.login)
                                .setOnCancelListener { finish() }
                                .setPositiveButton(R.string.okButton) { _, _ -> launch { checkLogin((loginView.findViewById<EditText>(R.id.password)).text.toString()) } }
                                .show()
                    }
                }

                override suspend fun onLoginSuccess() {
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

                override suspend fun onLoginFailure() {
                    finish()
                }
            })
        }
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
        GlobalScope.launch {
            refreshContent(doUpdate)
            refreshNavigation()
        }
    }

    private suspend fun refreshNavigation() {
        val nav = navigationFragment
        nav?.update(false)
    }

    private suspend fun refreshContent(doUpdate: Boolean) {
        val content = contentFragment
        content?.update(doUpdate)
    }

    private fun handleTimerUpdates() {
        // We post this delayed, as otherwise we will block the application startup (causing
        // ugly ANRs).
        Handler().post {
            val autoUpdateTime = applicationProperties.getStringSharedPreference(SettingsKeys.AUTO_UPDATE_TIME,
                    "-1")!!
            val updateInterval = Integer.valueOf(autoUpdateTime)

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
            LOGGER.info("onStop() : receiver was not registered, ignore ...")
        }

        refresh_layout?.isRefreshing = false
        updateShowRefreshProgressIcon()
    }

    private fun updateShowRefreshProgressIcon() {
        if (optionsMenu == null) return

        this.invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
        var myData = data
        if (!saveInstanceStateCalled) {
            if (myData == null) myData = Bundle()

            LOGGER.info("switch to " + fragmentType!!.name + " with " + myData.toString())
            if (fragmentType.isTopLevelFragment) {
                clearBackStack()
            }

            val drawerId = fragmentType.drawerMenuId
            if (drawerId > 0) {
                val item = nav_drawer.menu.findItem(drawerId)
                if (item != null) {
                    item.isChecked = true
                    mSelectedDrawerId = drawerId
                }
            }

            val contentFragment = createContentFragment(fragmentType, myData)
            val navigationFragment = createNavigationFragment(fragmentType, myData)

            setContent(navigationFragment, contentFragment!!, !fragmentType.isTopLevelFragment)
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
            title = nav_drawer.menu.findItem(mSelectedDrawerId).title
        }
        if (title == null) {
            title = getTitle()
        }

        actionBar.title = title
    }

    private fun createContentFragment(fragmentType: FragmentType?, data: Bundle): BaseFragment? {
        if (fragmentType == null) {
            sendBroadcast(Intent(REDRAW))
            return null
        }
        return try {
            val fragmentClass = fragmentType.contentClass
            createFragmentForClass(data, fragmentClass)
        } catch (e: Exception) {
            LOGGER.error("cannot instantiate fragment", e)
            null
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
            LOGGER.error("cannot instantiate fragment", e)
            return null
        }

    }

    private fun setContent(navigationFragment: BaseFragment?, contentFragment: BaseFragment, addToBackStack: Boolean) {
        if (saveInstanceStateCalled) return

        val hasNavigation = hasNavigation(navigationFragment, contentFragment)

        val fragmentManager = supportFragmentManager
        if (fragmentManager == null) {
            LOGGER.error("fragment manager is null in #setContent")
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
            transaction.replace(R.id.navigation, navigationFragment!!, NAVIGATION_TAG)
        }
        if (addToBackStack) {
            transaction.addToBackStack(contentFragment.javaClass.name)
            showDrawerToggle(false)
        }

        transaction.setBreadCrumbTitle(contentFragment.getTitle(this))
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
        for (i in 0 until entryCount) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
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
        if (packageName == AndFHEMApplication.PREMIUM_PACKAGE) {
            menu.removeItem(R.id.menu_premium)
        }

        val refreshItem = menu.findItem(R.id.menu_refresh)

        if (refresh_layout?.isRefreshing == true) {
            refreshItem.setActionView(R.layout.actionbar_indeterminate_progress)
        } else {
            refreshItem.actionView = null
        }

        attachSearchView(menu)
        this.optionsMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    private fun attachSearchView(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.menu_search)
        val searchView = searchMenuItem?.actionView as SearchView?
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = false

            override fun onSuggestionClick(position: Int): Boolean {
                searchMenuItem?.collapseActionView()
                return false
            }
        })
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchMenuItem?.collapseActionView()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    companion object {

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(AndFHEMMainActivity::class.java)

        private const val NAVIGATION_TAG = "NAVIGATION_TAG"
        private const val CONTENT_TAG = "CONTENT_TAG"
        private const val STATE_DRAWER_ID = "drawer_id"
    }
}
