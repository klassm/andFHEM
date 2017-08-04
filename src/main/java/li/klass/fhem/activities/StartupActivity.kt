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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.common.base.Optional
import com.google.common.base.Strings.isNullOrEmpty
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.PreferenceKeys.STARTUP_PASSWORD
import li.klass.fhem.constants.PreferenceKeys.UPDATE_ON_APPLICATION_START
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.service.intent.FavoritesIntentService
import li.klass.fhem.service.intent.LicenseIntentService
import li.klass.fhem.service.room.RoomListService
import li.klass.fhem.service.room.RoomListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
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

        if (!isNullOrEmpty(password)) {
            showLoginDialog()
        } else {
            handleLoginStatus()
        }
    }

    private fun showLoginDialog() {
        loginStatus.visibility = View.GONE
        loginLayout.visibility = View.VISIBLE

        val loginButton = findViewById(R.id.login) as Button
        loginButton.setOnClickListener {
            val passwordInput = findViewById(R.id.password) as EditText
            val password = passwordInput.text.toString()
            if (password == password) {
                handleLoginStatus()
            } else {
                DialogUtil.showAlertDialog(this@StartupActivity, null,
                        getString(R.string.wrongPassword))
            }
            passwordInput.setText("")
        }
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

        val updateOnApplicationStart = applicationProperties.getBooleanSharedPreference(UPDATE_ON_APPLICATION_START, false, this)
        if (updateOnApplicationStart) {
            executeRemoteUpdate()
        } else {
            loadFavorites()
        }
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

    private fun loadFavorites() {
        setCurrentStatus(R.string.currentStatus_loadingFavorites)

        startService(Intent(Actions.FAVORITES_PRESENT)
                .setClass(this, FavoritesIntentService::class.java)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, object : FhemResultReceiver() {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultData == null || resultCode == ResultCodes.ERROR) {
                            Log.e(TAG, "loadFavorites : cannot load favorites: " + resultData)
                        } else {
                            val favoritesPresent = resultData.getBoolean(BundleExtraKeys.HAS_FAVORITES)
                            Log.d(TAG, "loadFavorites : favorites_present=" + favoritesPresent)
                            gotoMainActivity(favoritesPresent)
                        }
                    }
                }))
    }

    private fun setCurrentStatus(stringId: Int) {
        (findViewById(R.id.currentStatus) as TextView).setText(stringId)
    }

    private val password: String
        get() = applicationProperties.getStringSharedPreference(STARTUP_PASSWORD, "", this)

    private fun gotoMainActivity(favoritesPresent: Boolean) {

        startActivity(Intent(this, AndFHEMMainActivity::class.java)
                .putExtra(BundleExtraKeys.HAS_FAVORITES, favoritesPresent))
    }

    companion object {
        private val TAG = StartupActivity::class.java.name
    }
}
