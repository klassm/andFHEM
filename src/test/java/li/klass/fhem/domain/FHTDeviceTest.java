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

public class FHTDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        GenericDevice device = getDefaultDevice(GenericDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(stateValueFor(device, "actuator")).isEqualTo("0 (%)");
        assertThat(stateValueFor(device, "day-temp")).isEqualTo("22.0 (°C)");
        assertThat(stateValueFor(device, "night-temp")).isEqualTo("6.5 (°C)");
        assertThat(stateValueFor(device, "windowopen-temp")).isEqualTo("6.5 (°C)");
        assertThat(stateValueFor(device, "measured-temp")).isEqualTo("23.1 (°C)");
        assertThat(attributeValueFor(device, "mode")).isEqualTo("auto");
        assertThat(stateValueFor(device, "warnings")).isEqualTo("Window open");
        assertThat(stateValueFor(device, "battery")).isEqualTo("ok");
        assertThat(stateValueFor(device, "desired-temp")).isEqualTo("6.5 (°C)");
        assertThat(device.getState()).isEqualTo("???");

        assertThat(device.getXmlListDevice().getSetList().contains("day-temp", "desired-temp", "manu-temp", "night-temp", "windowopen-temp")).isEqualTo(true);
    }

    @Test
    public void testDeviceWithMultipleActors() {
        GenericDevice device = getDeviceFor("fht_multi_actuators", GenericDevice.class);
        assertThat(device).isNotNull();
    }

    @Override
    protected String getFileName() {
        return "fht.xml";
    }
}
