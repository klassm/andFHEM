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

import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.domain.core.DeviceXMLParsingBase
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

class ReadingsProxyDeviceTest : DeviceXMLParsingBase() {

    @Test
    fun testRGBProxy() {
        val device = getDeviceFor("rgb")

        assertThat(device).isNotNull()
        assertThat(device.name).isEqualTo("rgb")
        assertThat(device.xmlListDevice.getState("rgb", true)).isEqualTo("FFFF00")
    }

    @Test
    fun testSwitchProxy() {
        val device = getDeviceFor("Bild")

        assertThat(device).isNotNull()
        assertThat(device.xmlListDevice.getState("rgb", true)).isNull()
    }

    @Test
    fun testDimmable() {
        val device = getDeviceFor("dimmable")

        assertThat(device).isNotNull()
        val dimmableBehavior = DimmableBehavior.behaviorFor(device, null)
        assertThat(dimmableBehavior!!.currentDimPosition).isEqualTo(100.0, Offset.offset(0.01))
        assertThat(dimmableBehavior.dimLowerBound).isEqualTo(4.0)
        assertThat(dimmableBehavior.dimUpperBound).isEqualTo(105.0)
        assertThat(dimmableBehavior.dimStep).isEqualTo(1.0)
    }

    override fun getFileName() = "readings_proxy.xml"
}
