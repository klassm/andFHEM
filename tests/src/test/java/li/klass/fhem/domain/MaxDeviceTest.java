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
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.MAXConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.util.DayUtil;
import org.junit.Test;

import static li.klass.fhem.domain.MaxDevice.HeatingMode.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
        assertThat(device.getDesiredTempDesc(), is("on"));
        assertThat(device.getDesiredTemp(), is(closeTo(30.5, 0.1)));
        assertThat(device.getTemperature(), is("21 (°C)"));
        assertThat(device.getWindowOpenTempDesc(), is("12.0 (°C)"));
        assertThat(device.getEcoTempDesc(), is("16.5 (°C)"));
        assertThat(device.getComfortTempDesc(), is("19.0 (°C)"));

        assertThat(device.getHeatingMode(), is(BOOST));
    }

    @Test
    public void testJournalDevice() {
        MaxDevice device = getDeviceFor("journalDevice");

        assertThat(device.getMeasured(), is("2013-02-27 22:00:03"));

        WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile = device.getWeekProfile();
        assertThat(weekProfile, is(notNullValue()));

        DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration> tuesday = weekProfile.getDayProfileFor(DayUtil.Day.TUESDAY);
        assertThat(tuesday.getHeatingIntervals().size(), is(6));
        assertThat(tuesday.getHeatingIntervalAt(0).getSwitchTime(), is("00:00"));
        assertThat(tuesday.getHeatingIntervalAt(0).getTemperature(), is(closeTo(17, 0.1)));
    }

    @Test
    public void testOnOffTemperatureDevice() {
        MaxDevice device = getDeviceFor("on_off");

        assertThat(device.getDesiredTemp(), is(closeTo(30.5, 0.01)));
        assertThat(device.getWindowOpenTemp(), is(closeTo(4.5, 0.01)));
        assertThat(device.getEcoTemp(), is(closeTo(30.5, 0.01)));
    }

    @Override
    protected String getFileName() {
        return "max.xml";
    }
}
