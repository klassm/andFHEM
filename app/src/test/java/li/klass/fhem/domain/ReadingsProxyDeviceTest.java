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

import static li.klass.fhem.domain.core.DeviceFunctionality.SWITCH;
import static org.fest.assertions.api.Assertions.assertThat;

public class ReadingsProxyDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testRGBProxy() {
        ReadingsProxyDevice device = getDeviceFor("rgb");

        assertThat(device).isNotNull();
        assertThat(device.getName()).isEqualTo("rgb");
        assertThat(device.getDeviceGroup()).isEqualTo(SWITCH);
        assertThat(device.getRGBColor()).isEqualTo(16776960);
        assertThat(device.getRgbDesc()).isEqualTo("0xFFFF00");
    }

    @Test
    public void testSwitchProxy() {
        ReadingsProxyDevice device = getDeviceFor("Bild");

        assertThat(device).isNotNull();
        assertThat(device.getDeviceGroup()).isEqualTo(SWITCH);
        assertThat(device.getRgbDesc()).isNull();
    }

    @Test
    public void testDimmable() {
        ReadingsProxyDevice device = getDeviceFor("dimmable");

        assertThat(device).isNotNull();
        assertThat(device.supportsDim()).isTrue();
        assertThat(device.getDimPosition()).isEqualTo(100);
        assertThat(device.getDimLowerBound()).isEqualTo(4);
        assertThat(device.getDimUpperBound()).isEqualTo(105);
        assertThat(device.getDimStep()).isEqualTo(1);
    }

    @Override
    protected String getFileName() {
        return "readings_proxy.xml";
    }
}
