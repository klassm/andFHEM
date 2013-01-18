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

import static li.klass.fhem.domain.MaxDevice.HeatingMode.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MaxDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testHeatingMode() {
        MaxDevice maxDevice = new MaxDevice();

        maxDevice.readMODE("auto");
        assertThat(maxDevice.getHeatingMode(), is(AUTO));

        maxDevice.readMODE("boost");
        assertThat(maxDevice.getHeatingMode(), is(BOOST));

        maxDevice.readMODE("manual");
        assertThat(maxDevice.getHeatingMode(), is(MANUAL));
    }

    @Test
    public void testShutterContactDevice() {
        MaxDevice device = getDeviceFor("device");

        assertThat(device.getBattery(), is("ok"));
        assertThat(device.getState(), is("closed"));
        assertThat(device.getSubType(), is(MaxDevice.SubType.WINDOW));
    }

    @Test
    public void testCubeDevice() {
        MaxDevice device = getDeviceFor("device1");

        assertThat(device.getState(), is("connected"));
        assertThat(device.getSubType(), is(MaxDevice.SubType.CUBE));
    }

    @Test
    public void testPushButtonDevice() {
        MaxDevice device = getDeviceFor("device2");

        assertThat(device.getState(), is("waiting for data"));
        assertThat(device.getSubType(), is(MaxDevice.SubType.SWITCH));
    }

    @Test
    public void testHeatingThermostatDevice() {
        MaxDevice device = getDeviceFor("device3");

        assertThat(device.getState(), is("17.0 °C"));
        assertThat(device.getSubType(), is(MaxDevice.SubType.TEMPERATURE));
        assertThat(device.getBattery(), is("ok"));
        assertThat(device.getActuator(), is("0 (%)"));
        assertThat(device.getDesiredTempDesc(), is("17.0 (°C)"));
        assertThat(device.getTemperature(), is("21 (°C)"));
        assertThat(device.getWindowOpenTempDesc(), is("12.0 (°C)"));
        assertThat(device.getEcoTempDesc(), is("16.5 (°C)"));
        assertThat(device.getComfortTempDesc(), is("19.0 (°C)"));

        assertThat(device.getHeatingMode(), is(BOOST));
    }

    public void testJournalDevice() {
        MaxDevice device = getDeviceFor("journalDevice");

        assertThat(device.getMeasured(), is("2013-01-12 15:27:55"));
    }

    @Override
    protected String getFileName() {
        return "max.xml";
    }
}
