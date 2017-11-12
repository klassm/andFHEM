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
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SortRoomsAttributeProviderTest {
    private val provider = SortRoomsAttributeProvider()

    @Test
    fun should_provide_sortRooms_for_missing_attribute() {
        val device = FHEMWEBDevice()
        device.xmlListDevice = XmlListDevice("FHEMWEB")

        val result = provider.provideFor(device)

        assertThat(result).isEmpty()
    }

    @Test
    fun should_provide_sortRooms_for_empty_attribute() {
        val device = FHEMWEBDevice()
        device.xmlListDevice = XmlListDevice("FHEMWEB")
        device.xmlListDevice.setAttribute(XmllistKey.Attribute.FhemWeb.sortRooms, "")

        val result = provider.provideFor(device)

        assertThat(result).isEmpty()
    }

    @Test
    fun should_provide_sortRooms_for_attribute() {
        val device = FHEMWEBDevice()
        device.xmlListDevice = XmlListDevice("FHEMWEB")
        device.xmlListDevice.setAttribute(XmllistKey.Attribute.FhemWeb.sortRooms, "bla blub blö")

        val result = provider.provideFor(device)

        assertThat(result).containsExactly("bla", "blub", "blö")
    }
}