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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.startup.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.startup.actions.StartupActions
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ScopedFragmentFactory
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.login.LoginUIService
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import javax.inject.Inject

class StartupActivity : Activity() {

    @Inject
    lateinit var applicationProperties: ApplicationProperties
    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService
    @Inject
    lateinit var deviceListService: DeviceListService
    @Inject
    lateinit var favoritesService: FavoritesService
    @Inject
    lateinit var loginUiService: LoginUIService
    @Inject
    lateinit var fcmHistoryService: FcmHistoryService
    @Inject
    lateinit var appWidgetUpdateService: AppWidgetUpdateService
    @Inject
    lateinit var startupActions: StartupActions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }

        setContentView(R.layout.startup)
    }

    override fun onResume() {
        super.onResume()

        val activity = this


        GlobalScope.launch(Dispatchers.Main) {
            withContext(IO) {
                deviceListService.resetUpdateProgress(this@StartupActivity)
            }

            loginUiService.doLoginIfRequired(activity, object : LoginUIService.LoginStrategy {
                override fun requireLogin(context: Context, checkLogin: suspend (String) -> Unit) {
                    loginStatus.visibility = View.GONE
                    loginLayout.visibility = View.VISIBLE

                    login.setOnClickListener {
                        val passwordInput = findViewById<EditText>(R.id.password)
                        val password = passwordInput.text.toString()
                        GlobalScope.launch(Dispatchers.Main) {
                            checkLogin(password)
                        }
                    }
                }

                override suspend fun onLoginSuccess() = handleLoginStatus()

                override suspend fun onLoginFailure() = finish()
            })
        }
    }

    private val loginLayout: View
        get() = findViewById(R.id.loginForm)

    private fun handleLoginStatus() {
        loginLayout.visibility = View.GONE
        loginStatus.visibility = View.VISIBLE

        handleStartupActions()
    }

    private fun handleStartupActions() {
        GlobalScope.launch(Dispatchers.Main) {
            startupActions.actions.map {
                setCurrentStatus(it.statusText)
                async(IO) {
                    try {
                        it.run()
                    } catch (e: Exception) {
                        logger.error("handleStartupActions - error while running ${it.javaClass.name}", e)
                    }
                }
            }.awaitAll()

            showMainActivity()
        }
    }

    private suspend fun showMainActivity() {
        setCurrentStatus(R.string.currentStatus_loadingFavorites)

        coroutineScope {
            val hasFavorites = withContext(IO) {
                favoritesService.hasFavorites()
            }
            logger.debug("showMainActivity : favorites_present=$hasFavorites")
            gotoMainActivity(hasFavorites)
        }
    }

    private fun setCurrentStatus(stringId: Int) {
        (findViewById<TextView>(R.id.currentStatus)).setText(stringId)
    }

    private fun gotoMainActivity(favoritesPresent: Boolean) {
        startActivity(Intent(this, AndFHEMMainActivity::class.java)
                .putExtra(BundleExtraKeys.HAS_FAVORITES, favoritesPresent))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StartupActivity::class.java)
    }
}
