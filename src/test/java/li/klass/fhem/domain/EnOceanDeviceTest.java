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

public class EnOceanDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void testForCorrectlySetAttributes() {
        GenericDevice device = getDefaultDevice(GenericDevice.class);
        assertThat(device).isNotNull();
        assertThat(device.getState()).isEqualTo(("on"));
        assertThat(device.getEventMapStateFor("BI")).isEqualTo(("off"));
        assertThat(device.getEventMapStateFor("B0")).isEqualTo(("on"));

        device.setState("B0");
        assertThat(device.getState()).isEqualTo(("on"));

        GenericDevice device1 = getDeviceFor("device1", GenericDevice.class);
        assertThat(device).isNotNull();
        assertThat(device1.getState()).isEqualTo(("153"));
        assertThat(device1.getMeasured()).isEqualTo(("04.11.2012 23:55"));

        GenericDevice device2 = getDeviceFor("device2", GenericDevice.class);
        assertThat(device2).isNotNull();
    }

    @Test
    public void testGatewaySwitchDevice() {
        GenericDevice device = getDeviceFor("device3", GenericDevice.class);
        assertThat(device).isNotNull();
    }

    @Test
    public void testShutterDevice() {
        GenericDevice device = getDeviceFor("shutter", GenericDevice.class);
        assertThat(device).isNotNull();
    }

    @Override
    protected String getFileName() {
        return "enocean.xml";
    }
}
