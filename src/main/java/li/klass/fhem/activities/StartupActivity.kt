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
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.common.base.Optional
import kotlinx.android.synthetic.main.startup.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.login.LoginUIService
import li.klass.fhem.service.intent.LicenseIntentService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.SettingsKeys.UPDATE_ON_APPLICATION_START
import li.klass.fhem.update.backend.RoomListService
import li.klass.fhem.update.backend.RoomListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.FhemResultReceiver
import org.jetbrains.anko.coroutines.experimental.bg
import javax.inject.Inject

class StartupActivity : Activity() {

    @Inject
    lateinit var applicationProperties: ApplicationProperties
    @Inject
    lateinit var roomListUpdateService: RoomListUpdateService
    @Inject
    lateinit var roomListService: RoomListService
    @Inject
    lateinit var favoritesService: FavoritesService
    @Inject
    lateinit var loginUiService: LoginUIService
    @Inject
    lateinit var fcmHistoryService: FcmHistoryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
        val application = application as AndFHEMApplication
        if (application.isAndFHEMAlreadyInstalled) {
            Log.e(TAG, "onCreate() - andFHEM is already installed")
            startActivity(Intent(this, DuplicateInstallActivity::class.java))
            finish()
            return
        }

        (getApplication() as AndFHEMApplication).daggerComponent.inject(this)

        setContentView(R.layout.startup)
    }

    override fun onResume() {
        super.onResume()

        async(UI) {
            bg {
                roomListService.resetUpdateProgress(this@StartupActivity)
            }.await()
        }

        loginUiService.doLoginIfRequired(this, object : LoginUIService.LoginStrategy {
            override fun requireLogin(context: Context, checkLogin: (String) -> Unit) {
                loginStatus.visibility = View.GONE
                loginLayout.visibility = View.VISIBLE

                login.setOnClickListener {
                    val passwordInput = findViewById<EditText>(R.id.password)
                    val password = passwordInput.text.toString()
                    checkLogin(password)
                }
            }

            override fun onLoginSuccess() {
                handleLoginStatus()
            }

            override fun onLoginFailure() {
                finish()
            }
        })
    }

    private val loginLayout: View
        get() = findViewById(R.id.loginForm)

    private fun handleLoginStatus() {
        loginLayout.visibility = View.GONE
        loginStatus.visibility = View.VISIBLE

        initializeGoogleBilling()
    }

    private fun initializeGoogleBilling() {
        setCurrentStatus(R.string.currentStatus_billing)

        startService(Intent(Actions.IS_PREMIUM)
                .setClass(this, LicenseIntentService::class.java)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, object : FhemResultReceiver() {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == ResultCodes.ERROR) {
                            Log.e(TAG, "initializeGoogleBilling() : cannot initialize connection to Google Billing")
                        } else {
                            Log.i(TAG, "initializeGoogleBilling() : connection was initialized")
                        }

                        // we need to continue anyway.
                        loadDeviceList()
                    }
                })
        )
    }

    private val loginStatus: View
        get() = findViewById(R.id.loginStatus)

    private fun loadDeviceList() {
        setCurrentStatus(R.string.currentStatus_loadingDeviceList)

        val updateOnApplicationStart = applicationProperties.getBooleanSharedPreference(UPDATE_ON_APPLICATION_START, false)
        if (updateOnApplicationStart) {
            executeRemoteUpdate()
        }
        deleteOldFcmMessages()
    }

    private fun executeRemoteUpdate() {
        val activityAsContext: Context = this
        async(UI) {
            val result = bg {
                roomListUpdateService.updateAllDevices(Optional.absent(), activityAsContext)
            }.await()

            when (result) {
                is RoomListUpdateService.UpdateResult.Success -> {
                    Log.d(TAG, "loadDeviceList() : device list was loaded")
                    loadFavorites()
                }
                else -> {
                    Log.e(TAG, "loadDeviceList() : cannot load device list")
                    gotoMainActivity(false)
                }
            }
        }
    }

    private fun deleteOldFcmMessages() {
        setCurrentStatus(R.string.currentStatus_deleteFcmHistory)
        val activityAsContext: Context = this
        val retentionDays = Integer.parseInt(applicationProperties.getStringSharedPreference(SettingsKeys.FCM_KEEP_MESSAGES_DAYS, "-1"))

        async(UI) {
            bg {
                fcmHistoryService.deleteContentOlderThan(retentionDays)
            }.await()
            loadFavorites()
        }
    }

    private fun loadFavorites() {
        setCurrentStatus(R.string.currentStatus_loadingFavorites)

        async(UI) {
            val hasFavorites = bg {
                favoritesService.hasFavorites(this@StartupActivity)
            }.await()
            Log.d(TAG, "loadFavorites : favorites_present=" + hasFavorites)
            gotoMainActivity(hasFavorites)
        }
    }

    private fun setCurrentStatus(stringId: Int) {
        (findViewById<TextView>(R.id.currentStatus) as TextView).setText(stringId)
    }

    private fun gotoMainActivity(favoritesPresent: Boolean) {

        startActivity(Intent(this, AndFHEMMainActivity::class.java)
                .putExtra(BundleExtraKeys.HAS_FAVORITES, favoritesPresent))
    }

    companion object {
        private val TAG = StartupActivity::class.java.name
    }
}
