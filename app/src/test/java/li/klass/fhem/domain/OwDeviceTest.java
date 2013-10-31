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

import static li.klass.fhem.domain.OwDevice.SubType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class OwDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testTemperatureDevice() {
        OwDevice device = getDeviceFor("Aussentemperatur");

        assertThat(device.getName(), is("Aussentemperatur"));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getTemperature(), is("0.0 (°C)"));
        assertThat(device.getSubType(), is(TEMPERATURE));

        OwDevice device1 = getDeviceFor("Vorlauf");
        assertThat(device1.getTemperature(), is("19.125 (°C)"));
        assertThat(device1.getSubType(), is(TEMPERATURE));

        assertThat(device.isSupported(), is(true));
        assertThat(device1.isSupported(), is(true));
    }

    @Test
    public void testRelaisDevice() {
        OwDevice device = getDeviceFor("DS2413A");
        assertThat(device.getSubType(), is(RELAIS));
        assertThat(device.getCounterA(), Matchers.is("2"));
        assertThat(device.getCounterB(), Matchers.is("3"));

        assertThat(device.isSupported(), is(true));
    }

    @Test
    public void testSwitchDevice() {
        OwDevice device = getDeviceFor("Relais1");
        assertThat(device.getSubType(), is(SWITCH));
        assertThat(device.supportsToggle(), is(true));
        assertThat(device.getState(), is("ein"));
        assertThat(device.getInternalState(), is("PIO 1"));
        assertThat(device.isOnByState(), is(true));
    }

    @Override
    protected String getFileName() {
        return "owdevice.xml";
    }
}
