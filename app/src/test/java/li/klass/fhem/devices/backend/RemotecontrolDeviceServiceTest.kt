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

package li.klass.fhem.devices.backend

import li.klass.fhem.devices.backend.RemotecontrolDeviceService.Entry
import li.klass.fhem.devices.backend.RemotecontrolDeviceService.Row
import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.Test

class RemotecontrolDeviceServiceTest {
    @Test
    fun should_create_rows() {
        // given
        val device = GenericDevice()
        val iconPath = "myIconPath"
        val iconPrefix = "myIconPrefix"
        val attributes = mapOf(
                "row01" to nodeOf("bla,blub,blu:icon"),
                "row00" to nodeOf("on,off"),
                "row02" to nodeOf(""),
                "rc_iconpath" to nodeOf(iconPath),
                "rc_iconprefix" to nodeOf(iconPrefix)
        )
        device.xmlListDevice = XmlListDevice("remotecontrol", attributes = attributes.toMutableMap())

        // when
        val result = RemotecontrolDeviceService().getRowsFor(device)

        // then
        assertThat(result).containsExactly(
                Row(0, listOf(
                        Entry("on", "/$iconPath/${iconPrefix}on.png"),
                        Entry("off", "/$iconPath/${iconPrefix}off.png")
                )),
                Row(1, listOf(
                        Entry("bla", "/$iconPath/${iconPrefix}bla.png"),
                        Entry("blub", "/$iconPath/${iconPrefix}blub.png"),
                        Entry("blu", "/$iconPath/${iconPrefix}icon.png")
                ))
        )
    }

    private fun nodeOf(content: String): DeviceNode =
            DeviceNode(DeviceNode.DeviceNodeType.ATTR, "", content, DateTime.now())
}