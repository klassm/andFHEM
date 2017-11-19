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

package li.klass.fhem.update.backend.fhemweb

import li.klass.fhem.constants.XmllistKey
import li.klass.fhem.domain.FHEMWEBDevice
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ColumnAttributeProviderTest {
    private val provider = ColumnAttributeProvider()

    @Test
    fun should_calculate_columns() {
        val device = deviceWithColumn("Julian:Szenen,Lampen,Geräte Küche:FHT,Lampen,Geräte Schlafzimmer:Szenen,FS20,Geräte")

        assertThat(provider.getFor(device, "Julian")).containsExactly("Szenen", "Lampen", "Geräte")
        assertThat(provider.getFor(device, "Küche")).containsExactly("FHT", "Lampen", "Geräte")
        assertThat(provider.getFor(device, "Schlafzimmer")).containsExactly("Szenen", "FS20", "Geräte")
    }

    @Test
    fun should_handle_column_values() {
        val device = deviceWithColumn("LivingRoom:FS20,notify|FHZ,IT DiningRoom:FS20|FHZ")

        assertThat(provider.getFor(device, "LivingRoom")).containsExactly("FS20", "notify", "FHZ", "IT")
        assertThat(provider.getFor(device, "DiningRoom")).containsExactly("FS20", "FHZ")
    }

    @Test
    fun should_handle_regexp_rooms() {
        val device = deviceWithColumn(".*:FS20,notify|FHZ,IT L.*:FS20|FHZ|notify,LightScene")

        assertThat(provider.getFor(device, "Lub")).containsExactly("FS20", "notify", "FHZ", "IT", "LightScene")
        assertThat(provider.getFor(device, "Blub")).containsExactly("FS20", "notify", "FHZ", "IT")
    }

    private fun deviceWithColumn(column: String): FHEMWEBDevice {
        val device = FHEMWEBDevice()
        device.xmlListDevice = XmlListDevice("FHEMWEB", mutableMapOf<String, DeviceNode>(), mutableMapOf<String, DeviceNode>(), mutableMapOf<String, DeviceNode>(), mutableMapOf<String, DeviceNode>())
        device.xmlListDevice.setAttribute(XmllistKey.Attribute.FhemWeb.column, column)
        return device
    }
}