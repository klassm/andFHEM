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

package li.klass.fhem.domain;

import org.junit.Test;

import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInOnOffDummy() {
        FhemDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("on");

        assertThat(device.getSetList().contains("on", "off")).isEqualTo(true);
    }

    @Test
    public void testForCorrectlySetAttributesInCommonDummy() {
        FhemDevice device = getDeviceFor("device1");

        assertThat(device.getName()).isEqualTo("device1");
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("??");
    }

    @Test
    public void testDeviceWithSetList() {
        FhemDevice device = getDeviceFor("deviceWithSetlist");

        assertThat((GroupSetListEntry) device.getSetList().get("state", false))
                .isEqualTo(new GroupSetListEntry("state", "17", "18", "19", "20", "21", "21.5", "22"));
    }

    @Test
    public void testDeviceWithTimer() {
        FhemDevice device = getDeviceFor("timerDevice");
        assertThat(device).isNotNull();
    }

    @Test
    public void testSliderDevice() {
        FhemDevice device = getDeviceFor("sliderDevice");
        DimmableBehavior behavior = DimmableBehavior.Companion.behaviorFor(device, null).get();
        assertThat(behavior.getDimUpperBound()).isEqualTo(50);
        assertThat(behavior.getDimLowerBound()).isEqualTo(10);
        assertThat(behavior.getDimStep()).isEqualTo(2);
    }

    @Test
    public void testEventMapDevice() {
        FhemDevice device = getDeviceFor("eventMapDevice");

        String[] eventMapStates = device.getAvailableTargetStatesEventMapTexts();
        assertThat(eventMapStates).isNotNull();

        SetList setList = device.getSetList();
        assertThat(setList).isNotNull();

        assertThat(setList.size()).isEqualTo(eventMapStates.length);

        assertThat(setList.contains("oben", "unten", "65", "40")).isEqualTo(true);

        assertThat(eventMapStates).contains("Oben", "Unten", "Halbschatten", "Vollschatten");
    }

    @Override
    protected String getFileName() {
        return "dummy.xml";
    }
}
