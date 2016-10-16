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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.tngtech.java.junit.dataprovider.DataProviders.testForEach;
import static li.klass.fhem.domain.AtDevice.AtRepetition.FRIDAY;
import static li.klass.fhem.domain.AtDevice.AtRepetition.ONCE;
import static li.klass.fhem.domain.AtDevice.AtRepetition.WEEKDAY;
import static li.klass.fhem.domain.AtDevice.AtRepetition.WEEKEND;
import static li.klass.fhem.domain.AtDevice.TimerType.ABSOLUTE;
import static li.klass.fhem.domain.AtDevice.TimerType.RELATIVE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class AtDeviceTest {
    @DataProvider
    public static Object[][] provider() {
        return testForEach(
                new TestCase()
                        .withHours(17)
                        .withMinutes(0)
                        .withSeconds(0)
                        .withTargetState("on")
                        .withTargetDevice("lamp")
                        .withAdditionalInformation(null)
                        .withRepetition(ONCE)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("17:00:00 set lamp on")
                        .withAssembledDefinition("17:00:00 { fhem(\"set lamp on\") }"),
                new TestCase()
                        .withHours(23)
                        .withMinutes(0)
                        .withSeconds(0)
                        .withTargetState("off")
                        .withTargetDevice("lamp")
                        .withAdditionalInformation(null)
                        .withRepetition(WEEKEND)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("*23:00:00 { fhem(\"set lamp off\") if ($we) }"),
                new TestCase()
                        .withHours(23)
                        .withMinutes(0)
                        .withSeconds(0)
                        .withTargetState("off-for-timer")
                        .withTargetDevice("lamp")
                        .withAdditionalInformation("200")
                        .withRepetition(WEEKDAY)
                        .withTimerType(RELATIVE)
                        .withDefinition("+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (not $we) }")
                        .withAssembledDefinition("+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }"),
                new TestCase()
                        .withHours(23)
                        .withMinutes(0)
                        .withSeconds(0)
                        .withTargetState("off-for-timer")
                        .withTargetDevice("lamp")
                        .withAdditionalInformation("200")
                        .withRepetition(WEEKDAY)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (NOT $we) }")
                        .withAssembledDefinition("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }"),
                new TestCase()
                        .withHours(23)
                        .withMinutes(0)
                        .withSeconds(0)
                        .withTargetState("off-for-timer")
                        .withTargetDevice("lamp")
                        .withAdditionalInformation("200")
                        .withRepetition(WEEKDAY)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }"),
                new TestCase()
                        .withHours(23)
                        .withMinutes(0)
                        .withSeconds(0)
                        .withTargetState("off-for-timer")
                        .withTargetDevice("lamp")
                        .withAdditionalInformation("200")
                        .withRepetition(WEEKDAY)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }"),
                new TestCase()
                        .withHours(7)
                        .withMinutes(15)
                        .withSeconds(0)
                        .withTargetState("desired-temp")
                        .withTargetDevice("Badezimmer")
                        .withAdditionalInformation("00.00")
                        .withRepetition(FRIDAY)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if ($wday == 5) }"),
                new TestCase()
                        .withHours(19)
                        .withMinutes(45)
                        .withSeconds(0)
                        .withTargetState("desired-temp")
                        .withTargetDevice("EZ.Heizung_Clima")
                        .withAdditionalInformation("24.00")
                        .withRepetition(ONCE)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("19:45:00 { fhem(\"set EZ.Heizung_Clima desired-temp 24.00\") }"),
                new TestCase()
                        .withHours(17)
                        .withMinutes(0)
                        .withSeconds(0)
                        .withTargetState("on")
                        .withTargetDevice("d")
                        .withAdditionalInformation(null)
                        .withRepetition(ONCE)
                        .withTimerType(ABSOLUTE)
                        .withDefinition("2016-10-16T17:00:00 { fhem(\"set d on\") }")
                        .withAssembledDefinition("17:00:00 { fhem(\"set d on\") }")
        );

    }


    @Test
    @UseDataProvider("provider")
    public void should_parse_and_assemble_definition(TestCase testCase) {
        // given
        AtDevice device = new AtDevice();

        // when
        device.parseDefinition(testCase.definition);

        // then
        assertThat(device.getHours()).isEqualTo(testCase.hours);
        assertThat(device.getMinutes()).isEqualTo(testCase.minutes);
        assertThat(device.getSeconds()).isEqualTo(testCase.seconds);
        assertThat(device.getTargetState()).isEqualTo(testCase.targetState);
        assertThat(device.getTargetDevice()).isEqualTo(testCase.targetDevice);
        assertThat(device.getTargetStateAddtionalInformation()).isEqualTo(testCase.additionalInformation);
        assertThat(device.getRepetition()).isEqualTo(testCase.repetition);
        assertThat(device.getTimerType()).isEqualTo(testCase.timerType);

        // when
        String assembledDefinition = device.toFHEMDefinition();

        // then
        assertThat(assembledDefinition).isEqualTo(testCase.assembledDefinition);
    }

    private static class TestCase {
        int hours;
        int minutes;
        int seconds;
        String targetDevice;
        String targetState;
        String additionalInformation;
        AtDevice.AtRepetition repetition;
        AtDevice.TimerType timerType;
        String definition;
        String assembledDefinition;

        TestCase withHours(int hours) {
            this.hours = hours;
            return this;
        }

        TestCase withMinutes(int minutes) {
            this.minutes = minutes;
            return this;
        }

        TestCase withSeconds(int seconds) {
            this.seconds = seconds;
            return this;
        }

        TestCase withTargetDevice(String targetDevice) {
            this.targetDevice = targetDevice;
            return this;
        }

        TestCase withTargetState(String targetState) {
            this.targetState = targetState;
            return this;
        }

        TestCase withAdditionalInformation(String additionalInformation) {
            this.additionalInformation = additionalInformation;
            return this;
        }

        TestCase withRepetition(AtDevice.AtRepetition repetition) {
            this.repetition = repetition;
            return this;
        }

        TestCase withTimerType(AtDevice.TimerType timerType) {
            this.timerType = timerType;
            return this;
        }

        TestCase withDefinition(String definition) {
            this.definition = definition;
            this.assembledDefinition = definition;
            return this;
        }

        TestCase withAssembledDefinition(String assembledDefinition) {
            this.assembledDefinition = assembledDefinition;
            return this;
        }
    }
}
