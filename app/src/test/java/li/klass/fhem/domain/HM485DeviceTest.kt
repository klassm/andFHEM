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

class HM485DeviceTest : DeviceXMLParsingBase() {

    @Test
    fun testDim() {
        val device = getDeviceFor("dim")
        val dimmableBehavior = DimmableBehavior.behaviorFor(device, null)
        assertThat(dimmableBehavior!!.currentDimPosition).isEqualTo(5.0, Offset.offset(0.01))
    }

    @Test
    fun testSwitch() {
        val device = getDeviceFor("switch")
        assertThat(device).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun testDeviceWithState() {
        val device = getDeviceFor("WZ.Terrassentuer_links")
        assertThat(device.state).isEqualTo("closed")
    }

    override fun getFileName() = "hm485.xml"
}
