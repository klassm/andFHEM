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

import li.klass.fhem.adapter.devices.hook.ButtonHook;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.domain.EIBDevice;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;

import static com.tngtech.java.junit.dataprovider.DataProviders.$;
import static com.tngtech.java.junit.dataprovider.DataProviders.$$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
        device.setXmlListDevice(new XmlListDevice("BLUB"));
        device.setState(readState);
        when(deviceHookProvider.getOffStateName(device)).thenReturn("off");

        // expect
        assertThat(onOffBehavior.isOnByState(device)).isEqualTo(isOn);
        assertThat(onOffBehavior.isOffByState(device)).isEqualTo(!isOn);
    }

    @UseDataProvider("isOnProvider")
    @Test
    public void should_handle_invert_state_hook(String readState, boolean isOn) {
        //  given
        ToggleableDevice device = new EIBDevice();
        XmlListDevice xmlListDevice = new XmlListDevice("BLUB");
        xmlListDevice.setInternal("NAME", "Name");
        device.setXmlListDevice(xmlListDevice);
        device.setState(readState);
        when(deviceHookProvider.getOffStateName(device)).thenReturn("off");
        when(deviceHookProvider.invertState(device)).thenReturn(false);

        ToggleableDevice device2 = new EIBDevice();
        XmlListDevice xmlListDevice2 = new XmlListDevice("BLA");
        xmlListDevice.setInternal("NAME", "name");
        device2.setXmlListDevice(xmlListDevice2);
        device2.setState(readState);
        when(deviceHookProvider.getOffStateName(device2)).thenReturn("off");
        when(deviceHookProvider.invertState(device2)).thenReturn(true);

        // expect
        assertThat(onOffBehavior.isOn(device)).isEqualTo(isOn);
        assertThat(onOffBehavior.isOn(device2)).isEqualTo(!isOn);
    }

    @DataProvider
    public static Object[][] hookProvider() {
        return $$(
                $(ButtonHook.ON_DEVICE, true, true),
                $(ButtonHook.ON_DEVICE, false, true),
                $(ButtonHook.OFF_DEVICE, true, false),
                $(ButtonHook.OFF_DEVICE, false, false),
                $(ButtonHook.NORMAL, true, true),
                $(ButtonHook.NORMAL, false, false)
        );
    }

    @Test
    @UseDataProvider("hookProvider")
    public void isOnConsideringHooks(ButtonHook hook, boolean isOn, boolean expected) throws Exception {
        GenericDevice device = new GenericDevice();
        device.setXmlListDevice(new XmlListDevice("GENERIC"));
        when(deviceHookProvider.buttonHookFor(device)).thenReturn(hook);
        when(deviceHookProvider.getOffStateName(device)).thenReturn("off");
        device.setState(isOn ? "on" : "off");

        boolean result = onOffBehavior.isOnConsideringHooks(device);

        assertThat(result).isEqualTo(expected);
    }
}