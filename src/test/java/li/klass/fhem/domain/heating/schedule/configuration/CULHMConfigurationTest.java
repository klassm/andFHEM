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
import li.klass.fhem.room.list.backend.xmllist.XmlListDevice;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.Reject;

import static org.assertj.core.api.Assertions.assertThat;

public class CULHMConfigurationTest {
    private CULHMConfiguration configuration = new CULHMConfiguration();
    private WeekProfile<FilledTemperatureInterval, CULHMConfiguration> weekProfile;

    @Before
    public void before() {
        weekProfile = new WeekProfile<>(configuration);
    }

    @Test
    public void testDayRead() {
        configuration.readNode(weekProfile, "tempListSat", "08:00 16.5 19:30 20 24:00 16.0");

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).getSwitchTime()).isEqualTo("08:00");
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).getSwitchTime()).isEqualTo("19:30");
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).getSwitchTime()).isEqualTo("24:00");
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).isTimeFixed()).isEqualTo(true);

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).getTemperature()).isEqualTo(16.5);
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).getTemperature()).isEqualTo(20.0);
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).getTemperature()).isEqualTo(16.0);
    }

    private FilledTemperatureInterval getHeatingIntervalAt(DayUtil.Day saturday, int position) {
        DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>> dayProfile = weekProfile.getDayProfileFor(saturday);
        Reject.ifNull(dayProfile);

        FilledTemperatureInterval interval = dayProfile.getHeatingIntervalAt(position);
        Reject.ifNull(interval);
        return interval;
    }

    @Test
    public void shouldIgnoreR_0_Pefixes() {
        configuration.readNode(weekProfile, "R_0_tempListSat", "08:00 16.5 19:30 20 24:00 16.0");
        assertThat(weekProfile.getDayProfileFor(DayUtil.Day.SATURDAY)).isNotNull();
    }

    @Test
    public void testGenerateCommand() {
        configuration.readNode(weekProfile, "tempListSat", "24:00 16.0 08:00 16.0 19:30 20");

        GenericDevice device = new GenericDevice();
        XmlListDevice xmlListDevice = new XmlListDevice("dummy");
        xmlListDevice.setInternal("NAME", "name");
        device.setXmlListDevice(xmlListDevice);

        getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).setChangedTemperature(23);

        List<String> commands = configuration.generateScheduleCommands(device.getName(), weekProfile);

        assertThat(commands).contains("set name tempListSat 08:00 16.0 19:30 20.0 24:00 23.0");
    }

    @Test
    public void testSetPrefixCanBeStillRead() {
        configuration.readNode(weekProfile, "tempListSat", "set_  05:45 17.0 07:00 21.0 18:00 17.0 23:00 21.0 24:00 17.0");

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).getTemperature()).isEqualTo(17.0);
    }
}
