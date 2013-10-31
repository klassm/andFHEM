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

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class EIBDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        EIBDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("on"));
        assertThat(device.isOnByState(), is(true));
        assertThat(device.isSpecialButtonDevice(), is(false));

        assertThat(device.getAvailableTargetStates(), is(notNullValue()));

        assertThat(device.getFileLog(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));

        assertThat(device.supportsToggle(), is(true));
    }

    @Test
    public void testTimeDevice() {
        EIBDevice timeDevice = getDeviceFor("time");
        EIBDevice dpt10Device = getDeviceFor("dpt10");

        assertThat(timeDevice.getModel(), is("time"));
        assertThat(dpt10Device.getModel(), is("time"));
    }

    @Test
    public void testDimmerDevice() {
        EIBDevice device = getDeviceFor("dimmer");

        assertThat(device.supportsDim(), is(true));
        assertThat(device.getDimPosition(), is(20));
        assertThat(device.getState(), is("20 (%)"));
    }

    @Test
    public void testNonSwitchDeviceTypes() {
        assertDeviceState("speedsensor", "1.7 (m/s)");
        assertDeviceState("tempsensor", "0.9 (°C)");
        assertDeviceState("lightsensor", "1158.4 (lux)");
        assertDeviceState("brightness", "13338.0 (lux)");
        assertDeviceState("time", "13:34:00");
        assertDeviceState("dpt10", "18:44:33");

        // missing data??
//        assertDeviceState("rainsensor", "0.9 (°C)");
    }

    private void assertDeviceState(String deviceName, String expectedState) {
        EIBDevice device = getDeviceFor(deviceName);
        assertThat(device.getState(), is(expectedState));
        assertThat(device.supportsToggle(), is(false));
    }

    @Override
    protected String getFileName() {
        return "eib.xml";
    }
}
