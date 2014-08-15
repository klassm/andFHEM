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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.Offset.offset;

public class CULEMDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        CULEMDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getDayUsage()).isEqualTo("9.490 (kWh)");
        assertThat(device.getMonthUsage()).isEqualTo("60.385 (kWh)");
        assertThat(device.getSumGraphDivisionFactor()).isEqualTo(2, offset(0.01));
        assertThat(device.getCurrentUsage()).isEqualTo("0 (kWh)");
        assertThat(device.getState()).isEqualTo("CNT: 62 CUM: 1254.521  5MIN: 0.000  TOP: 0.000");
        assertThat(device.getCumulativeKwh()).isEqualTo("1254.52 (kWh)");


        assertThat(device.getSetList().getEntries().size()).isEqualTo(0);

        assertThat(device.getLogDevices()).hasSize(1);
        assertThat(device.getDeviceCharts().size()).isEqualTo(1);
    }

    @Override
    protected String getFileName() {
        return "cul_em.xml";
    }
}
