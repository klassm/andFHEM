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

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class TRXLightDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        TRXLightDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.isOn(), is(false));
        assertThat(device.getState(), is("off"));
        assertThat(device.getType(), is("ARC"));

        assertThat(device.getAvailableTargetStates(), is(notNullValue()));

        assertThat(device.getFileLog(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));

        assertThat(device.supportsToggle(), is(true));
        assertThat(device.supportsDim(), is(false));

        TRXLightDevice device1 = getDeviceFor("device1");
        assertThat(device1.isOn(), is(true));
        assertThat(device.supportsDim(), is(false));

        TRXLightDevice device2 = getDeviceFor("device2");
        assertThat(device2.getState(), is("on"));
        assertThat(device2.isOn(), is(true));
        assertThat(device2.supportsDim(), is(true));
        assertThat(device2.getDimPosition(), is(15));

        TRXLightDevice device3 = getDeviceFor("device3");
        assertThat(device3.getState(), is("level 12"));
        assertThat(device3.isOn(), is(true));
        assertThat(device3.supportsDim(), is(true));
        assertThat(device3.getDimPosition(), is(12));
    }

    @Test
    public void testFormatTargetState() {
        TRXLightDevice device3 = getDeviceFor("device3");

        device3.setState("off");
        assertThat(device3.formatTargetState("level 13"), is("level 13"));
        assertThat(device3.formatTargetState("on"), is("on"));
        assertThat(device3.formatTargetState("off"), is("off"));

        device3.setState("level 13");
        assertThat(device3.formatTargetState("level 12"), is("level 12"));
    }

    @Test
    public void testSetState() {
        TRXLightDevice device3 = getDeviceFor("device3");

        device3.setState("level 5");
        assertThat(device3.getState(), is("level 5"));
    }

    @Override
    protected String getFileName() {
        return "trx_light.xml";
    }
}
