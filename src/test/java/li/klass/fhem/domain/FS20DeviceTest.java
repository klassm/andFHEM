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

import static org.assertj.core.api.Assertions.assertThat;


public class FS20DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInFirstDevice() {
        GenericDevice device = getDefaultDevice(GenericDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("on");

        assertThat(device.getEventMap().getValueFor("off-for-timer 12")).isEqualTo("Ab80");
        assertThat(device.getEventMap().getValueFor("off")).isEqualTo("Ab");
        assertThat(device.getEventMap().getValueFor("on")).isEqualTo("Auf");

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getSortBy()).isEqualTo("1");
    }

    @Test
    public void testForCorrectlySetAttributesInSecondDevice() {
        GenericDevice device = getDeviceFor("device1", GenericDevice.class);

        assertThat(device.getName()).isEqualTo("device1");
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("off");

        assertThat(device.getEventMapStateFor("off")).isEqualTo("closed");
        assertThat(device.getEventMapStateFor("on")).isEqualTo("open");
        assertThat(device.getEventMapStateFor("dummy")).isEqualTo("dummy");

        assertThat(device.getAlias()).isEqualTo("myAlias");

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getInternalDeviceGroupOrGroupAttributes(context)).contains("dimmer", "switch", "temperature");
        assertThat(device.getInternalDeviceGroupOrGroupAttributes(context)).hasSize(3);

        assertThat(device.getWidgetName()).isEqualTo("myAlias");
    }

    @Test
    public void testForCorrectlySetAttributesInThirdDevice() {
        GenericDevice device = getDeviceFor("device2", GenericDevice.class);

        assertThat(device.getWidgetName()).isEqualTo("widget_name");
    }

    @Test
    public void testAlwaysHiddenHook() {
        GenericDevice device = new GenericDevice();
        assertThat(device.isSupported()).isEqualTo(true);

        device.setAlwaysHidden("true");
        assertThat(device.isSupported()).isEqualTo(false);

        device.setAlwaysHidden("false");
        assertThat(device.isSupported()).isEqualTo(true);
    }

    @Test
    public void testAlwaysHiddenDevice() {
        GenericDevice device = getDeviceFor("device3", GenericDevice.class);
        assertThat(device).isNull();
    }

    @Override
    protected String getFileName() {
        return "fs20.xml";
    }
}
