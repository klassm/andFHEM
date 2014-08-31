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

package li.klass.fhem.domain.culhm;

import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.fest.assertions.api.Assertions.assertThat;

public class THPLSensorTest extends DeviceXMLParsingBase {
    @Test
    public void should_set_default_device_attributes() {
        CULHMDevice device = getDeviceFor("default");

        assertThat(device.getName()).isEqualTo("default");

        assertThat(device.getState()).isEqualTo("T: 25.0 H: 54 L: 0.56 batVoltage: 2.58");
        assertThat(device.getSubType()).isEqualTo(CULHMDevice.SubType.THPL);
        assertThat(device.supportsDim()).isEqualTo(false);

        assertThat(device.getMeasuredTemp()).isEqualTo("25.0 (°C)");
        assertThat(device.getBattery()).isEqualTo("ok");
        assertThat(device.getHumidity()).isEqualTo("54 (%)");
        assertThat(device.getLuminosity()).isEqualTo("0.56 (lm)");
        assertThat(device.getBatteryVoltage()).isEqualTo("2.58 (V)");

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts()).isEmpty();

        assertThat(device.isSupported()).isEqualTo(true);
    }

    @Test
    public void should_set_pressure_device_attributes() {
        CULHMDevice device = getDeviceFor("pressure");

        assertThat(device.getName()).isEqualTo("pressure");

        assertThat(device.getPressure()).isEqualTo("998 (hPa)");
        assertThat(device.getPressureNN()).isEqualTo("1024 (hPa)");

        assertThat(device.isSupported()).isTrue();
    }

    @Override
    protected String getFileName() {
        return "THPLSensor.xml";
    }
}
