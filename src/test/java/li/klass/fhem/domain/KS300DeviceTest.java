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

import static org.assertj.core.api.Assertions.assertThat;

public class KS300DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        KS300Device device = getDefaultDevice(KS300Device.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);
        assertThat(device.getWidgetName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);

        assertThat(device.getAverageDay()).isEqualTo("T: 4.8  H: 78  W: 6.6  R: 5.1");
        assertThat(device.getAverageMonth()).isEqualTo("T: 6.7  H: 38  W: 42.4  R: 10.2");
        assertThat(device.getRain()).isEqualTo("24.5 (l/m²)");
        assertThat(device.getRaining()).isEqualTo("no (yes/no)");
        assertThat(device.getWind()).isEqualTo("2.2 (km/h)");
        assertThat(device.getHumidity()).isEqualTo("74.0 (%)");
        assertThat(device.getTemperature()).isEqualTo("2.0 (°C)");
        assertThat(device.getState()).isEqualTo("T: 2.0  H: 74  W: 2.2  R: 24.5  IR: no");

        assertThat(device.getSetList().getEntries().size()).isEqualTo(0);

        assertThat(device.getLogDevices()).isNotNull();
        assertThat(device.getDeviceCharts().size()).isEqualTo(3);
    }

    @Test
    public void testIsOutdated() {
        KS300Device device = new KS300Device();

        long now = System.currentTimeMillis();

        device.setMeasured(DateFormatUtil.FHEM_DATE_FORMAT.print(now));
        long outdateTime = device.getTimeRequiredForStateError();

        assertThat(device.isOutdatedData(device.getLastMeasureTime() + outdateTime + 10000)).isTrue();
        assertThat(device.isOutdatedData(device.getLastMeasureTime() + outdateTime - 10000)).isFalse();
    }


    @Override
    protected String getFileName() {
        return "ks300.xml";
    }
}
