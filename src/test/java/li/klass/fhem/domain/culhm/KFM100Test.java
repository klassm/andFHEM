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

package li.klass.fhem.domain.culhm;

import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class KFM100Test extends DeviceXMLParsingBase {

    @Test
    public void testForCorrectlySetAttributes() {
        CULHMDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("171.3 (l)"));
        assertThat(device.getSubType(), is(CULHMDevice.SubType.FILL_STATE));
        assertThat(device.supportsDim(), is(false));

        assertThat(device.getRawValue(), is("20"));
        assertThat(device.getFillContentLitresMaximum(), is(4198));
        assertThat(device.getFillContentPercentage(), is("4 (%)"));
        assertThat(device.getFillContentPercentageRaw(), is(closeTo(0.04, 0.01)));
        assertThat(device.getFillContentLitresRaw(), is(closeTo(171.3, 0.01)));
        assertThat(device.getRawToReadable(), is("10:0 255:4198"));

        assertThat(device.getLogDevices(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(1));

        assertThat(device.getMeasured(), is("2012-07-26 21:55:58"));

        device.setContent("4300l");
        device.afterAllXMLRead();
        assertThat(device.getFillContentPercentageRaw(), is(closeTo(1, 0.01)));

        assertThat(device.isSupported(), is(true));
    }

    @Override
    protected String getFileName() {
        return "kfm100.xml";
    }
}
