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

package li.klass.fhem.devices.list.backend

import android.app.Application
import android.content.Context
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import li.klass.fhem.domain.core.DeviceFunctionality.*
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import li.klass.fhem.widget.deviceFunctionality.DeviceGroupHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ParentsProviderTest {
    @Test
    fun should_provide_sorted_parents() {
        // given
        val room = "myRoom"

        val context = mock<Context> {
            on { getString(DIMMER.captionId) } doReturn "dimmer"
            on { getString(SWITCH.captionId) } doReturn "switch"
            on { getString(FHEM.captionId) } doReturn "fhem"
            on { getString(UNKNOWN.captionId) } doReturn "unknown"
        }
        val application = mock<Application> { on { applicationContext } doReturn context }

        val deviceGroupHolder = mock<DeviceGroupHolder> {
            on { getVisible(context) } doReturn listOf(DIMMER, SWITCH)
            on { getInvisible(context) } doReturn listOf(FHEM, UNKNOWN)
        }

        val fhemWebConfigurationService = mock<FhemWebConfigurationService> {
            on { getColumnAttributeFor(room) } doReturn listOf("fhem", "switch", "blub")
        }

        val roomDeviceList = mock<RoomDeviceList> {
            on { roomName } doReturn room
            on { getDeviceGroups() } doReturn setOf("blub", "blö", "abc")
        }

        // when
        val result = ParentsProvider(deviceGroupHolder, fhemWebConfigurationService, application)
                .parentsFor(roomDeviceList)

        // then
        assertThat(result).containsExactly("switch", "blub", "dimmer", "abc", "blö")
    }
}