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

package li.klass.fhem.domain

import li.klass.fhem.devices.backend.RemotecontrolDeviceService
import li.klass.fhem.domain.core.DeviceXMLParsingBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RemoteControlDeviceTest : DeviceXMLParsingBase() {

    @Test
    fun testAttributesDefaultDevice() {
        val device = defaultDevice!!

        assertThat(device).isNotNull()

        assertThat(stateValueFor(device, "channel")).isEqualTo("kabel eins")
        assertThat(stateValueFor(device, "currentTitle")).isEqualTo("Cold Case - Kein Opfer ist je vergessen")

        val rows = RemotecontrolDeviceService().getRowsFor(device)
        assertThat(rows.size).isEqualTo(14)

        val row0 = rows[0].entries
        assertThat(row0.size).isEqualTo(3)
        assertThat(row0[0].command).isEqualTo("POWEROFF")
        assertThat(row0[0].icon).isEqualTo("/icons/remotecontrol/black_btn_POWEROFF.png")
        assertThat(row0[1].command).isEqualTo("TV")
        assertThat(row0[2].command).isEqualTo("HDMI")

        val row1 = rows[1].entries
        assertThat(row1.size).isEqualTo(3)
        assertThat(row1[0].command).isEqualTo("")
        assertThat(row1[0].icon).isEqualTo("/icons/remotecontrol/black_btn_blank.png")
        assertThat(row1[1].command).isEqualTo("")
        assertThat(row1[2].command).isEqualTo("")

        val row2 = rows[2].entries
        assertThat(row2.size).isEqualTo(3)
        assertThat(row2[0].command).isEqualTo("1")
        assertThat(row2[1].command).isEqualTo("2")
        assertThat(row2[2].command).isEqualTo("3")

        val row3 = rows[3].entries
        assertThat(row3.size).isEqualTo(3)
        assertThat(row3[0].command).isEqualTo("4")
        assertThat(row3[1].command).isEqualTo("5")
        assertThat(row3[2].command).isEqualTo("6")

        val row4 = rows[4].entries
        assertThat(row4.size).isEqualTo(3)
        assertThat(row4[0].command).isEqualTo("7")
        assertThat(row4[1].command).isEqualTo("8")
        assertThat(row4[2].command).isEqualTo("9")

        val row7 = rows[7].entries
        assertThat(row7.size).isEqualTo(3)
        assertThat(row7[0].command).isEqualTo("VOLUP")
        assertThat(row7[1].command).isEqualTo("MUTE")
        assertThat(row7[2].command).isEqualTo("channelUP")
        assertThat(row7[2].icon).isEqualTo("/icons/remotecontrol/black_btn_CHUP.png")
    }

    override fun getFileName(): String = "remotecontrol.xml"
}
