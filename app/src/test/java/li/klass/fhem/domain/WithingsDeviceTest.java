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

public class WithingsDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void body_device_is_read_correctly() {
        GenericDevice device = getDeviceFor("body", GenericDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getName()).isEqualTo("body");

        assertThat(stateValueFor(device, "fatFreeMass")).isEqualTo("68.0 (kg)");
        assertThat(stateValueFor(device, "fatMassWeight")).isEqualTo("17.0 (kg)");
        assertThat(stateValueFor(device, "fatRatio")).isEqualTo("20.0 (%)");
        assertThat(stateValueFor(device, "heartPulse")).isEqualTo("70");
        assertThat(stateValueFor(device, "weight")).isEqualTo("85.0 (kg)");
        assertThat(stateValueFor(device, "height")).isEqualTo("1.9 (m)");
    }

    @Test
    public void scale_device_is_read_correctly() {
        GenericDevice device = getDeviceFor("scale", GenericDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getName()).isEqualTo("scale");

        assertThat(stateValueFor(device, "batteryLevel")).isEqualTo("91 (%)");
        assertThat(stateValueFor(device, "co2")).isEqualTo("967 (ppm)");
        assertThat(stateValueFor(device, "temperature")).isEqualTo("23.6 (°C)");
    }

    @Override
    protected String getFileName() {
        return "withings.xml";
    }
}
