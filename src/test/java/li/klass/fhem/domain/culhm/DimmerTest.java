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

import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.fest.assertions.api.Assertions.assertThat;

public class DimmerTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        CULHMDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("off");
        assertThat(device.getSubType()).isEqualTo(CULHMDevice.SubType.DIMMER);

        assertThat(device.isOnByState()).isEqualTo(false);
        assertThat(device.getDimPosition()).isEqualTo(0);
        assertThat(device.supportsDim()).isEqualTo(true);

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);

        assertThat(device.isSupported()).isEqualTo(true);
    }

    @Test
    public void testDim() {
        CULHMDevice device = getDefaultDevice();
        device.setState("5 %");

        assertThat(device.getDimPosition()).isEqualTo(5);
        assertThat(device.getDimDownPosition()).isEqualTo(4);
        assertThat(device.getDimUpPosition()).isEqualTo(6);

        device.setState("on");
        assertThat(device.getDimPosition()).isEqualTo(100);
        device.setState("off");
        assertThat(device.getDimPosition()).isEqualTo(0);
    }

    @Override
    protected String getFileName() {
        return "dimmer.xml";
    }
}
