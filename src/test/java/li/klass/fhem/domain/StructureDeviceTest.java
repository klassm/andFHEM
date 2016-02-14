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
import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;

import static org.assertj.core.api.Assertions.assertThat;

public class StructureDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInOnOffDummy() {
        GenericDevice device = getDefaultDevice(GenericDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("on");
        assertThat(device.supportsToggle()).isEqualTo(true);

        assertThat(device.getSetList().contains("on", "off")).isEqualTo(true);
    }

    @Test
    public void testDeviceWithSetList() {
        GenericDevice device = getDeviceFor("deviceWithSetlist", GenericDevice.class);

        assertThat((GroupSetListEntry) device.getSetList().get("state")).isEqualTo(new GroupSetListEntry("state", "17", "18", "19", "20", "21", "21.5", "22"));
    }

    @Test
    public void testSlider() {
        GenericDevice device = getDeviceFor("slider", GenericDevice.class);
        assertThat(device).isNotNull();

        SetListEntry value = device.getSetList().get("pct");
        assertThat(value).isInstanceOf(SliderSetListEntry.class);
        assertThat((SliderSetListEntry) value).isEqualTo(new SliderSetListEntry("pct", 10, 2, 110));
    }

    @Override
    protected String getFileName() {
        return "structure.xml";
    }
}
