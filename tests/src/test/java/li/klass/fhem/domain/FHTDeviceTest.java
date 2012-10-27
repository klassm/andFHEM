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

import li.klass.fhem.R;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.domain.fht.FHTMode;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class FHTDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        FHTDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getActuator(), is("0.0 (%)"));
        assertThat(device.getDayTemperature(), is(closeTo(22, 0.01)));
        assertThat(device.getDayTemperatureDesc(), is("22.0 (°C)"));
        assertThat(device.getNightTemperature(), is(closeTo(6.5, 0.01)));
        assertThat(device.getNightTemperatureDesc(), is("6.5 (°C)"));
        assertThat(device.getWindowOpenTemp(), is(closeTo(6.5, 0.01)));
        assertThat(device.getWindowOpenTempDesc(), is("6.5 (°C)"));
        assertThat(device.getTemperature(), is("23.1 (°C)"));
        assertThat(device.getMode(), is(FHTMode.AUTO));
        assertThat(device.getWarnings(), is("Window open"));
        assertThat(device.getBattery(), is("ok"));
        assertThat(device.getState(), is("???"));

        FHTDayControl monday = device.getDayControlMap().get(R.string.monday);
        assertThat(monday.getFrom1(), is("10:00"));
        assertThat(monday.getFrom2(), is("24:00"));
        assertThat(monday.getTo1(), is("17:00"));
        assertThat(monday.getTo2(), is("09:00"));

        FHTDayControl tuesday = device.getDayControlMap().get(R.string.tuesday);
        assertThat(tuesday.getFrom1(), is("10:00"));
        assertThat(tuesday.getFrom2(), is("24:00"));
        assertThat(tuesday.getTo1(), is("17:00"));
        assertThat(tuesday.getTo2(), is("24:00"));

        FHTDayControl wednesday = device.getDayControlMap().get(R.string.wednesday);
        assertThat(wednesday.getFrom1(), is("10:00"));
        assertThat(wednesday.getFrom2(), is("24:00"));
        assertThat(wednesday.getTo1(), is("17:00"));
        assertThat(wednesday.getTo2(), is("24:00"));

        FHTDayControl thursday = device.getDayControlMap().get(R.string.thursday);
        assertThat(thursday.getFrom1(), is("10:00"));
        assertThat(thursday.getFrom2(), is("24:00"));
        assertThat(thursday.getTo1(), is("17:00"));
        assertThat(thursday.getTo2(), is("24:00"));

        FHTDayControl friday = device.getDayControlMap().get(R.string.friday);
        assertThat(friday.getFrom1(), is("10:00"));
        assertThat(friday.getFrom2(), is("24:00"));
        assertThat(friday.getTo1(), is("17:30"));
        assertThat(friday.getTo2(), is("24:00"));

        FHTDayControl saturday = device.getDayControlMap().get(R.string.saturday);
        assertThat(saturday.getFrom1(), is("09:00"));
        assertThat(saturday.getFrom2(), is("24:00"));
        assertThat(saturday.getTo1(), is("17:00"));
        assertThat(saturday.getTo2(), is("24:00"));

        FHTDayControl sunday = device.getDayControlMap().get(R.string.sunday);
        assertThat(sunday.getFrom1(), is("10:00"));
        assertThat(sunday.getFrom2(), is("13:00"));
        assertThat(sunday.getTo1(), is("17:00"));
        assertThat(sunday.getTo2(), is("24:00"));

        assertThat(device.getAvailableTargetStates(), is(notNullValue()));

        assertThat(device.getFileLog(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(2));

        assertThat(device.getAvailableTargetStates(), hasItemInArray("day-temp"));
        assertThat(device.getAvailableTargetStates(), hasItemInArray("desired-temp"));
        assertThat(device.getAvailableTargetStates(), hasItemInArray("manu-temp"));
        assertThat(device.getAvailableTargetStates(), hasItemInArray("night-temp"));
        assertThat(device.getAvailableTargetStates(), hasItemInArray("windowopen-temp"));
    }

    @Override
    protected String getFileName() {
        return "fht.xml";
    }
}
