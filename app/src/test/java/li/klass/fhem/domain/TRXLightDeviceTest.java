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
import org.junit.experimental.categories.Category;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.testsuite.category.DeviceTestBase;

import static org.fest.assertions.api.Assertions.assertThat;

@Category(DeviceTestBase.class)
public class TRXLightDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        TRXLightDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.isOnByState()).isEqualTo(false);
        assertThat(device.getState()).isEqualTo("off");
        assertThat(device.getType()).isEqualTo("ARC");

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);

        assertThat(device.supportsToggle()).isEqualTo(true);
        assertThat(device.supportsDim()).isEqualTo(false);

        TRXLightDevice device1 = getDeviceFor("device1");
        assertThat(device1.isOnByState()).isEqualTo(true);
        assertThat(device.supportsDim()).isEqualTo(false);

        TRXLightDevice device2 = getDeviceFor("device2");
        assertThat(device2.getState()).isEqualTo("level 15");
        assertThat(device2.isOnByState()).isEqualTo(true);
        assertThat(device2.supportsDim()).isEqualTo(true);
        assertThat(device2.getDimPosition()).isEqualTo(16);

        TRXLightDevice device3 = getDeviceFor("device3");
        assertThat(device3.getState()).isEqualTo("level 12");
        assertThat(device3.isOnByState()).isEqualTo(true);
        assertThat(device3.supportsDim()).isEqualTo(true);
        assertThat(device3.getDimPosition()).isEqualTo(13);
    }

    @Test
    public void testFormatTargetState() {
        TRXLightDevice device3 = getDeviceFor("device3");

        device3.setState("off");
        assertThat(device3.formatTargetState("level 13")).isEqualTo("level 13");
        assertThat(device3.formatTargetState("on")).isEqualTo("on");
        assertThat(device3.formatTargetState("off")).isEqualTo("off");

        device3.setState("level 13");
        assertThat(device3.formatTargetState("level 12")).isEqualTo("level 12");
    }

    @Test
    public void testSetState() {
        TRXLightDevice device3 = getDeviceFor("device3");

        device3.setState("level 5");
        assertThat(device3.getState()).isEqualTo("level 5");
    }

    @Override
    protected String getFileName() {
        return "trx_light.xml";
    }
}
