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
import li.klass.fhem.util.DateFormatUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class KS300DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        KS300Device device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));
        assertThat(device.getWidgetName(), is(DEFAULT_TEST_DEVICE_NAME));

        assertThat(device.getAverageDay(), is("T: 4.8  H: 78  W: 6.6  R: 5.1"));
        assertThat(device.getAverageMonth(), is("T: 6.7  H: 38  W: 42.4  R: 10.2"));
        assertThat(device.getRain(), is("24.5 (l/m2)"));
        assertThat(device.getRaining(), is("no (yes/no)"));
        assertThat(device.getWind(), is("2.2 (km/h)"));
        assertThat(device.getHumidity(), is("74 (%)"));
        assertThat(device.getTemperature(), is("2.0 (°C)"));
        assertThat(device.getState(), is("T: 2.0  H: 74  W: 2.2  R: 24.5  IR: no"));

        assertThat(device.getSetList().getEntries().size(), is(0));

        assertThat(device.getLogDevices(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(3));
    }

    @Test
    public void testIsOutdated() {
        KS300Device device = new KS300Device();

        long now = System.currentTimeMillis();

        device.readMEASURED(DateFormatUtil.toReadable(now));
        long outdateTime = device.getTimeRequiredForStateError();

        assertThat(device.isOutdatedData(device.getLastMeasureTime() + outdateTime + 10000), is(true));
        assertThat(device.isOutdatedData(device.getLastMeasureTime() + outdateTime - 10000), is(false));

        device.readMEASURED("abc");
        assertThat(device.isOutdatedData(1), is(false));
    }


    @Override
    protected String getFileName() {
        return "ks300.xml";
    }
}
