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

import static org.fest.assertions.api.Assertions.assertThat;

public class CM160DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testPropertiesSet() {
        CM160Device device = getDefaultDevice();

        assertThat(device.getCurrent()).isEqualTo("3.92 (A)");
        assertThat(device.getPower()).isEqualTo("901.6 (W)");
        assertThat(device.getCost()).isEqualTo("0.2461 (€/h)");
        assertThat(device.getCo2()).isEqualTo("0.4147 (kg/h)");

        assertThat(device.getCumHour()).isEqualTo("W: 101772 W, P: 1.70 kWh, C: 0.46 €, CO2: 0.78 kg");
        assertThat(device.getCumDay()).isEqualTo("W: 4026875 W, P: 67.11 kWh, C: 18.32 €, CO2: 30.87 kg");
        assertThat(device.getCumMonth()).isEqualTo("W: 5948688 W, P: 99.14 kWh, C: 27.07 €, CO2: 45.61 kg");
        assertThat(device.getCumYear()).isEqualTo("W: 5948688 W, P: 99.14 kWh, C: 27.07 €, CO2: 45.61 kg");

        assertThat(device.getState()).isEqualTo("A: 3.92 A, W: 901.60 W, C: 0.2461 €/h, CO2: 0.4147 kg/h");

        assertThat(device.getMeasured()).isEqualTo("2014-03-23 20:30:13");
    }

    @Override
    protected String getFileName() {
        return "cm160.xml";
    }
}
