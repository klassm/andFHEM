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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class HourCounterDeviceTest extends DeviceXMLParsingBase {
    @DataProvider
    public static Object[][] PROVIDER() {
        return new Object[][]{
                {"Gaszaehler", "626.54 (m³)", "365.1 (€)", "1.07 (€)", "18.5 (kWh)", "23.02.2015 21:00"},
                {"Stromzaehler", "2304.53 (kW)", "594.57 (€)", "1.43 (€)", "6.0 (kWh)", "23.02.2015 21:35"},
        };
    }

    @Test
    @UseDataProvider("PROVIDER")
    public void should_read_device_attributes(String deviceName, String totalCounter, String totalPrice,
                                              String dayPrice, String dayCounter, String measured) {
        HourCounterDevice device = getDeviceFor(deviceName, HourCounterDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getMeasured()).isEqualTo(measured);
        assertThat(device.getCumulativeUsage()).isEqualTo(totalCounter);
        assertThat(device.getPrice()).isEqualTo(totalPrice);
        assertThat(device.getPricePerDay()).isEqualTo(dayPrice);
        assertThat(device.getDayKwh()).isEqualTo(dayCounter);
    }

    @Override
    protected String getFileName() {
        return "hourCounter.xml";
    }
}
