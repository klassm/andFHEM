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

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.content.Intent.ACTION_VIEW
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.navigation.ui.setupWithNavController
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.core.UpdateTimerTask
import li.klass.fhem.activities.drawer.actions.DrawerActions
import li.klass.fhem.billing.BillingService
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.ServerType
import li.klass.fhem.connection.ui.AvailableConnectionDataAdapter
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ScopedFragmentFactory
import li.klass.fhem.databinding.MainViewBinding
import li.klass.fhem.login.LoginUIService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.ui.FragmentType.*
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.util.PermissionUtil
import li.klass.fhem.util.navigation.navController
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

open class AndFHEMMainActivity : AppCompatActivity() {

    inner class Receiver : BroadcastReceiver() {

        val intentFilter = IntentFilter().apply {
            addAction(Actions.DO_UPDATE)
            addAction(Actions.SHOW_EXECUTING_DIALOG)
            addAction(Actions.DISMISS_EXECUTING_DIALOG)
            addAction(Actions.SHOW_TOAST)
            addAction(Actions.SHOW_ALERT)
            addAction(Actions.BACK)
            addAction(Actions.CONNECTIONS_CHANGED)
            addAction(Actions.REDRAW)
            addAction(ACTION_SEARCH)
            addAction(ACTION_VIEW)
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (!saveInstanceStateCalled) {
                runOnUiThread(Runnable {
                    try {
                        val action = intent.action ?: return@Runnable

                        when (action) {
                            Actions.SHOW_EXECUTING_DIALOG -> {
                                updateShowRefreshProgressIcon(true)
                            }
                            Actions.DISMISS_EXECUTING_DIALOG -> {
                                updateShowRefreshProgressIcon(false)
                            }
                            Actions.SHOW_TOAST -> {
                                var content: String? =
                                    intent.getStringExtra(BundleExtraKeys.CONTENT)
                                if (content == null) {
                                    content =
                                        getString(intent.getIntExtra(BundleExtraKeys.STRING_ID, 0))
                                }
                                Toast.makeText(
                                    this@AndFHEMMainActivity,
                                    content,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Actions.SHOW_ALERT -> {
                                DialogUtil.showAlertDialog(
                                    this@AndFHEMMainActivity,
                                    intent.getIntExtra(
                                        BundleExtraKeys.ALERT_TITLE_ID,
                                        R.string.blank
                                    ),
                                    intent.getIntExtra(
                                        BundleExtraKeys.ALERT_CONTENT_ID,
                                        R.string.blank
                                    )
                                )
                            }
                            Actions.CONNECTIONS_CHANGED -> {
                                if (availableConnectionDataAdapter != null) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        launch {
                                            availableConnectionDataAdapter!!.doLoad()
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("exception occurred while receiving broadcast", e)
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

    @Inject
    lateinit var scopedFragmentFactory: ScopedFragmentFactory

    private var broadcastReceiver: Receiver? = null

    private var optionsMenu: Menu? = null

    private var timer: Timer? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private var saveInstanceStateCalled: Boolean = false
    private var mSelectedDrawerId = -1
    private var isRefreshing = false

    private var availableConnectionDataAdapter: AvailableConnectionDataAdapter? = null

    private lateinit var viewBinding: MainViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        PermissionUtil.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        supportFragmentManager.fragmentFactory = scopedFragmentFactory

        themeInitializer.init()

        super.onCreate(savedInstanceState)

        try {
            saveInstanceStateCalled = false
            viewBinding = MainViewBinding.inflate(LayoutInflater.from(this))

            setContentView(viewBinding.root)
            val startDestId = determineStartupFragmentFromProperties()
            if (startDestId != R.id.allDevicesFragment) {
                navController().navigate(startDestId)
            }

            broadcastReceiver = Receiver()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(
                    broadcastReceiver,
                    broadcastReceiver!!.intentFilter,
                    RECEIVER_NOT_EXPORTED
                )
            } else {
                registerReceiver(broadcastReceiver, broadcastReceiver!!.intentFilter)
            }

            initDrawerLayout()

        } catch (e: Throwable) {
            logger.error("onCreate() : error during initialization", e)
        }
    }

    private fun determineStartupFragmentFromProperties(): Int {
        val hasFavorites = intent?.extras?.getBoolean(BundleExtraKeys.HAS_FAVORITES) ?: false
        val startupView = applicationProperties.getStringSharedPreference(
            SettingsKeys.STARTUP_VIEW,
            FAVORITES.name
        )
        var preferencesStartupFragment: FragmentType? =
            FragmentType.forEnumName(startupView) ?: ALL_DEVICES
        logger.debug("handleStartupFragment() : startup view is $preferencesStartupFragment")

        if (preferencesStartupFragment == null) {
            preferencesStartupFragment = ALL_DEVICES
        }

        var fragmentType: FragmentType = preferencesStartupFragment
        if (fragmentType == FAVORITES && !hasFavorites) {
            fragmentType = ALL_DEVICES
        }

        return when (fragmentType) {
            FAVORITES -> R.id.favoritesFragment
            ROOM_LIST -> R.id.roomListFragment
            else -> R.id.allDevicesFragment
        }
    }

    private suspend fun initConnectionSpinner(spinner: View, onConnectionChanged: Runnable) {
        val connectionSpinner = spinner as Spinner
        availableConnectionDataAdapter = AvailableConnectionDataAdapter(
            connectionSpinner,
            onConnectionChanged,
            connectionService
        ) {
            navController().navigate(AndFHEMMainActivityDirections.actionToConnectionList())
        }
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
            viewBinding.navDrawer.menu.findItem(mSelectedDrawerId).isChecked = true
        }
    }

    private fun initDrawerLayout() {
        val navController = navController() ?: return
        viewBinding.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)

        viewBinding.navDrawer.setupWithNavController(navController)
        viewBinding.navDrawer.setNavigationItemSelectedListener { item ->
            val result = drawerActions.handle(this, item.itemId)
            if (result) {
                viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            result
        }
        if (packageName == AndFHEMApplication.PREMIUM_PACKAGE) {
            viewBinding.navDrawer.menu.removeItem(R.id.menu_premium)
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        actionBarDrawerToggle = object : ActionBarDrawerToggle(
            this, viewBinding.drawerLayout,
            R.string.drawerOpen, R.string.drawerClose
        ) {
            override fun onDrawerClosed(view: View) {
                invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                invalidateOptionsMenu()
            }
        }.apply {
            syncState()
        }

        val updateDrawerIndicator = {
            val hasPrevious = navController.previousBackStackEntry != null
            actionBarDrawerToggle.isDrawerIndicatorEnabled = !hasPrevious
        }

        navController.addOnDestinationChangedListener { _, _, _ ->
            updateDrawerIndicator()
        }
        updateDrawerIndicator()

        if (connectionService.getCurrentServer()?.serverType != ServerType.FHEMWEB) {
            viewBinding.navDrawer.menu.removeItem(R.id.fhem_log)
        }

        viewBinding.drawerLayout.addDrawerListener(actionBarDrawerToggle)

        GlobalScope.launch(Dispatchers.Main) {
            initConnectionSpinner(
                viewBinding.navDrawer.getHeaderView(0).findViewById(R.id.connection_spinner)
            ) {
                if (viewBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        broadcastReceiver?.onReceive(this, data)
    }

    override fun onResume() {
        super.onResume()

        logger.info("onResume() : resuming")
        val activity = this
        GlobalScope.launch(Dispatchers.Main) {
            loginUiService.doLoginIfRequired(activity, object : LoginUIService.LoginStrategy {
                override fun requireLogin(context: Context, checkLogin: suspend (String) -> Unit) {
                    val loginView = layoutInflater.inflate(R.layout.login, null)
                    AlertDialog.Builder(context)
                            .setView(loginView)
                            .setTitle(R.string.login)
                            .setOnCancelListener { finish() }
                            .setPositiveButton(R.string.okButton) { _, _ ->
                                val password = (loginView.findViewById<EditText>(R.id.password)).text.toString()
                                GlobalScope.launch(Dispatchers.Main) {
                                    checkLogin(password)
                                }
                            }
                            .show()
                }

                override suspend fun onLoginSuccess() {
                    saveInstanceStateCalled = false

                    if (broadcastReceiver != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            registerReceiver(
                                broadcastReceiver,
                                broadcastReceiver!!.intentFilter,
                                RECEIVER_NOT_EXPORTED
                            )
                        } else {
                            registerReceiver(broadcastReceiver, broadcastReceiver!!.intentFilter)
                        }
                    }

                    if (availableConnectionDataAdapter != null) {
                        availableConnectionDataAdapter!!.doLoad()
                    }
                    handleTimerUpdates()
                }

                override suspend fun onLoginFailure() {
                    finish()
                }
            })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        when (intent.action) {
            ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY) ?: ""
                navController().navigate(
                    AndFHEMMainActivityDirections.actionToSearchResults(query)
                )
            }
            ACTION_VIEW -> {
                intent.getStringExtra(SearchManager.QUERY)?.let { query ->
                    navController().navigate(
                        AndFHEMMainActivityDirections.actionToDeviceDetailRedirect(query, null)
                    )
                }
            }
        }

        navController().handleDeepLink(intent)
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
                logger.info("handleTimerUpdates() - scheduling update every {} minutes", updateInterval / 1000 / 60)
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

        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: IllegalArgumentException) {
            logger.info("onStop() : receiver was not registered, ignore ...")
        }

        updateShowRefreshProgressIcon()
    }

    private fun updateShowRefreshProgressIcon(isRefreshing: Boolean = false) {
        if (optionsMenu == null) return
        this.isRefreshing = isRefreshing

        this.invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        if (viewBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
            sendBroadcast(Intent(Actions.DO_UPDATE).putExtra(BundleExtraKeys.DO_REFRESH, true))
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

        if (isRefreshing) {
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
        private val logger = LoggerFactory.getLogger(AndFHEMMainActivity::class.java)

        private const val STATE_DRAWER_ID = "drawer_id"
    }
}
