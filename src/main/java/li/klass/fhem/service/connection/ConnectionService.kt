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

package li.klass.fhem.service.connection

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.common.base.Optional
import com.google.common.base.Preconditions.checkArgument
import com.google.common.collect.FluentIterable.from
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists.newArrayList
import com.google.gson.Gson
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.domain.core.DeviceType
import li.klass.fhem.domain.core.DeviceVisibility
import li.klass.fhem.fhem.connection.DummyServerSpec
import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.fhem.connection.ServerType
import li.klass.fhem.fhem.connection.ServerType.FHEMWEB
import li.klass.fhem.settings.SettingsKeys.SELECTED_CONNECTION
import li.klass.fhem.util.ApplicationProperties
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionService @Inject
constructor(val applicationProperties: ApplicationProperties,
            val licenseIntentService: LicenseService) {

    private fun getTestData(context: Context): FHEMServerSpec? {
        var testData: FHEMServerSpec? = null
        if (LicenseService.isDebug(context)) {
            testData = DummyServerSpec(TEST_DATA_ID, "test.xml")
            testData.name = "TestData"
            testData.serverType = ServerType.DUMMY
        }
        return testData
    }

    private val dummyData: FHEMServerSpec
        get() {
            val dummyData = DummyServerSpec(DUMMY_DATA_ID, "dummyData.xml")
            dummyData.name = "DummyData"
            dummyData.serverType = ServerType.DUMMY
            return dummyData
        }

    fun create(saveData: SaveData, context: Context) {
        if (exists(saveData.name, context)) return
        licenseIntentService.isPremium({ isPremium ->
            if (isPremium || getCountWithoutDummy(context) < AndFHEMApplication.Companion.PREMIUM_ALLOWED_FREE_CONNECTIONS) {

                val server = FHEMServerSpec(newUniqueId(context))
                saveData.fillServer(server)
                saveToPreferences(server, context)
            }
        }, context)
    }

    fun update(id: String, saveData: SaveData, context: Context) {
        val server = forId(id, context) ?: return
        saveData.fillServer(server)
        saveToPreferences(server, context)
    }

    fun exists(id: Optional<String>, context: Context): Boolean =
            !id.isPresent || exists(id.get(), context)

    fun exists(id: String?, context: Context): Boolean =
            mayShowDummyConnections(context, getAll(context)) && (DUMMY_DATA_ID == id || TEST_DATA_ID == id) || getPreferences(context)!!.contains(id)

    private fun mayShowDummyConnections(context: Context, all: List<FHEMServerSpec>): Boolean {
        val nonDummies = from(all)
                .filter(FHEMServerSpec.notInstanceOf(DummyServerSpec::class.java))
                .toList()
        return nonDummies.isEmpty() || LicenseService.isDebug(context)
    }

    private fun getCountWithoutDummy(context: Context): Int {
        val all = getPreferences(context)!!.all ?: return 0
        return all.size
    }

    private fun newUniqueId(context: Context): String {
        var id: String? = null
        while (id == null || exists(id, context) || DUMMY_DATA_ID == id
                || TEST_DATA_ID == id || MANAGEMENT_DATA_ID == id) {
            id = UUID.randomUUID().toString()
        }

        return id
    }

    private fun saveToPreferences(server: FHEMServerSpec, context: Context) {
        if (server.serverType == ServerType.DUMMY) return

        getPreferences(context)!!.edit().putString(server.id, serialize(server)).apply()
    }

    private fun getPreferences(context: Context): SharedPreferences? =
            context.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE)

    fun forId(id: String, context: Context): FHEMServerSpec? {
        if (DUMMY_DATA_ID == id) return dummyData
        val testData = getTestData(context)
        if (TEST_DATA_ID == id && testData != null) return testData

        val json = getPreferences(context)!!.getString(id, null) ?: return null
        return deserialize(json)
    }

    fun delete(id: String, context: Context): Boolean {
        if (!exists(id, context)) return false

        getPreferences(context)!!.edit().remove(id).apply()

        return true
    }


    fun listAll(context: Context): ArrayList<FHEMServerSpec> =
            ArrayList(getAllIncludingDummies(context))

    private fun getAll(context: Context): List<FHEMServerSpec> {
        val servers = newArrayList<FHEMServerSpec>()

        val preferences = getPreferences(context) ?: return servers

        val all = preferences.all ?: return servers

        val values = all.values
        values
                .map { it as String }
                .mapTo(servers) { deserialize(it) }

        return servers
    }

    private fun getAllIncludingDummies(context: Context): List<FHEMServerSpec> {
        val all = getAll(context)
        var builder: ImmutableList.Builder<FHEMServerSpec> = ImmutableList.builder<FHEMServerSpec>()
                .addAll(all)
        if (mayShowDummyConnections(context, all)) {
            builder = builder.add(dummyData)
            val testData = getTestData(context)
            if (testData != null) builder = builder.add(testData)
        }
        return builder.build()
    }

    fun mayShowInCurrentConnectionType(deviceType: DeviceType, context: Context): Boolean {
        val visibility = deviceType.visibility ?: return true

        val serverType = getCurrentServer(context)!!.serverType
        if (visibility == DeviceVisibility.NEVER) return false

        val showOnlyIn = visibility.showOnlyIn
        return showOnlyIn == null || serverType == showOnlyIn
    }

    fun getCurrentServer(context: Context): FHEMServerSpec? =
            getServerFor(context, Optional.absent())

    fun getServerFor(context: Context, id: Optional<String>): FHEMServerSpec? =
            forId(id.or(getSelectedId(context)), context)

    fun getSelectedId(context: Context): String {
        val id = applicationProperties.getStringSharedPreference(SELECTED_CONNECTION, DUMMY_DATA_ID, context)

        if (!exists(id, context)) {
            val all = getAllIncludingDummies(context)
            val connection = all[0]
            return connection.id
        }

        return id!!
    }

    fun setSelectedId(id: String, context: Context) {
        var idToSet = id
        if (!exists(id, context)) idToSet = DUMMY_DATA_ID
        applicationProperties.setSharedPreference(SELECTED_CONNECTION, idToSet, context)
    }

    fun getPortOfSelectedConnection(context: Context): Int {
        val spec = getCurrentServer(context)
        val serverType = spec!!.serverType

        return when (serverType) {
            ServerType.TELNET -> spec.port
            ServerType.DUMMY -> 0
            FHEMWEB -> getPortOfFHEMWEBSpec(spec)

            else -> throw IllegalArgumentException("unknown spec type: " + spec.serverType)
        }
    }

    private fun getPortOfFHEMWEBSpec(spec: FHEMServerSpec): Int {
        checkArgument(spec.serverType == FHEMWEB)
        val explicitPortPattern = Pattern.compile(":([\\d]+)")
        val url = spec.url
        val matcher = explicitPortPattern.matcher(url)
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1))!!
        }

        if (url.startsWith("https://")) {
            return 443
        }

        return if (url.startsWith("http://")) {
            80
        } else 0

    }

    companion object {
        val DUMMY_DATA_ID = "-1"
        private val TEST_DATA_ID = "-2"
        val MANAGEMENT_DATA_ID = "-3"
        private val GSON = Gson()
        val PREFERENCES_NAME = "fhemConnections"

        internal fun serialize(serverSpec: FHEMServerSpec): String = GSON.toJson(serverSpec)

        internal fun deserialize(json: String): FHEMServerSpec =
                GSON.fromJson(json, FHEMServerSpec::class.java)
    }
}
