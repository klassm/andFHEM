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
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DummyDeviceTest : DeviceXMLParsingBase() {
    @Test
    fun testForCorrectlySetAttributesInOnOffDummy() {
        val device = defaultDevice!!

        assertThat(device.name).isEqualTo(DEFAULT_TEST_DEVICE_NAME)
        assertThat(device.roomConcatenated).isEqualTo(DEFAULT_TEST_ROOM_NAME)

        assertThat(device.state).isEqualTo("on")

        assertThat(device.setList.contains("on", "off")).isEqualTo(true)
    }

    @Test
    fun testForCorrectlySetAttributesInCommonDummy() {
        val device = getDeviceFor("device1")!!

        assertThat(device.name).isEqualTo("device1")
        assertThat(device.roomConcatenated).isEqualTo(DEFAULT_TEST_ROOM_NAME)

        assertThat(device.state).isEqualTo("??")
    }

    @Test
    fun testDeviceWithSetList() {
        val device = getDeviceFor("deviceWithSetlist")!!

        assertThat(device.setList["state", false] as GroupSetListEntry)
                .isEqualTo(GroupSetListEntry("state", listOf("17", "18", "19", "20", "21", "21.5", "22")))
    }

    @Test
    fun testDeviceWithTimer() {
        val device = getDeviceFor("timerDevice")
        assertThat(device).isNotNull()
    }

    @Test
    fun testSliderDevice() {
        val device = getDeviceFor("sliderDevice")!!
        val behavior = DimmableBehavior.behaviorFor(device, null)
        assertThat(behavior!!.dimUpperBound).isEqualTo(50.0)
        assertThat(behavior.dimLowerBound).isEqualTo(10.0)
        assertThat(behavior.dimStep).isEqualTo(2.0)
    }

    @Test
    fun testEventMapDevice() {
        val device = getDeviceFor("eventMapDevice")!!

        val eventMapStates = device.availableTargetStatesEventMapTexts
        assertThat(eventMapStates).isNotNull

        val setList = device.setList
        assertThat(setList).isNotNull()

        assertThat(setList.size()).isEqualTo(eventMapStates.size)

        assertThat(setList.contains("oben", "unten", "65", "40")).isEqualTo(true)

        assertThat(eventMapStates).contains("Oben", "Unten", "Halbschatten", "Vollschatten")
    }

    override fun getFileName() = "dummy.xml"
}
