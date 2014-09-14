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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class HMSDeviceTest extends DeviceXMLParsingBase {
    @Override
    public void loadDevices() throws Exception {
        super.loadDevices();
    }

    @Test
    public void testForCorrectlySetAttributes() {
        HMSDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getHumidity(), is("40.0 (%)"));
        assertThat(device.getTemperature(), is("12.6 (°C)"));
        assertThat(device.getModel(), is("HMS100T"));
        assertThat(device.getBattery(), is("ok"));
        assertThat(device.getSwitchDetect(), is("on"));
        assertThat(device.getState(), is("T: 12.6  Bat: ok"));
        assertThat(device.getMeasured(), is("2010-04-05 14:06:52"));

        assertThat(device.getSetList().getEntries().size(), is(0));

        assertThat(device.getLogDevices(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(2));
    }

    @Override
    protected String getFileName() {
        return "hms.xml";
    }
}
