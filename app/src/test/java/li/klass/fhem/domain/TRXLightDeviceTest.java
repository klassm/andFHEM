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

import static org.assertj.core.api.Assertions.assertThat;

@Category(DeviceTestBase.class)
public class TRXLightDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        GenericDevice device = getDefaultDevice(GenericDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("off");

        assertThat(device.getXmlListDevice().getSetList().getEntries()).isNotEmpty();

        assertThat(device.supportsToggle()).isEqualTo(true);
        assertThat(device.supportsDim()).isEqualTo(false);

        GenericDevice device1 = getDeviceFor("device1", GenericDevice.class);
        assertThat(device1.supportsDim()).isEqualTo(false);

        GenericDevice device2 = getDeviceFor("device2", GenericDevice.class);
        assertThat(device2.getState()).isEqualTo("level 15");

        GenericDevice device3 = getDeviceFor("device3", GenericDevice.class);
        assertThat(device3.getState()).isEqualTo("level 12");
    }

    @Test
    public void testFormatTargetState() {
        GenericDevice device3 = getDeviceFor("device3", GenericDevice.class);

        device3.setState("off");
        assertThat(device3.formatTargetState("level 13")).isEqualTo("level 13");
        assertThat(device3.formatTargetState("on")).isEqualTo("on");
        assertThat(device3.formatTargetState("off")).isEqualTo("off");

        device3.setState("level 13");
        assertThat(device3.formatTargetState("level 12")).isEqualTo("level 12");
    }

    @Test
    public void testSetState() {
        GenericDevice device3 = getDeviceFor("device3", GenericDevice.class);

        device3.setState("level 5");
        assertThat(device3.getState()).isEqualTo("level 5");
    }

    @Override
    protected String getFileName() {
        return "trx_light.xml";
    }
}
