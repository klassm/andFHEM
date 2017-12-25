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
import li.klass.fhem.domain.core.FhemDevice;

import static org.assertj.core.api.Assertions.assertThat;


public class FS20DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInFirstDevice() {
        FhemDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("on");

        assertThat(device.getEventMap().getValueFor("off-for-timer 12")).isEqualTo("Ab80");
        assertThat(device.getEventMap().getValueFor("off")).isEqualTo("Ab");
        assertThat(device.getEventMap().getValueFor("on")).isEqualTo("Auf");

        assertThat(device.getSetList().getEntries()).isNotEmpty();
    }

    @Test
    public void testForCorrectlySetAttributesInSecondDevice() {
        FhemDevice device = getDeviceFor("device1");

        assertThat(device.getName()).isEqualTo("device1");
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("off");

        assertThat(device.getEventMapStateFor("off")).isEqualTo("closed");
        assertThat(device.getEventMapStateFor("on")).isEqualTo("open");
        assertThat(device.getEventMapStateFor("dummy")).isEqualTo("dummy");

        assertThat(device.getAlias()).isEqualTo("myAlias");

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getInternalDeviceGroupOrGroupAttributes()).contains("dimmer", "switch", "temperature");
        assertThat(device.getInternalDeviceGroupOrGroupAttributes()).hasSize(3);

        assertThat(device.getWidgetName()).isEqualTo("myAlias");
    }

    @Test
    public void testForCorrectlySetAttributesInThirdDevice() {
        FhemDevice device = getDeviceFor("device2");

        assertThat(device.getWidgetName()).isEqualTo("widget_name");
    }

    @Test
    public void testAlwaysHiddenDevice() {
        FhemDevice device = getDeviceFor("device3");
        assertThat(device).isNull();
    }

    @Override
    protected String getFileName() {
        return "fs20.xml";
    }
}
