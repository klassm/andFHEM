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

package li.klass.fhem.domain.culhm;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.List;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.CULHMConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.util.DayUtil;

import static li.klass.fhem.util.DayUtil.Day.FRIDAY;
import static li.klass.fhem.util.DayUtil.Day.MONDAY;
import static li.klass.fhem.util.DayUtil.Day.SATURDAY;
import static li.klass.fhem.util.DayUtil.Day.SUNDAY;
import static li.klass.fhem.util.DayUtil.Day.THURSDAY;
import static li.klass.fhem.util.DayUtil.Day.TUESDAY;
import static li.klass.fhem.util.DayUtil.Day.WEDNESDAY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.Offset.offset;

public class ThermostatTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        CULHMDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("12%");
        assertThat(device.getSubType()).isEqualTo(CULHMDevice.SubType.THERMOSTAT);

        assertThat(device.isSupported()).isEqualTo(true);

        CULHMDevice device1 = getDeviceFor("device1");
        assertThat(device1).isNotNull();
        assertThat(device1.isSupported()).isEqualTo(true);
        assertThat(device.getSubType()).isEqualTo(CULHMDevice.SubType.THERMOSTAT);

        CULHMDevice device2 = getDeviceFor("device2");
        assertThat(device2).isNotNull();
        assertThat(device2.isSupported()).isEqualTo(true);
        assertThat(device2.getSubType()).isEqualTo(CULHMDevice.SubType.HEATING);
        assertThat(device2.getDesiredTemp()).isEqualTo(16, offset(0.01));
        assertThat(device2.getDesiredTempDesc()).isEqualTo("16.0 (°C)");
        assertThat(device2.getActuator()).isEqualTo("86 (%)");

        assertThat(device.isSupported()).isEqualTo(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_read_weekday_profile_with_weekday_prefixes() {
        CULHMDevice device = getDeviceFor("deviceWithPrefix");

        assertThat(device).isNotNull();

        WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice> weekProfile =
                device.getWeekProfile();
        assertThat(weekProfile).isNotNull();

        assertWeekProfileContainsExactly(weekProfile, SATURDAY,
                Pair.of("08:00", 20.0), Pair.of("23:00", 22.0), Pair.of("24:00", 20.0));
        assertWeekProfileContainsExactly(weekProfile, SUNDAY,
                Pair.of("08:00", 20.0), Pair.of("22:00", 22.0), Pair.of("24:00", 20.0));
        assertWeekProfileContainsExactly(weekProfile, MONDAY,
                Pair.of("04:50", 20.0), Pair.of("22:00", 22.0), Pair.of("24:00", 20.0));
        assertWeekProfileContainsExactly(weekProfile, TUESDAY,
                Pair.of("04:50", 20.0), Pair.of("22:00", 22.0), Pair.of("24:00", 20.0));
        assertWeekProfileContainsExactly(weekProfile, WEDNESDAY,
                Pair.of("04:50", 20.0), Pair.of("22:00", 22.0), Pair.of("24:00", 20.0));
        assertWeekProfileContainsExactly(weekProfile, THURSDAY,
                Pair.of("04:50", 20.0), Pair.of("22:00", 22.0), Pair.of("24:00", 20.0));
        assertWeekProfileContainsExactly(weekProfile, FRIDAY,
                Pair.of("04:50", 20.0), Pair.of("23:00", 22.0), Pair.of("24:00", 20.0));
    }

    private void assertWeekProfileContainsExactly(WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice> weekProfile,
                                                  DayUtil.Day day, Pair<String, Double>... switchTimeTemperature) {
        DayProfile<FilledTemperatureInterval, CULHMDevice, CULHMConfiguration> dayProfile = weekProfile.getDayProfileFor(day);
        List<FilledTemperatureInterval> heatingIntervals = dayProfile.getHeatingIntervals();
        assertThat(heatingIntervals.size()).isEqualTo(switchTimeTemperature.length);

        for (Pair<String, Double> expected : switchTimeTemperature) {
            boolean found = false;
            for (FilledTemperatureInterval heatingInterval : heatingIntervals) {
                if (Math.abs(expected.getRight() - heatingInterval.getTemperature()) <= 0.01 &&
                        expected.getLeft().equals(heatingInterval.getSwitchTime())) {
                    found = true;
                    break;
                }
            }
            assertThat(found).as(day + " " + expected.toString()).isTrue();
        }
    }

    @Override
    protected String getFileName() {
        return "thermostat.xml";
    }
}
