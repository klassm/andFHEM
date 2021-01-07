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
import io.mockk.every
import io.mockk.mockk
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

        val context: Context = mockk()
        every { context.getString(DIMMER.captionId) } returns "dimmer"
        every { context.getString(SWITCH.captionId) } returns "switch"
        every { context.getString(FHEM.captionId) } returns "fhem"
        every { context.getString(UNKNOWN.captionId) } returns "unknown"

        val application: Application = mockk()
        every { application.applicationContext }.returns(context)


        val deviceGroupHolder: DeviceGroupHolder = mockk()
        every { deviceGroupHolder.getVisible(context) } returns listOf(DIMMER, SWITCH)
        every { deviceGroupHolder.getInvisible(context) } returns listOf(FHEM, UNKNOWN)

        val fhemWebConfigurationService: FhemWebConfigurationService = mockk()
        every { fhemWebConfigurationService.getColumnAttributeFor(room) } returns listOf("fhem", "switch", "blub")

        val roomDeviceList: RoomDeviceList = mockk()
        every { roomDeviceList.roomName } returns room
        every { roomDeviceList.getDeviceGroups() } returns setOf("blub", "blö", "abc")

        // when
        val result = ParentsProvider(deviceGroupHolder, fhemWebConfigurationService, application)
                .parentsFor(roomDeviceList)

        // then
        assertThat(result).containsExactly("switch", "blub", "dimmer", "abc", "blö")
    }
}