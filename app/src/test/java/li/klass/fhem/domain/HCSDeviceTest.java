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


public class HCSDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        HCSDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("demand");

        assertThat(device.getEcoTemperatureOff()).isEqualTo("23 (°C)");
        assertThat(device.getEcoTemperatureOn()).isEqualTo("12 (°C)");
        assertThat(device.getThermostatThresholdOff()).isEqualTo("0.5 (°C)");
        assertThat(device.getThermostatThresholdOn()).isEqualTo("0.5 (°C)");
        assertThat(device.getValveThresholdOff()).isEqualTo("25 (%)");
        assertThat(device.getValveThresholdOn()).isEqualTo("30 (%)");

        assertThat(device.getMode()).isEqualTo("valve");

        assertThat(device.getNumberOfDemandDevices()).isEqualTo(2);
        assertThat(device.getNumberOfExcludedDevices()).isEqualTo(3);
        assertThat(device.getNumberOfIdleDevices()).isEqualTo(3);

        assertThat(device.getCommaSeparatedDemandDevices()).isEqualTo("FHT_WOHNZIMMER, FHT_WOHNZIMMER1");

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);
    }

    @Override
    protected String getFileName() {
        return "hcs.xml";
    }
}
