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

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListGroupValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class DummyDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInOnOffDummy() {
        DummyDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("on"));
        assertThat(device.isSpecialButtonDevice(), is(false));
        assertThat(device.supportsToggle(), is(true));
        assertThat(device.isOnByState(), is(true));

        assertThat(device.getSetList().contains("on", "off"), is(true));

        assertThat(device.getLogDevice(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));
        assertThat(device.supportsDim(), is(false));
    }

    @Test
    public void testForCorrectlySetAttributesInCommonDummy() {
        DummyDevice device = getDeviceFor("device1");

        assertThat(device.getName(), is("device1"));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("??"));
        assertThat(device.isSpecialButtonDevice(), is(false));
        assertThat(device.supportsToggle(), is(false));

        assertThat(device.getLogDevice(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));
        assertThat(device.supportsDim(), is(false));
    }

    @Test
    public void testDeviceWithSetList() {
        DummyDevice device = getDeviceFor("deviceWithSetlist");

        assertThat((SetListGroupValue) device.getSetList().get("state"), is(equalTo(new SetListGroupValue("17", "18", "19", "20", "21", "21.5", "22"))));
        assertThat(device.supportsDim(), is(false));
    }

    @Test
    public void testDeviceWithTimer() {
        DummyDevice device = getDeviceFor("timerDevice");

        assertThat(device.isTimerDevice(), is(true));
        assertThat(device.supportsDim(), is(false));
    }

    @Test
    public void testSliderDevice() {
        DummyDevice device = getDeviceFor("sliderDevice");
        assertThat(device.supportsDim(), is(true));

        assertThat(device.getDimUpperBound(), is(50));
        assertThat(device.getDimLowerBound(), is(10));
        assertThat(device.getDimStep(), is(2));
    }

    @Test
    public void testEventMapDevice() {
        DummyDevice device = getDeviceFor("eventMapDevice");

        String[] eventMapStates = device.getAvailableTargetStatesEventMapTexts();
        assertThat(eventMapStates, is(notNullValue()));

        SetList setList = device.getSetList();
        assertThat(setList, is(notNullValue()));

        assertThat(setList.size(), is(eventMapStates.length));

        assertThat(setList.contains("oben", "unten", "65", "40"), is(true));

        assertThat(eventMapStates, hasItemInArray("Oben"));
        assertThat(eventMapStates, hasItemInArray("Unten"));
        assertThat(eventMapStates, hasItemInArray("Halbschatten"));
        assertThat(eventMapStates, hasItemInArray("Vollschatten"));
    }

    @Test
    public void testRGBDevice() {
        DummyDevice device = getDeviceFor("rgbDevice");

        assertThat(device.getRgbDesc(), is("0xFFAB01"));
        assertThat(device.getRGBColor(), is(16755457));
    }

    @Test
    public void testOnOffEventMapDevice() {
        DummyDevice device = getDeviceFor("onOffEventMap");

        assertThat(device.supportsToggle(), is(true));
        assertThat(device.isOnByState(), is(true));
    }

    @Override
    protected String getFileName() {
        return "dummy.xml";
    }
}
