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
import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.fhem.connection.ServerType
import li.klass.fhem.settings.SettingsKeys.SELECTED_CONNECTION
import li.klass.fhem.testutil.mock
import li.klass.fhem.util.ApplicationProperties
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNot.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

@RunWith(DataProviderRunner::class)
class ConnectionServiceTest {
    val licenseService: LicenseService = mock()
    val applicationProperties: ApplicationProperties = mock()
    private val connectionService = ConnectionService(applicationProperties, licenseService)


    @Test
    fun testFHEMServerSpecSerializeDeserialize() {
        val serverSpec = FHEMServerSpec("test")
        serverSpec.url = "http://test.com"
        serverSpec.username = "hallowelt"
        serverSpec.password = "myPassword"
        serverSpec.name = "MyServer"
        serverSpec.ip = "192.168.0.1"
        serverSpec.port = 7072
        serverSpec.serverType = ServerType.FHEMWEB

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
        val context = mock(Context::class.java)
        val sharedPreferences = mock(SharedPreferences::class.java)
        given(applicationProperties.getStringSharedPreference(ArgumentMatchers.eq(SELECTED_CONNECTION), ArgumentMatchers.anyString(), ArgumentMatchers.eq(context))).willReturn("a")
        given(context.getSharedPreferences(ConnectionService.PREFERENCES_NAME, Activity.MODE_PRIVATE)).willReturn(sharedPreferences)
        given(sharedPreferences.contains("a")).willReturn(true)
        given(sharedPreferences.getString("a", null)).willReturn(ConnectionService.serialize(spec))

        // when
        val port = connectionService.getPortOfSelectedConnection(context)

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
            val spec = FHEMServerSpec("a")
            spec.serverType = ServerType.TELNET
            spec.port = port
            return spec
        }

        private fun fhemwebSpecFor(url: String): FHEMServerSpec {
            val spec = FHEMServerSpec("a")
            spec.serverType = ServerType.FHEMWEB
            spec.url = url
            return spec
        }

        private fun dummySpec(): FHEMServerSpec {
            val spec = FHEMServerSpec("a")
            spec.serverType = ServerType.DUMMY
            return spec
        }
    }
}
