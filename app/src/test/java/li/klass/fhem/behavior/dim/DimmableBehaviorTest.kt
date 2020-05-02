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

package li.klass.fhem.behavior.dim

import com.tngtech.java.junit.dataprovider.DataProviderRunner
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(DataProviderRunner::class)
class DimmableBehaviorTest {

    @Test
    fun should_create_discrete_behavior() {
        val device = deviceFor("dim10% dim20%")

        val behavior = DimmableBehavior.behaviorFor(device, null)

        assertThat(behavior!!.behavior).isInstanceOf(DiscreteDimmableBehavior::class.java)
        assertThat(behavior.fhemDevice).isSameAs(device)
    }

    @Test
    fun should_create_continuous_behavior() {
        val device = deviceFor("state:slider,0,1,100")

        val behavior = DimmableBehavior.behaviorFor(device, null)

        assertThat(behavior!!.behavior).isInstanceOf(ContinuousDimmableBehavior::class.java)
        assertThat(behavior.fhemDevice).isSameAs(device)
    }

    @Test
    fun should_return_absent_if_neither_continuous_nor_discrete_behavior_applies() {
        val device = deviceFor("on off")

        val result = DimmableBehavior.behaviorFor(device, null)

        assertThat(result).isNull()
    }

    private fun deviceFor(setList: String): FhemDevice {
        return FhemDevice(XmlListDevice(
                "generic", HashMap(), HashMap(), HashMap(),
                mutableMapOf("sets" to DeviceNode(DeviceNode.DeviceNodeType.HEADER, "sets", setList, ""))
        ))
    }
}