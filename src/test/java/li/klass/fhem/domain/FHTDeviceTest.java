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
import li.klass.fhem.domain.fht.FHTMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class FHTDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        FHTDevice device = getDefaultDevice(FHTDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getActuator()).isEqualTo("0.0 (%)");
        assertThat(device.getDayTemperature()).isCloseTo(22, offset(0.01));
        assertThat(device.getDayTemperatureDesc()).isEqualTo("22.0 (°C)");
        assertThat(device.getNightTemperature()).isCloseTo(6.5, offset(0.01));
        assertThat(device.getNightTemperatureDesc()).isEqualTo("6.5 (°C)");
        assertThat(device.getWindowOpenTemp()).isCloseTo(6.5, offset(0.01));
        assertThat(device.getWindowOpenTempDesc()).isEqualTo("6.5 (°C)");
        assertThat(device.getTemperature()).isEqualTo("23.1 (°C)");
        assertThat(device.getHeatingMode()).isEqualTo(FHTMode.AUTO);
        assertThat(device.getWarnings()).isEqualTo("Window open");
        assertThat(device.getBattery()).isEqualTo("ok");
        assertThat(device.getState()).isEqualTo("???");
        assertThat(device.getDesiredTemp()).isCloseTo(6.5, offset(0.01));
        assertThat(device.getDesiredTempDesc()).isEqualTo("6.5 (°C)");

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getLogDevices()).isNotNull();
        assertThat(device.getDeviceCharts()).hasSize(1);

        assertThat(device.getSetList().contains("day-temp", "desired-temp", "manu-temp", "night-temp", "windowopen-temp")).isEqualTo(true);
    }

    @Test
    public void testDeviceWithMultipleActors() {
        FHTDevice device = getDeviceFor("fht_multi_actuators", FHTDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getLogDevices()).isNotNull();
        assertThat(device.getDeviceCharts()).hasSize(1);
    }

    @Override
    protected String getFileName() {
        return "fht.xml";
    }
}
