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
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.MAXConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.util.DayUtil;

import static li.klass.fhem.domain.MaxDevice.HeatingMode.AUTO;
import static li.klass.fhem.domain.MaxDevice.HeatingMode.BOOST;
import static li.klass.fhem.domain.MaxDevice.HeatingMode.MANUAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class MaxDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testHeatingMode() {
        MaxDevice maxDevice = new MaxDevice();

        maxDevice.setMode("auto");
        assertThat(maxDevice.getHeatingMode()).isEqualTo(AUTO);

        maxDevice.setMode("boost");
        assertThat(maxDevice.getHeatingMode()).isEqualTo(BOOST);

        maxDevice.setMode("manual");
        assertThat(maxDevice.getHeatingMode()).isEqualTo(MANUAL);
    }

    @Test
    public void testShutterContactDevice() {
        MaxDevice device = getDeviceFor("device", MaxDevice.class);

        assertThat(device.getBattery()).isEqualTo("ok");
        assertThat(device.getState()).isEqualTo("closed");
        assertThat(device.getSubType()).isEqualTo(MaxDevice.SubType.WINDOW);
    }

    @Test
    public void testCubeDevice() {
        MaxDevice device = getDeviceFor("device1", MaxDevice.class);

        assertThat(device.getState()).isEqualTo("connected");
        assertThat(device.getSubType()).isEqualTo(MaxDevice.SubType.CUBE);
    }

    @Test
    public void testPushButtonDevice() {
        MaxDevice device = getDeviceFor("device2", MaxDevice.class);

        assertThat(device.getState()).isEqualTo("waiting for data");
        assertThat(device.getSubType()).isEqualTo(MaxDevice.SubType.SWITCH);
    }

    @Test
    public void testHeatingThermostatDevice() {
        MaxDevice device = getDeviceFor("device3", MaxDevice.class);

        assertThat(device.getState()).isEqualTo("17.0 °C");
        assertThat(device.getSubType()).isEqualTo(MaxDevice.SubType.TEMPERATURE);
        assertThat(device.getBattery()).isEqualTo("ok");
        assertThat(device.getActuator()).isEqualTo("0 (%)");
        assertThat(device.getDesiredTempDesc()).isEqualTo("on");
        assertThat(device.getDesiredTemp()).isCloseTo(30.5, offset(0.1));
        assertThat(device.getTemperature()).isEqualTo("21.0 (°C)");
        assertThat(device.getWindowOpenTempDesc()).isEqualTo("12.0 (°C)");
        assertThat(device.getEcoTempDesc()).isEqualTo("16.5 (°C)");
        assertThat(device.getComfortTempDesc()).isEqualTo("19.0 (°C)");

        assertThat(device.getHeatingMode()).isEqualTo(BOOST);
    }

    @Test
    public void testJournalDevice() {
        MaxDevice device = getDeviceFor("journalDevice", MaxDevice.class);

        WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile = device.getWeekProfile();
        assertThat(weekProfile).isNotNull();

        DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration> tuesday = weekProfile.getDayProfileFor(DayUtil.Day.TUESDAY);
        assertThat(tuesday.getHeatingIntervals().size()).isEqualTo(6);
        assertThat(tuesday.getHeatingIntervalAt(0).getSwitchTime()).isEqualTo("00:00");
        assertThat(tuesday.getHeatingIntervalAt(0).getTemperature()).isCloseTo(17, offset(0.1));
    }

    @Test
    public void testOnOffTemperatureDevice() {
        MaxDevice device = getDeviceFor("on_off", MaxDevice.class);

        assertThat(device.getDesiredTemp()).isCloseTo(30.5, offset(0.01));
        assertThat(device.getWindowOpenTemp()).isCloseTo(4.5, offset(0.01));
        assertThat(device.getEcoTemp()).isCloseTo(30.5, offset(0.01));
    }

    @Override
    protected String getFileName() {
        return "max.xml";
    }
}
