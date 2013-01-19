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
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class HCSDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        HCSDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("demand"));

        assertThat(device.getEcoTemperatureOff(), is("23 (°C)"));
        assertThat(device.getEcoTemperatureOn(), is("12 (°C)"));
        assertThat(device.getThermostatThresholdOff(), is("0.5 (°C)"));
        assertThat(device.getThermostatThresholdOn(), is("0.5 (°C)"));
        assertThat(device.getValveThresholdOff(), is("25 (°C)"));
        assertThat(device.getValveThresholdOn(), is("30 (°C)"));

        assertThat(device.getMode(), is("valve"));

        assertThat(device.getNumberOfDemandDevices(), is("3"));
        assertThat(device.getNumberOfExcludedDevices(), is("3"));
        assertThat(device.getNumberOfIdleDevices(), is("3"));

        assertThat(device.getFileLog(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));
    }

    @Override
    protected String getFileName() {
        return "hcs.xml";
    }
}
