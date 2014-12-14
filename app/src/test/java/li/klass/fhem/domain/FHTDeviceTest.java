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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class FHTDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        FHTDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getActuator(), is("0.0 (%)"));
        assertThat(device.getDayTemperature(), is(closeTo(22, 0.01)));
        assertThat(device.getDayTemperatureDesc(), is("22.0 (°C)"));
        assertThat(device.getNightTemperature(), is(closeTo(6.5, 0.01)));
        assertThat(device.getNightTemperatureDesc(), is("6.5 (°C)"));
        assertThat(device.getWindowOpenTemp(), is(closeTo(6.5, 0.01)));
        assertThat(device.getWindowOpenTempDesc(), is("6.5 (°C)"));
        assertThat(device.getTemperature(), is("23.1 (°C)"));
        assertThat(device.getHeatingMode(), is(FHTMode.AUTO));
        assertThat(device.getWarnings(), is("Window open"));
        assertThat(device.getBattery(), is("ok"));
        assertThat(device.getState(), is("???"));
        assertThat(device.getDesiredTemp(), is(closeTo(6.5, 0.01)));
        assertThat(device.getDesiredTempDesc(), is("6.5 (°C)"));

        assertThat(device.getSetList().getEntries().size(), is(not(0)));

        assertThat(device.getLogDevices(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(1));

        assertThat(device.getSetList().contains("day-temp", "desired-temp", "manu-temp", "night-temp", "windowopen-temp"), is(true));
    }

    @Test
    public void testDeviceWithMultipleActors() {
        FHTDevice device = getDeviceFor("fht_multi_actuators");
        assertThat(device, is(notNullValue()));

        assertThat(device.getLogDevices(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(1));
    }

    @Override
    protected String getFileName() {
        return "fht.xml";
    }
}
