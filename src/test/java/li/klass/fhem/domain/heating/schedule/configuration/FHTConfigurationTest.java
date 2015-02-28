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

import java.util.Arrays;
import java.util.List;

import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval;
import li.klass.fhem.util.DayUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class FHTConfigurationTest {

    private FHTConfiguration configuration = new FHTConfiguration();
    private WeekProfile<FromToHeatingInterval, FHTConfiguration, FHTDevice> weekProfile;

    @Before
    public void before() {
        weekProfile = new WeekProfile<>(configuration);
    }

    @Test
    public void testDayRead() {
        configuration.readNode(weekProfile, "MON-FROM1", "24:00");
        configuration.readNode(weekProfile, "MON-FROM2", "08:25");
        configuration.readNode(weekProfile, "MON-TO1", "09:30");
        configuration.readNode(weekProfile, "MON-TO2", "11:56");

        assertThat(weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(0).getFromTime()).isEqualTo("00:00");
        assertThat(weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(1).getFromTime()).isEqualTo("08:25");
        assertThat(weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(0).getToTime()).isEqualTo("09:30");
        assertThat(weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(1).getToTime()).isEqualTo("11:56");
    }

    @Test
    public void testGenerateCommandParts() {
        FHTDevice device = new FHTDevice();
        device.setName("name");

        WeekProfile<FromToHeatingInterval, FHTConfiguration, FHTDevice> weekProfile = new WeekProfile<>(configuration);
        weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(0).setChangedFromTime("03:25");
        weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(1).setChangedFromTime("06:25");
        weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(0).setChangedToTime("04:25");
        weekProfile.getDayProfileFor(DayUtil.Day.MONDAY).getHeatingIntervalAt(1).setChangedToTime("07:25");

        List<String> parts = configuration.generateCommandParts(weekProfile.getChangedDayProfiles());
        assertThat(parts)
                .hasSize(4)
                .contains("mon-from1 03:25")
                .contains("mon-from2 06:25")
                .contains("mon-to1 04:25")
                .contains("mon-to2 07:25");
    }

    @Test
    public void testGenerateCommands() {
        FHTDevice device = new FHTDevice();
        device.setName("name");

        List<String> commandParts = Arrays.asList(
                "mon-from1 03:25",
                "mon-to1 06:25",
                "mon-from2 09:25",
                "mon-to2 12:25",
                "tue-from1 03:25",
                "tue-to1 06:25",
                "tue-from2 09:25",
                "tue-to2 12:25",
                "wed-from1 03:25",
                "wed-to1 06:25",
                "wed-from2 09:25",
                "wed-to2 12:25"
        );

        List<String> commands = configuration.generateCommands(device, commandParts);
        assertThat(commands)
                .hasSize(2)
                .contains("set name mon-from1 03:25 mon-to1 06:25 mon-from2 09:25 mon-to2 12:25 tue-from1 03:25 tue-to1 06:25 tue-from2 09:25 tue-to2 12:25")
                .contains("set name wed-from1 03:25 wed-to1 06:25 wed-from2 09:25 wed-to2 12:25");
    }
}
