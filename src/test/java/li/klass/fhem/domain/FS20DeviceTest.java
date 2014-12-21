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
import li.klass.fhem.domain.core.ToggleableDevice;

import static org.fest.assertions.api.Assertions.assertThat;


public class FS20DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInFirstDevice() {
        FS20Device device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("on");
        assertThat(device.isOnByState()).isEqualTo(true);
        assertThat(device.isSpecialButtonDevice()).isEqualTo(false);

        assertThat(device.getEventMap().get("off-for-timer 12")).isEqualTo("Ab80");
        assertThat(device.getEventMap().get("off")).isEqualTo("Ab");
        assertThat(device.getEventMap().get("on")).isEqualTo("Auf");

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);
    }

    @Test
    public void testForCorrectlySetAttributesInSecondDevice() {
        FS20Device device = getDeviceFor("device1");

        assertThat(device.getName()).isEqualTo("device1");
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("off");
        assertThat(device.isOnByState()).isEqualTo(false);

        assertThat(device.getEventMapStateFor("off")).isEqualTo("closed");
        assertThat(device.getEventMapStateFor("on")).isEqualTo("open");
        assertThat(device.getEventMapStateFor("dummy")).isEqualTo("dummy");

        assertThat(device.getAlias()).isEqualTo("myAlias");
        assertThat(device.isSpecialButtonDevice()).isEqualTo(true);
        assertThat(device.getButtonHookType()).isEqualTo(ToggleableDevice.ButtonHookType.ON_OFF_DEVICE);

        assertThat(device.getSetList().getEntries()).isNotEmpty();

        assertThat(device.getLogDevices()).isNotEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(1);

        assertThat(device.getInternalDeviceGroupOrGroupAttributes()).contains("dimmer", "switch", "temperature");
        assertThat(device.getInternalDeviceGroupOrGroupAttributes()).hasSize(3);

        assertThat(device.getWidgetName()).isEqualTo("myAlias");
    }

    @Test
    public void testForCorrectlySetAttributesInThirdDevice() {
        FS20Device device = getDeviceFor("device2");

        assertThat(device.isOnRespectingInvertHook()).isEqualTo(true);
        assertThat(device.isOnByState()).isEqualTo(false);
        assertThat(device.getWidgetName()).isEqualTo("widget_name");
    }

    @Test
    public void testDim() {
        FS20Device device = new FS20Device();
        device.setState("dim12%");

        assertThat(device.getDimPosition()).isEqualTo(2);

        assertThat(device.getDimDownPosition()).isEqualTo(FS20Device.dimStates.indexOf("dim6%"));
        assertThat(device.getDimUpPosition()).isEqualTo(FS20Device.dimStates.indexOf("dim18%"));

        device.setState("on");
        assertThat(device.getDimUpPosition()).isEqualTo(FS20Device.dimStates.indexOf("dim100%"));

        device.setState("off");
        assertThat(device.getDimDownPosition()).isEqualTo(FS20Device.dimStates.indexOf("off"));
    }

    @Test
    public void testAlwaysHiddenHook() {
        FS20Device device = new FS20Device();
        assertThat(device.isSupported()).isEqualTo(true);

        device.readALWAYS_HIDDEN("true");
        assertThat(device.isSupported()).isEqualTo(false);

        device.readALWAYS_HIDDEN("false");
        assertThat(device.isSupported()).isEqualTo(true);
    }

    @Test
    public void testAlwaysHiddenDevice() {
        FS20Device device = getDeviceFor("device3");
        assertThat(device).isNull();
    }

    @Override
    protected String getFileName() {
        return "fs20.xml";
    }
}
