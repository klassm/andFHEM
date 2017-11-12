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

package li.klass.fhem.domain.heating.schedule.configuration;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;
import li.klass.fhem.util.DayUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class MAXConfigurationTest {
    private MAXConfiguration configuration = new MAXConfiguration();
    private WeekProfile<FilledTemperatureInterval, MAXConfiguration> weekProfile;

    @Before
    public void before() {
        weekProfile = new WeekProfile<>(configuration);
    }

    @Test
    public void testDayRead() {
        configuration.readNode(weekProfile, "weekprofile-0-Sat-time", "00:00-07:00  /  07:00-23:00  /  23:00-00:00");
        configuration.readNode(weekProfile, "weekprofile-0-Sat-temp", "15 °C  /  22 °C /  15.5 °C");
        configuration.readNode(weekProfile, "weekprofile-6-Fri-temp", "15 °C  /  22 °C /  15 °C");
        configuration.readNode(weekProfile, "weekprofile-6-Fri-time", "00:00-06:00  /  06:00-23:00  /  23:00-00:00");

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).getSwitchTime()).isEqualTo("00:00");
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).isTimeFixed()).isTrue();
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).getSwitchTime()).isEqualTo("07:00");
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).getSwitchTime()).isEqualTo("23:00");
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).getSwitchTime()).isEqualTo("00:00");
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).isTimeFixed()).isTrue();
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 1).getSwitchTime()).isEqualTo("06:00");
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 2).getSwitchTime()).isEqualTo("23:00");

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).getTemperature()).isEqualTo(15.0);
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).getTemperature()).isEqualTo(22.0);
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).getTemperature()).isEqualTo(15.5);
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).getTemperature()).isEqualTo(15.0);
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 1).getTemperature()).isEqualTo(22.0);
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 2).getTemperature()).isEqualTo(15.0);
    }

    @Test
    public void testGenerateCommand() {
        configuration.readNode(weekProfile, "weekprofile-0-Sat-time", "00:00-07:00  /  07:00-23:00  /  23:00-00:00");
        configuration.readNode(weekProfile, "weekprofile-0-Sat-temp", "15 °C  /  22 °C  /  15 °C");
        configuration.readNode(weekProfile, "weekprofile-6-Fri-temp", "15 °C /  22 °C /  15 °C");
        configuration.readNode(weekProfile, "weekprofile-6-Fri-time", "00:00-06:00  /  06:00-23:00  /  23:00-00:00");

        GenericDevice device = new GenericDevice();
        XmlListDevice xmlListDevice = new XmlListDevice("dummy");
        xmlListDevice.setInternal("NAME", "name");
        device.setXmlListDevice(xmlListDevice);

        getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).setChangedTemperature(23);
        getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).setChangedTemperature(23);

        List<String> commands = configuration.generateScheduleCommands(device.getName(), weekProfile);

        assertThat(commands).contains("set name weekProfile Sat 23.0,07:00,22.0,23:00,15.0")
                .contains("set name weekProfile Fri 23.0,06:00,22.0,23:00,15.0");
    }

    private FilledTemperatureInterval getHeatingIntervalAt(DayUtil.Day saturday, int position) {
        DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>> dayProfile = weekProfile.getDayProfileFor(saturday);
        assert dayProfile != null;

        return dayProfile.getHeatingIntervalAt(position);
    }
}
