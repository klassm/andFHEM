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

package li.klass.fhem.adapter.devices.hook;

import com.google.common.collect.Maps;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static com.tngtech.java.junit.dataprovider.DataProviders.$;
import static com.tngtech.java.junit.dataprovider.DataProviders.$$;
import static li.klass.fhem.adapter.devices.hook.DeviceHookProvider.HOOK_OFF;
import static li.klass.fhem.adapter.devices.hook.DeviceHookProvider.HOOK_ON;
import static li.klass.fhem.adapter.devices.hook.DeviceHookProvider.HOOK_ON_OFF;
import static li.klass.fhem.adapter.devices.hook.DeviceHookProvider.HOOK_TOGGLE;
import static li.klass.fhem.adapter.devices.hook.DeviceHookProvider.HOOK_WEBCMD;
import static li.klass.fhem.adapter.devices.hook.DeviceHookProvider.OFF_STATE_NAME;
import static li.klass.fhem.domain.core.DeviceType.GENERIC;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.ATTR;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class DeviceHookProviderTest {
    @DataProvider
    public static Object[][] buttonHookProvider() {
        deviceFor(HOOK_OFF, true);
        return new Object[][]{
                {deviceFor(HOOK_OFF, true), ButtonHook.OFF_DEVICE},
                {deviceFor(HOOK_OFF, false), ButtonHook.NORMAL},
                {deviceFor(HOOK_ON, true), ButtonHook.ON_DEVICE},
                {deviceFor(HOOK_ON, false), ButtonHook.NORMAL},
                {deviceFor(HOOK_ON_OFF, true), ButtonHook.ON_OFF_DEVICE},
                {deviceFor(HOOK_ON_OFF, false), ButtonHook.NORMAL},
                {deviceFor(HOOK_TOGGLE, true), ButtonHook.TOGGLE_DEVICE},
                {deviceFor(HOOK_TOGGLE, false), ButtonHook.NORMAL},
                {deviceFor(HOOK_WEBCMD, true), ButtonHook.WEBCMD_DEVICE},
                {deviceFor(HOOK_WEBCMD, false), ButtonHook.NORMAL},
        };
    }

    @Test
    @UseDataProvider("buttonHookProvider")
    public void should_parse_button_hook(FhemDevice device, ButtonHook expectedHookType) {
        DeviceHookProvider provider = new DeviceHookProvider();

        ButtonHook hook = provider.buttonHookFor(device);

        assertThat(hook).isEqualTo(expectedHookType);
    }

    private static GenericDevice deviceFor(String hookAttribute, boolean isActive) {
        String value = isActive ? "true" : "false";

        XmlListDevice xmlListDevice = new XmlListDevice(GENERIC.getXmllistTag(),
                Maps.<String, DeviceNode>newHashMap(),
                Maps.<String, DeviceNode>newHashMap(),
                Maps.<String, DeviceNode>newHashMap(),
                Maps.<String, DeviceNode>newHashMap());
        xmlListDevice.getAttributes().put(hookAttribute, new DeviceNode(ATTR, hookAttribute, value, (DateTime) null));
        xmlListDevice.setInternal("NAME", "name");
        GenericDevice device = new GenericDevice();
        device.setXmlListDevice(xmlListDevice);

        return device;
    }

    @DataProvider
    public static Object[][] offStateNameProvider() {
        return $$(
                $("", "", "off"),
                $("on", "off", "off"),
                $("off", "OFF", "OFF")
        );
    }

    @UseDataProvider("offStateNameProvider")
    @Test
    public void should_provide_off_state_name(String setList, String offStateName, String expectedState) throws Exception {
        DeviceHookProvider provider = new DeviceHookProvider();
        GenericDevice device = deviceFor(HOOK_OFF, true);
        device.setSetList(setList);
        device.getXmlListDevice().setAttribute(OFF_STATE_NAME, offStateName);

        String stateName = provider.getOffStateName(device);

        assertThat(stateName).isEqualTo(expectedState);
    }
}