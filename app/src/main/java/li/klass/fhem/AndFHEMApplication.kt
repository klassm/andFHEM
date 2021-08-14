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

package li.klass.fhem

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.alexfu.phoenix.Phoenix
import com.google.firebase.FirebaseApp
import dagger.android.support.DaggerApplication
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.activities.StartupActivity
import li.klass.fhem.alarm.clock.update.AlarmClockUpdateService
import li.klass.fhem.billing.BillingService
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.dagger.DaggerApplicationComponent
import li.klass.fhem.dagger.DatabaseModule
import li.klass.fhem.graph.ui.GraphActivity
import li.klass.fhem.settings.SettingsKeys.APPLICATION_VERSION
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import javax.inject.Inject

class AndFHEMApplication : DaggerApplication(), Phoenix.Callback {
    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService

    @Inject
    lateinit var alarmClockUpdateService: AlarmClockUpdateService

    @Inject
    lateinit var connectionService: ConnectionService

    @Inject
    lateinit var billingService: BillingService

    var isUpdate = false
        private set

    var currentApplicationVersion: String? = null
        private set
    lateinit var daggerComponent: ApplicationComponent
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        daggerComponent = createDaggerComponent()
        daggerComponent.inject(this)
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        setDefaultUncaughtExceptionHandler()
        setStrictMode()

        Phoenix.rise(this, this);

        val firebaseApp = FirebaseApp.initializeApp(this)
        firebaseApp?.setAutomaticResourceManagementEnabled(true)

        setApplicationInformation()
        billingService.start(this)

        GlobalScope.launch {
            alarmClockUpdateService.scheduleUpdate()
        }
    }

    override fun onUpdate(oldVersion: Int, newVersion: Int) {
        GlobalScope.launch {
            deviceListUpdateService.checkForCorruptedDeviceList()
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setStrictMode() {
        try {
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .setClassInstanceLimit(GraphActivity::class.java, 3)
                    .setClassInstanceLimit(StartupActivity::class.java, 3)
                    .penaltyLog()
                    .build())

            val builder = StrictMode.ThreadPolicy.Builder()

            StrictMode.setThreadPolicy(builder
                    .detectDiskReads()
                    .detectDiskWrites()
                    .permitDiskReads()
                    .permitDiskWrites()
                    .detectCustomSlowCalls()
                    .detectNetwork()
                    .penaltyLog()
                    .build())
        } catch (e: Exception) {
            Log.v(TAG, "cannot enable strict mode", e)
        }

    }

    private fun createDaggerComponent(): ApplicationComponent =
            DaggerApplicationComponent.builder()
                    .databaseModule(DatabaseModule(this))
                    .application(this)
                    .build()

    private fun setApplicationInformation() {
        val savedVersion = applicationProperties.getStringSharedPreference(APPLICATION_VERSION, null)
        currentApplicationVersion = findOutPackageApplicationVersion()

        if (currentApplicationVersion != savedVersion) {
            isUpdate = true
            applicationProperties.setSharedPreference(APPLICATION_VERSION, currentApplicationVersion!!)
        }
    }

    private fun findOutPackageApplicationVersion(): String = try {
        val pkg = packageName
        packageManager.getPackageInfo(pkg, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        Log.d(AndFHEMApplication::class.java.name, "cannot find the application version", e)
        ""
    }

    companion object {
        val TAG = AndFHEMApplication::class.java.name
        val ANDFHEM_MAIL = "andfhem@klass.li"
        val AD_UNIT_ID = "a14fae70fa236de"
        val INAPP_PREMIUM_ID = "li.klass.fhem.premium"
        val INAPP_PREMIUM_DONATOR_ID = "li.klass.fhem.premiumdonator"
        val PREMIUM_PACKAGE = "li.klass.fhempremium"
        val PREMIUM_ALLOWED_FREE_CONNECTIONS = 1

        var application: AndFHEMApplication? = null
            private set

        private fun setDefaultUncaughtExceptionHandler() {
            try {
                Thread.setDefaultUncaughtExceptionHandler { t, e -> Log.e(TAG, String.format("Uncaught Exception detected in thread %s", t.toString()), e) }
            } catch (e: SecurityException) {
                Log.e(TAG, "Could not set the Default Uncaught Exception Handler", e)
            }

        }

        val androidSDKLevel: Int
            get() = Build.VERSION.SDK_INT
    }

    override fun applicationInjector() = createDaggerComponent()
}
