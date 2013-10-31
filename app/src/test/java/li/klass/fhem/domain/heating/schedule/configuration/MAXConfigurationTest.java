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

import li.klass.fhem.domain.MaxDevice;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.Reject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class MAXConfigurationTest {
    private MAXConfiguration configuration = new MAXConfiguration();
    private WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile;

    @Before
    public void before() {
        weekProfile = new WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice>(configuration);
    }

    @Test
    public void testDayRead() {
        configuration.readNode(weekProfile, "WEEKPROFILE-0-SAT-TIME", "00:00-07:00  /  07:00-23:00  /  23:00-00:00");
        configuration.readNode(weekProfile, "WEEKPROFILE-0-SAT-TEMP", "15 °C  /  22 °C /  15.5 °C");
        configuration.readNode(weekProfile, "WEEKPROFILE-6-FRI-TEMP", "15 °C  /  22 °C /  15 °C");
        configuration.readNode(weekProfile, "WEEKPROFILE-6-FRI-TIME", "00:00-06:00  /  06:00-23:00  /  23:00-00:00");

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).getSwitchTime(), is("00:00"));
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).isTimeFixed(), is(true));
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).getSwitchTime(), is("07:00"));
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).getSwitchTime(), is("23:00"));
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).getSwitchTime(), is("00:00"));
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).isTimeFixed(), is(true));
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 1).getSwitchTime(), is("06:00"));
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 2).getSwitchTime(), is("23:00"));

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).getTemperature(), is(15.0));
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).getTemperature(), is(22.0));
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).getTemperature(), is(15.5));
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).getTemperature(), is(15.0));
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 1).getTemperature(), is(22.0));
        assertThat(getHeatingIntervalAt(DayUtil.Day.FRIDAY, 2).getTemperature(), is(15.0));
    }

    @Test
    public void testGenerateCommand() {
        configuration.readNode(weekProfile, "WEEKPROFILE-0-SAT-TIME", "00:00-07:00  /  07:00-23:00  /  23:00-00:00");
        configuration.readNode(weekProfile, "WEEKPROFILE-0-SAT-TEMP", "15 °C  /  22 °C  /  15 °C");
        configuration.readNode(weekProfile, "WEEKPROFILE-6-FRI-TEMP", "15 °C /  22 °C /  15 °C");
        configuration.readNode(weekProfile, "WEEKPROFILE-6-FRI-TIME", "00:00-06:00  /  06:00-23:00  /  23:00-00:00");

        MaxDevice device = new MaxDevice();
        device.setName("name");

        getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).setChangedTemperature(23);
        getHeatingIntervalAt(DayUtil.Day.FRIDAY, 0).setChangedTemperature(23);

        List<String> commands = configuration.generateScheduleCommands(device, weekProfile);

        assertThat(commands, hasItem("set name weekProfile Sat 23.0,07:00,22.0,23:00,15.0"));
        assertThat(commands, hasItem("set name weekProfile Fri 23.0,06:00,22.0,23:00,15.0"));
    }

    private FilledTemperatureInterval getHeatingIntervalAt(DayUtil.Day saturday, int position) {
        DayProfile<FilledTemperatureInterval, MaxDevice, MAXConfiguration> dayProfile = weekProfile.getDayProfileFor(saturday);
        Reject.ifNull(dayProfile);

        FilledTemperatureInterval interval = dayProfile.getHeatingIntervalAt(position);
        Reject.ifNull(interval);
        return interval;
    }
}
