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
import li.klass.fhem.domain.core.ToggleableDevice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class FS20DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInFirstDevice() {
        FS20Device device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("on"));
        assertThat(device.isOnByState(), is(true));
        assertThat(device.isSpecialButtonDevice(), is(false));

        assertThat(device.getEventMap().get("off-for-timer 12"), is("Ab80"));
        assertThat(device.getEventMap().get("off"), is("Ab"));
        assertThat(device.getEventMap().get("on"), is("Auf"));

        assertThat(device.getAvailableTargetStates(), is(notNullValue()));

        assertThat(device.getFileLog(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));
    }

    @Test
    public void testForCorrectlySetAttributesInSecondDevice() {
        FS20Device device = getDeviceFor("device1");

        assertThat(device.getName(), is("device1"));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("off"));
        assertThat(device.isOnByState(), is(false));

        assertThat(device.getEventMapStateFor("off"), is("closed"));
        assertThat(device.getEventMapStateFor("on"), is("open"));
        assertThat(device.getEventMapStateFor("dummy"), is("dummy"));

        assertThat(device.getAlias(), is("myAlias"));
        assertThat(device.isSpecialButtonDevice(), is(true));
        assertThat(device.getButtonHookType(), is(ToggleableDevice.ButtonHookType.ON_OFF_DEVICE));

        assertThat(device.getAvailableTargetStates(), is(notNullValue()));

        assertThat(device.getFileLog(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(1));

        assertThat(device.getWidgetName(), is("myAlias"));
    }

    @Test
    public void testForCorrectlySetAttributesInThirdDevice() {
        FS20Device device = getDeviceFor("device2");

        assertThat(device.isOnRespectingInvertHook(), is(true));
        assertThat(device.isOnByState(), is(false));
        assertThat(device.getWidgetName(), is("widget_name"));
    }

    @Test
    public void testDim() {
        FS20Device device = new FS20Device();
        device.setState("dim12%");

        assertThat(device.getDimPosition(), is(2));

        assertThat(device.getDimDownPosition(), is(FS20Device.dimStates.indexOf("dim6%")));
        assertThat(device.getDimUpPosition(), is(FS20Device.dimStates.indexOf("dim18%")));

        device.setState("on");
        assertThat(device.getDimUpPosition(), is(FS20Device.dimStates.indexOf("dim100%")));

        device.setState("off");
        assertThat(device.getDimDownPosition(), is(FS20Device.dimStates.indexOf("off")));
    }

    @Override
    protected String getFileName() {
        return "fs20.xml";
    }
}
