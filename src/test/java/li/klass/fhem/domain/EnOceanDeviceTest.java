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
        EnOceanDevice device = getDefaultDevice(EnOceanDevice.class);
        assertThat(device.getSubType()).isEqualTo((EnOceanDevice.SubType.SWITCH));
        assertThat(device.getState()).isEqualTo(("on"));
        assertThat(device.getEventMapStateFor("BI")).isEqualTo(("off"));
        assertThat(device.getEventMapStateFor("B0")).isEqualTo(("on"));
        assertThat(device.getOffStateName()).isEqualTo(("BI"));
        assertThat(device.getOnStateName()).isEqualTo(("B0"));

        device.setState("B0");
        assertThat(device.getState()).isEqualTo(("on"));

        EnOceanDevice device1 = getDeviceFor("device1", EnOceanDevice.class);
        assertThat(device1.getSubType()).isEqualTo((EnOceanDevice.SubType.SENSOR));
        assertThat(device1.getState()).isEqualTo(("153"));
        assertThat(device1.getMeasured()).isEqualTo(("04.11.2012 23:55"));

        EnOceanDevice device2 = getDeviceFor("device2", EnOceanDevice.class);
        assertThat(device2.getOffStateName()).isEqualTo(("released"));
        assertThat(device2.getOnStateName()).isEqualTo(("B0"));

        device.setSubtype("");
    }

    @Test
    public void testGatewaySwitchDevice() {
        EnOceanDevice device = getDeviceFor("device3", EnOceanDevice.class);
        assertThat(device.getSubType()).isEqualTo((EnOceanDevice.SubType.SWITCH));
    }

    @Test
    public void testShutterDevice() {
        EnOceanDevice device = getDeviceFor("shutter", EnOceanDevice.class);

        assertThat(device).isNotNull();

        assertThat(device.getSubType()).isEqualTo((EnOceanDevice.SubType.SHUTTER));

        assertThat(device.getModel()).isEqualTo(("FSB14"));
        assertThat(device.getManufacturerId()).isEqualTo(("00D"));
    }

    @Override
    protected String getFileName() {
        return "enocean.xml";
    }
}
