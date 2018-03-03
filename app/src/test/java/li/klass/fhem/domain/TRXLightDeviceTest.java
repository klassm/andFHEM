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
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.testsuite.category.DeviceTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@Category(DeviceTestBase.class)
public class TRXLightDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        FhemDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("off");

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        FhemDevice device1 = getDeviceFor("device1");
        assertThat(device1).isNotNull();

        FhemDevice device2 = getDeviceFor("device2");
        assertThat(device2.getState()).isEqualTo("level 15");

        FhemDevice device3 = getDeviceFor("device3");
        assertThat(device3.getState()).isEqualTo("level 12");
    }

    @Override
    protected String getFileName() {
        return "trx_light.xml";
    }
}
