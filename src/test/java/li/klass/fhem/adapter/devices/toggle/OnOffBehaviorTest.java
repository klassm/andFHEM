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

package li.klass.fhem.adapter.devices.toggle;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.domain.EIBDevice;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.testutil.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(DataProviderRunner.class)
public class OnOffBehaviorTest {
    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @InjectMocks
    private OnOffBehavior onOffBehavior;

    @Mock
    private DeviceHookProvider deviceHookProvider;

    @DataProvider
    public static Object[][] isOnProvider() {
        return new Object[][]{
                {"on", true},
                {"off", false},
                {"dim20%", true},
                {"off-for-timer 2000", false},
                {"on-for-timer 2000", true}
        };
    }

    @UseDataProvider("isOnProvider")
    @Test
    public void should_recognize_on_and_off_states_correctly(String readState, boolean isOn) {

        //  given
        ToggleableDevice device = new EIBDevice();
        device.setXmlListDevice(mock(XmlListDevice.class));
        device.setState(readState);

        // expect
        assertThat(onOffBehavior.isOnByState(device)).isEqualTo(isOn);
        assertThat(onOffBehavior.isOffByState(device)).isEqualTo(!isOn);
    }

    @UseDataProvider("isOnProvider")
    @Test
    public void should_handle_invert_state_hook(String readState, boolean isOn) {
        //  given
        ToggleableDevice device = new EIBDevice();
        device.setXmlListDevice(mock(XmlListDevice.class));
        device.setState(readState);
        device.setName("on");
        given(deviceHookProvider.invertState(device)).willReturn(false);

        ToggleableDevice device2 = new EIBDevice();
        device2.setXmlListDevice(mock(XmlListDevice.class));
        device2.setState(readState);
        device.setName("off");
        given(deviceHookProvider.invertState(device2)).willReturn(true);

        // expect
        assertThat(onOffBehavior.isOn(device)).isEqualTo(isOn);
        assertThat(onOffBehavior.isOn(device2)).isEqualTo(!isOn);
    }
}