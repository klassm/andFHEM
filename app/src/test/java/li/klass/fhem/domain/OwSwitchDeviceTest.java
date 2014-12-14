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

import static li.klass.fhem.domain.OwSwitchDevice.setStateForAB;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class OwSwitchDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        OwSwitchDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.isOnA(), is(true));
        assertThat(device.isOnB(), is(false));
    }

    @Test
    public void testReadGPIO() {
        assertGPIO("0", true, true);
        assertGPIO("1", false, true);
        assertGPIO("2", true, false);
        assertGPIO("3", false, false);
    }

    private void assertGPIO(String value, boolean aExpect, boolean bExpect) {
        OwSwitchDevice device = new OwSwitchDevice();

        device.readGPIO(value);
        assertThat(device.isOnA(), is(aExpect));
        assertThat(device.isOnB(), is(bExpect));
    }

    @Test
    public void testSetStateForAB() {
        assertThat(setStateForAB(true, true), is(0));
        assertThat(setStateForAB(false, true), is(1));
        assertThat(setStateForAB(true, false), is(2));
        assertThat(setStateForAB(false, false), is(3));
    }

    @Override
    protected String getFileName() {
        return "owswitch.xml";
    }
}
