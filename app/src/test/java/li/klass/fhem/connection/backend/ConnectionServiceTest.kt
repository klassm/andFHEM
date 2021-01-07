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
import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import io.mockk.every
import io.mockk.mockk
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.connection.backend.ConnectionService.Companion.DUMMY_DATA_ID
import li.klass.fhem.settings.SettingsKeys.SELECTED_CONNECTION
import li.klass.fhem.util.ApplicationProperties
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class ConnectionServiceTest {
    private val licenseService: LicenseService = mockk()
    private val applicationProperties: ApplicationProperties = mockk()
    private val applicationContext: Context = mockk()
    private val application: Application = mockk()
    private lateinit var connectionService: ConnectionService

    @Before
    fun setUp() {
        every { application.applicationContext } returns applicationContext
        connectionService = ConnectionService(applicationProperties, licenseService, application)
    }

    @Test
    fun testFHEMServerSpecSerializeDeserialize() {
        val serverSpec = FHEMServerSpec("test", ServerType.FHEMWEB, "MyServer")
        serverSpec.url = "http://test.com"
        serverSpec.username = "hallowelt"
        serverSpec.password = "myPassword"
        serverSpec.ip = "192.168.0.1"
        serverSpec.port = 7072

        val json = ConnectionService.serialize(serverSpec)
        assertThat(json, `is`(not(nullValue())))

        val deserialized = ConnectionService.deserialize(json)

        assertThat(deserialized.url, `is`("http://test.com"))
        assertThat(deserialized.username, `is`("hallowelt"))
        assertThat(deserialized.password, `is`("myPassword"))
        assertThat(deserialized.name, `is`("MyServer"))
        assertThat(deserialized.ip, `is`("192.168.0.1"))
        assertThat(deserialized.port, `is`(7072))
        assertThat(deserialized.serverType, `is`(ServerType.FHEMWEB))
    }

    @Test
    @UseDataProvider("portDataProvider")
    fun should_extract_port(spec: FHEMServerSpec, expectedPort: Int) {
        // given
        val sharedPreferences: SharedPreferences = mockk()
        every { applicationProperties.getStringSharedPreference(SELECTED_CONNECTION, DUMMY_DATA_ID) } returns "a"
        every { applicationContext.getSharedPreferences(ConnectionService.PREFERENCES_NAME, Activity.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.contains("a") } returns true
        every { sharedPreferences.getString("a", null) } returns ConnectionService.serialize(spec)
        every { sharedPreferences.all } returns emptyMap()
        every { licenseService.isDebug() } returns false

        // when
        val port = connectionService.getPortOfSelectedConnection()

        // then
        assertThat(port).isEqualTo(expectedPort)
    }

    companion object {

        @DataProvider
        @JvmStatic
        @Suppress("unused")
        fun portDataProvider(): Array<Array<Any>> =
                arrayOf(
                        arrayOf(telnetSpecFor(8043), 8043),
                        arrayOf(fhemwebSpecFor("http://192.168.0.1:8084/fhem"), 8084),
                        arrayOf(fhemwebSpecFor("https://192.168.0.1:8084/fhem"), 8084),
                        arrayOf(fhemwebSpecFor("https://192.168.0.1:8084"), 8084),
                        arrayOf(fhemwebSpecFor("https://192.168.0.1/fhem"), 443),
                        arrayOf(fhemwebSpecFor("http://192.168.0.1/fhem"), 80),
                        arrayOf(dummySpec(), 0)
                )

        private fun telnetSpecFor(port: Int): FHEMServerSpec {
            val spec = FHEMServerSpec("a", ServerType.TELNET, "bla")
            spec.port = port
            return spec
        }

        private fun fhemwebSpecFor(url: String): FHEMServerSpec {
            val spec = FHEMServerSpec("a", ServerType.FHEMWEB, "bla")
            spec.url = url
            return spec
        }

        private fun dummySpec(): FHEMServerSpec = FHEMServerSpec("a", ServerType.DUMMY, "bla")
    }
}
