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

import static li.klass.fhem.domain.AtDevice.AtRepetition.FRIDAY;
import static li.klass.fhem.domain.AtDevice.AtRepetition.ONCE;
import static li.klass.fhem.domain.AtDevice.AtRepetition.WEEKDAY;
import static li.klass.fhem.domain.AtDevice.AtRepetition.WEEKEND;
import static li.klass.fhem.domain.AtDevice.TimerType.ABSOLUTE;
import static li.klass.fhem.domain.AtDevice.TimerType.RELATIVE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class AtDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testParseDefinition() {
        AtDevice device;

        device = parse("17:00:00 set lamp on", 17, 0, 0, "lamp", "on", null,
                ONCE, ABSOLUTE, true);
        assemble(device, "17:00:00 { fhem(\"set lamp on\") }");

        device = parse("*23:00:00 { fhem(\"set lamp off\") if ($we) }", 23, 0, 0, "lamp", "off", null,
                WEEKEND, ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off\") if ($we) }");

        device = parse("+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (not $we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                WEEKDAY, RELATIVE, true);
        assemble(device, "+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (NOT $we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                WEEKDAY, ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                WEEKDAY, ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (0 && !$we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                WEEKDAY, ABSOLUTE, false);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                WEEKDAY, ABSOLUTE, false);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 1) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                WEEKDAY, ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                WEEKDAY, ABSOLUTE, false);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }");

        device = parse("*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if (!$we && 0) }", 7, 15, 0, "Badezimmer",
                "desired-temp", "00.00", WEEKDAY, ABSOLUTE, false);
        assemble(device, "*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if (!$we && 0) }");

        device = parse("*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if ($wday == 5) }", 7, 15, 0, "Badezimmer",
                "desired-temp", "00.00", FRIDAY, ABSOLUTE, true);
        assemble(device, "*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if ($wday == 5) }");

        device = parse("*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if ($wday == 5 && 0) }", 7, 15, 0, "Badezimmer",
                "desired-temp", "00.00", FRIDAY, ABSOLUTE, false);
        assemble(device, "*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if ($wday == 5 && 0) }");

        device = parse("19:45:00 { fhem(\"set EZ.Heizung_Clima desired-temp 24.00\") }", 19, 45, 0, "EZ.Heizung_Clima",
                "desired-temp", "24.00", ONCE, ABSOLUTE, true);
        assemble(device, "19:45:00 { fhem(\"set EZ.Heizung_Clima desired-temp 24.00\") }");
    }

    private AtDevice parse(String definition,
                           int expectedHours, int expectedMinutes, int expectedSeconds,
                           String expectedTargetDevice, String expectedTargetState,
                           String expectedAdditionalInformation,
                           AtDevice.AtRepetition expectedRepetition,
                           AtDevice.TimerType expectedTimerType, boolean isActive) {
        AtDevice device = new AtDevice();
        device.parseDefinition(definition);

        assertThat(device.getHours()).isEqualTo(expectedHours);
        assertThat(device.getMinutes()).isEqualTo(expectedMinutes);
        assertThat(device.getSeconds()).isEqualTo(expectedSeconds);
        assertThat(device.getTargetState()).isEqualTo(expectedTargetState);
        assertThat(device.getTargetDevice()).isEqualTo(expectedTargetDevice);
        assertThat(device.getTargetStateAddtionalInformation()).isEqualTo(expectedAdditionalInformation);
        assertThat(device.getRepetition()).isEqualTo(expectedRepetition);
        assertThat(device.getTimerType()).isEqualTo(expectedTimerType);
        assertThat(device.isActive()).isEqualTo(isActive);

        return device;
    }

    private void assemble(AtDevice device, String expectedDefinition) {
        assertEquals(expectedDefinition, device.toFHEMDefinition());
    }

    @Test
    public void testForCorrectlySetAttributes() {
        AtDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getTimerType()).isEqualTo(ABSOLUTE);
        assertThat(device.getFormattedSwitchTime()).isEqualTo("23:00:00");
        assertThat(device.getHours()).isEqualTo(23);
        assertThat(device.getMinutes()).isEqualTo(0);
        assertThat(device.getSeconds()).isEqualTo(0);
        assertThat(device.getNextTrigger()).isEqualTo("23:00:00");
        assertThat(device.getRepetition()).isEqualTo(WEEKEND);
        assertThat(device.getTargetDevice()).isEqualTo("lamp");
        assertThat(device.getTargetState()).isEqualTo("off");
        assertThat(device.getTargetStateAddtionalInformation()).isNullOrEmpty();

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);
    }

    @Override
    protected String getFileName() {
        return "at.xml";
    }
}
