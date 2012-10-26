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
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;

public class AtDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testParseDefinition() {
        AtDevice device;

        device = parse("17:00:00 set lamp on", 17, 0, 0, "lamp", "on", null,
                AtDevice.AtRepetition.ONCE, AtDevice.TimerType.ABSOLUTE, true);
        assemble(device, "17:00:00 { fhem(\"set lamp on\") }");

        device = parse("*23:00:00 { fhem(\"set lamp off\") if ($we) }", 23, 0, 0, "lamp", "off", null,
                AtDevice.AtRepetition.WEEKEND, AtDevice.TimerType.ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off\") if ($we) }");

        device = parse("+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (not $we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                AtDevice.AtRepetition.WEEKDAY, AtDevice.TimerType.RELATIVE, true);
        assemble(device, "+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (NOT $we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                AtDevice.AtRepetition.WEEKDAY, AtDevice.TimerType.ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                AtDevice.AtRepetition.WEEKDAY, AtDevice.TimerType.ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (0 && !$we) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                AtDevice.AtRepetition.WEEKDAY, AtDevice.TimerType.ABSOLUTE, false);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                AtDevice.AtRepetition.WEEKDAY, AtDevice.TimerType.ABSOLUTE, false);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 1) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                AtDevice.AtRepetition.WEEKDAY, AtDevice.TimerType.ABSOLUTE, true);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we) }");

        device = parse("*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }", 23, 0, 0, "lamp", "off-for-timer", "200",
                AtDevice.AtRepetition.WEEKDAY, AtDevice.TimerType.ABSOLUTE, false);
        assemble(device, "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!$we && 0) }");
    }

    private AtDevice parse(String definition,
                           int expectedHours, int expectedMinutes, int expectedSeconds,
                           String expectedTargetDevice, String expectedTargetState,
                           String expectedAdditionalInformation,
                           AtDevice.AtRepetition expectedRepetition,
                           AtDevice.TimerType expectedTimerType, boolean isActive) {
        AtDevice device = new AtDevice();
        device.parseDefinition(definition);

        assertEquals(expectedHours, device.getHours());
        assertEquals(expectedMinutes, device.getMinutes());
        assertEquals(expectedSeconds, device.getSeconds());
        assertEquals(expectedTargetState, device.getTargetState());
        assertEquals(expectedTargetDevice, device.getTargetDevice());
        assertEquals(expectedAdditionalInformation, device.getTargetStateAddtionalInformation());
        assertEquals(expectedRepetition, device.getRepetition());
        assertEquals(expectedTimerType, device.getTimerType());
        assertEquals(isActive, device.isActive());

        return device;
    }

    private void assemble(AtDevice device, String expectedDefinition) {
        assertEquals(expectedDefinition, device.toFHEMDefinition());
    }

    @Test
    public void testForCorrectlySetAttributes() {
        AtDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getTimerType(), is(AtDevice.TimerType.ABSOLUTE));
        assertThat(device.getFormattedSwitchTime(), is("23:00:00"));
        assertThat(device.getHours(), is(23));
        assertThat(device.getMinutes(), is(0));
        assertThat(device.getSeconds(), is(0));
        assertThat(device.getNextTrigger(), is("23:00:00"));
        assertThat(device.getRepetition(), is(AtDevice.AtRepetition.WEEKEND));
        assertThat(device.getTargetDevice(), is("lamp"));
        assertThat(device.getTargetState(), is("off"));
        assertThat(device.getTargetStateAddtionalInformation(), is(nullValue()));

        assertThat(device.getFileLog(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));
    }

    @Override
    protected String getFileName() {
        return "at.xml";
    }
}
