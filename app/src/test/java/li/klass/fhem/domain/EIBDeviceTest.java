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

import static org.fest.assertions.api.Assertions.assertThat;

public class EIBDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        EIBDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("on");
        assertThat(device.isOnByState()).isEqualTo(true);
        assertThat(device.isSpecialButtonDevice()).isEqualTo(false);

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);

        assertThat(device.supportsToggle()).isEqualTo(true);
    }

    @Test
    public void testTimeDevice() {
        EIBDevice timeDevice = getDeviceFor("time");
        EIBDevice dpt10Device = getDeviceFor("dpt10");

        assertThat(timeDevice.getModel()).isEqualTo("time");
        assertThat(dpt10Device.getModel()).isEqualTo("time");
    }

    @Test
    public void testDimmerDevice() {
        EIBDevice device = getDeviceFor("dimmer");

        assertThat(device.supportsDim()).isEqualTo(true);
        assertThat(device.getDimPosition()).isEqualTo(20);
        assertThat(device.getState()).isEqualTo("20 (%)");
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

    @Test
    public void testPercentDeviceWithUnsetState() {
        EIBDevice device = getDeviceFor("percent_with_unset_state");
        assertThat(device.getState()).isNotNull();
        assertThat(device.getState()).isEqualTo("0 (%)");
    }

    private void assertDeviceState(String deviceName, String expectedState) {
        EIBDevice device = getDeviceFor(deviceName);
        assertThat(device.getState()).isEqualTo(expectedState);
        assertThat(device.supportsToggle()).isEqualTo(false);
    }

    @Override
    protected String getFileName() {
        return "eib.xml";
    }
}
