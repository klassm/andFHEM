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

public class HUEDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        HUEDevice device = getDefaultDevice(HUEDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);
        assertThat(device.getAlias()).isEqualTo("Extended color light 1");

        assertThat(device.getBrightness()).isEqualTo(254);
        assertThat(device.getBrightnessDesc()).isEqualTo("254");

        assertThat(device.getSaturation()).isEqualTo(144);
        assertThat(device.getSaturationDesc()).isEqualTo("144");

        assertThat(device.getRgbDesc()).isEqualTo("0xFFEE8B");
        assertThat(device.getRgb()).isEqualTo(16772747);

        assertThat(device.getXy()).isEqualTo(new double[]{0.4595, 0.4105});

        assertThat(device.getPositionForDimState("off")).isEqualTo(0);
    }

    @Test
    public void testDeviceWithoutXYAttribute() {
        HUEDevice device = getDeviceFor("device1", HUEDevice.class);
        assertThat(device.getXy()).isNull();
    }

    @Override
    protected String getFileName() {
        return "hue.xml";
    }
}
