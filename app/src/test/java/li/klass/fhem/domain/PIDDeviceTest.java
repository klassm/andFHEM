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
import li.klass.fhem.update.backend.xmllist.XmlListDevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

public class PIDDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        GenericDevice device = getDefaultDevice(GenericDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        XmlListDevice xmlListDevice = device.getXmlListDevice();
        assertThat(device.getState()).isEqualTo("16.8 (delta -0.800000000000001)");
        assertThat(xmlListDevice.getState("delta", true)).contains("-0.800000000000001");
        assertThat(xmlListDevice.getState("desired", true)).contains("16.0 (°C)");
        assertThat(xmlListDevice.getState("actuation", true)).contains("0 (%)");

        assertThat(xmlListDevice.getSetList().getEntries()).isNotEmpty();
    }

    @Test
    public void should_read_PID20_devices() {
        GenericDevice device = getDeviceFor("eg.wohnen.pid", GenericDevice.class);
        assertThat(device).isNotNull();

        XmlListDevice xmlListDevice = device.getXmlListDevice();
        assertThat(xmlListDevice.getState("delta", true)).contains("0.129999999999999");
        assertThat(xmlListDevice.getState("desired", true)).contains("21.5 (°C)");
        assertThat(xmlListDevice.getState("actuation", true)).contains("27 (%)");
        assertThat(xmlListDevice.getState("measured", true)).contains("21.37 (°C)");
    }

    @Override
    protected String getFileName() {
        return "pid.xml";
    }
}
