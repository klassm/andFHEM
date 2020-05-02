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

package li.klass.fhem.connection.backend

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.connection.backend.ServerType.*
import li.klass.fhem.domain.core.DeviceVisibility
import li.klass.fhem.settings.SettingsKeys.SELECTED_CONNECTION
import li.klass.fhem.util.ApplicationProperties
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionService @Inject
constructor(private val applicationProperties: ApplicationProperties,
            private val licenseService: LicenseService,
            private val application: Application) {

    private val dummyData: FHEMServerSpec
        get() = DummyServerSpec(DUMMY_DATA_ID, "dummyData.xml", "DummyData")

    private fun getTestData(): FHEMServerSpec? {
        var testData: FHEMServerSpec? = null
        if (licenseService.isDebug()) {
            testData = DummyServerSpec(TEST_DATA_ID, "test.xml", "TestData")
        }
        return testData
    }

    fun create(saveData: SaveData) {
        if (exists(saveData.name)) return
        GlobalScope.launch(Dispatchers.Main) {
            val isPremium = licenseService.isPremium()
            if (isPremium || getCountWithoutDummy() < AndFHEMApplication.PREMIUM_ALLOWED_FREE_CONNECTIONS) {

                val server = FHEMServerSpec(newUniqueId(), saveData.serverType, saveData.name)
                saveData.fillServer(server)
                saveToPreferences(server)
            }
        }
    }

    fun update(id: String, saveData: SaveData) {
        val server = forId(id) ?: return
        saveData.fillServer(server)
        saveToPreferences(server)
    }

    fun exists(id: String?): Boolean =
            mayShowDummyConnections(getAll()) && (DUMMY_DATA_ID == id
                    || TEST_DATA_ID == id)
                    || getPreferences()!!.contains(id)

    private fun mayShowDummyConnections(all: List<FHEMServerSpec>): Boolean {
        val nonDummies = all.filter { it !is DummyServerSpec }
                .toList()
        return nonDummies.isEmpty() || licenseService.isDebug()
    }

    private fun getCountWithoutDummy(): Int {
        val all = getPreferences()!!.all ?: return 0
        return all.size
    }

    private fun newUniqueId(): String {
        var id: String? = null
        while (id == null || exists(id) || DUMMY_DATA_ID == id
                || TEST_DATA_ID == id || MANAGEMENT_DATA_ID == id) {
            id = UUID.randomUUID().toString()
        }

        return id
    }

    private fun saveToPreferences(server: FHEMServerSpec) {
        if (server.serverType == DUMMY) return

        getPreferences()!!.edit().putString(server.id, serialize(server)).apply()
    }

    private fun getPreferences(): SharedPreferences? =
            applicationContext.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE)

    fun forId(id: String): FHEMServerSpec? {
        if (DUMMY_DATA_ID == id) return dummyData
        val testData = getTestData()
        if (TEST_DATA_ID == id && testData != null) return testData

        val json = getPreferences()!!.getString(id, null) ?: return null
        return deserialize(json)
    }

    fun delete(id: String): Boolean {
        if (!exists(id)) return false

        getPreferences()!!.edit().remove(id).apply()

        return true
    }


    fun listAll(): ArrayList<FHEMServerSpec> =
            ArrayList(getAllIncludingDummies())

    private fun getAll(): List<FHEMServerSpec> {
        val servers = mutableListOf<FHEMServerSpec>()

        val preferences = getPreferences() ?: return servers

        val all = preferences.all ?: return servers

        val values = all.values
        values
                .map { it as String }
                .mapTo(servers) { deserialize(it) }

        return servers
    }

    private fun getAllIncludingDummies(): List<FHEMServerSpec> {
        val all = getAll()
        val builder = all.toMutableList()
        if (mayShowDummyConnections(all)) {
            builder.add(dummyData)
            val testData = getTestData()
            if (testData != null) builder.add(testData)
        }
        return builder.toList()
    }

    fun mayShowInCurrentConnectionType(deviceType: String, connectionId: String? = null): Boolean {
        val visibility = deviceTypeVisibility[deviceType] ?: return true
        val serverType = getServerFor(connectionId)?.serverType ?: return false
        if (visibility == DeviceVisibility.NEVER) return false

        val showOnlyIn = visibility.showOnlyIn
        return showOnlyIn == null || serverType == showOnlyIn
    }

    fun getCurrentServer(): FHEMServerSpec? =
            getServerFor(null)

    fun getServerFor(id: String?): FHEMServerSpec? =
            forId(id ?: getSelectedId())

    fun getSelectedId(): String {
        val id = applicationProperties.getStringSharedPreference(SELECTED_CONNECTION, DUMMY_DATA_ID)

        if (!exists(id)) {
            val all = getAllIncludingDummies()
            val connection = all[0]
            return connection.id
        }

        return id!!
    }

    fun setSelectedId(id: String) {
        var idToSet = id
        if (!exists(id)) idToSet = DUMMY_DATA_ID
        applicationProperties.setSharedPreference(SELECTED_CONNECTION, idToSet)
    }

    fun getPortOfSelectedConnection(): Int {
        val spec = getCurrentServer()
        val serverType = spec!!.serverType

        return when (serverType) {
            TELNET -> spec.port
            DUMMY -> 0
            FHEMWEB -> getPortOfFHEMWEBSpec(spec)
        }
    }

    private fun getPortOfFHEMWEBSpec(spec: FHEMServerSpec): Int {
        if (spec.serverType != FHEMWEB) {
            throw IllegalArgumentException("expected FHEMWEB, got " + spec.serverType)
        }
        val explicitPortPattern = Pattern.compile(":([\\d]+)")
        val url = spec.url
        val matcher = explicitPortPattern.matcher(url)
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1))!!
        }

        if (url?.startsWith("https://") == true) return 443
        return if (url?.startsWith("http://") == true) 80 else 0
    }

    private val applicationContext: Context get() = application.applicationContext

    companion object {
        private const val TEST_DATA_ID = "-2"
        const val DUMMY_DATA_ID = "-1"
        const val MANAGEMENT_DATA_ID = "-3"
        const val PREFERENCES_NAME = "fhemConnections"

        private val deviceTypeVisibility = mapOf(
                "FLOORPLAN" to DeviceVisibility.FHEMWEB_ONLY,
                "remotecontrol" to DeviceVisibility.FHEMWEB_ONLY
        )

        internal fun serialize(serverSpec: FHEMServerSpec): String = Gson().toJson(serverSpec)

        internal fun deserialize(json: String): FHEMServerSpec =
                Gson().fromJson(json, FHEMServerSpec::class.java)


    }
}
