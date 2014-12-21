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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GPIO4DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        GPIO4Device device = getDeviceFor("temp");

        assertThat(device.getTemperature(), is("22.937 (°C)"));
        assertThat(device.getState(), is("22.937 °C (Mittelwert: 22.7 °C)"));

        // this is not supported and, thus, removed
        GPIO4Device rPi = getDeviceFor("RPi");
        assertThat(rPi, is(nullValue()));
    }

    @Test
    public void testDS18B20Device() {
        GPIO4Device device = getDeviceFor("DS18B20");
        assertThat(device, is(notNullValue()));

        assertThat(device.isSupported(), is(true));
        assertThat(device.getTemperature(), is("20.437 (°C)"));
    }

    @Test
    public void testAdditionalAttributesDevice() {
        GPIO4Device device = getDeviceFor("additionalAttributes");
        assertThat(device, is(notNullValue()));

        assertThat(device.getAverageDay(), is("20.6 (°C)"));
        assertThat(device.getAverageMonth(), is("20.3 (°C)"));
        assertThat(device.getMinDay(), is("20.1 (°C)"));
        assertThat(device.getMinMonth(), is("0.0 (°C)"));
        assertThat(device.getMaxDay(), is("22.2 (°C)"));
        assertThat(device.getMaxMonth(), is("22.6 (°C)"));
    }

    @Override
    protected String getFileName() {
        return "gpio4.xml";
    }
}
