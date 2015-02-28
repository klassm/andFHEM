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

public class OregonDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        OregonDevice device = getDefaultDevice(OregonDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getBattery()).isEqualTo("90 (%)");
        assertThat(device.getDewpoint()).isEqualTo("4.3 (°C)");
        assertThat(device.getForecast()).isEqualTo("rain");
        assertThat(device.getHumidity()).isEqualTo("46 (%)");
        assertThat(device.getPressure()).isEqualTo("1000 (hPa)");
        assertThat(device.getTemperature()).isEqualTo("15.9 (°C)");
        assertThat(device.getRainRate()).isEqualTo("0 (mm/h)");
        assertThat(device.getRainTotal()).isEqualTo("976.2998 (l/m2)");
        assertThat(device.getWindAvgSpeed()).isEqualTo("0 (km/h)");
        assertThat(device.getWindDirection()).isEqualTo("245 SW");
        assertThat(device.getWindSpeed()).isEqualTo("0 (km/h)");
        assertThat(device.getUvValue()).isEqualTo("10");
        assertThat(device.getUvRisk()).isEqualTo("high");
        assertThat(device.getState()).isEqualTo("T: 15.9  H: 46");

        assertThat(device.getSetList().getEntries().size()).isEqualTo(0);

        assertThat(device.getLogDevices()).isNotNull();
        assertThat(device.getDeviceCharts().size()).isEqualTo(6);
    }

    @Override
    protected String getFileName() {
        return "oregon.xml";
    }
}
