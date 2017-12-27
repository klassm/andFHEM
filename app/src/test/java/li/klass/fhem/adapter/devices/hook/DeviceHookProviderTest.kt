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

package li.klass.fhem.adapter.devices.hook

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType.ATTR
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class DeviceHookProviderTest {

    @Test
    @UseDataProvider("buttonHookProvider")
    fun should_parse_button_hook(testCase: ButtonHookTestCase) {
        val provider = DeviceHookProvider()

        val hook = provider.buttonHookFor(testCase.device)

        assertThat(hook).isEqualTo(testCase.expectedHookType)
    }

    @UseDataProvider("offStateNameProvider")
    @Test
    @Throws(Exception::class)
    fun should_provide_off_state_name(testCase: OffStateNameTestCase) {
        val provider = DeviceHookProvider()
        val device = deviceFor(DeviceHookProvider.HOOK_OFF, true, testCase.setList)
        device.xmlListDevice.setAttribute(DeviceHookProvider.OFF_STATE_NAME, testCase.offStateName)

        val stateName = provider.getOffStateName(device)

        assertThat(stateName).isEqualTo(testCase.expectedState)
    }

    @UseDataProvider("onStateNameProvider")
    @Test
    @Throws(Exception::class)
    fun should_provide_on_state_name(testCase: OnStateNameTestCase) {
        val provider = DeviceHookProvider()
        val device = deviceFor(DeviceHookProvider.HOOK_ON, true, testCase.setList)
        device.xmlListDevice.setAttribute(DeviceHookProvider.ON_STATE_NAME, testCase.onStateName)

        val stateName = provider.getOnStateName(device)

        assertThat(stateName).isEqualTo(testCase.expectedState)
    }

    data class OnStateNameTestCase(val setList: String, val onStateName: String, val expectedState: String?)
    data class OffStateNameTestCase(val setList: String, val offStateName: String, val expectedState: String?)
    data class ButtonHookTestCase(val device: FhemDevice, val expectedHookType: ButtonHook)

    companion object {
        @DataProvider
        @JvmStatic
        fun buttonHookProvider() =
                listOf(
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_OFF, true, ""), expectedHookType = ButtonHook.OFF_DEVICE),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_OFF, false, ""), expectedHookType = ButtonHook.NORMAL),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_ON, true, ""), expectedHookType = ButtonHook.ON_DEVICE),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_ON, false, ""), expectedHookType = ButtonHook.NORMAL),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_ON_OFF, true, ""), expectedHookType = ButtonHook.ON_OFF_DEVICE),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_ON_OFF, false, ""), expectedHookType = ButtonHook.NORMAL),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_TOGGLE, true, ""), expectedHookType = ButtonHook.TOGGLE_DEVICE),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_TOGGLE, false, ""), expectedHookType = ButtonHook.NORMAL),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_WEBCMD, true, ""), expectedHookType = ButtonHook.WEBCMD_DEVICE),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_WEBCMD, false, ""), expectedHookType = ButtonHook.NORMAL),
                        ButtonHookTestCase(device = deviceFor(DeviceHookProvider.HOOK_WEBCMD, true, "").apply {
                            xmlListDevice.setAttribute(DeviceHookProvider.WIDGET_OVERRIDE, DeviceHookProvider.WIDGET_OVERRIDE_NOARG)
                        }, expectedHookType = ButtonHook.DEVICE_VALUES)
                )

        private fun deviceFor(hookAttribute: String, isActive: Boolean, setList: String): FhemDevice {
            val value = if (isActive) "true" else "false"

            val xmlListDevice = XmlListDevice("Bla",
                    Maps.newHashMap(),
                    Maps.newHashMap(),
                    Maps.newHashMap(),
                    ImmutableMap.of("sets", DeviceNode(DeviceNode.DeviceNodeType.HEADER, "sets", setList, "")))
            xmlListDevice.attributes.put(hookAttribute, DeviceNode(ATTR, hookAttribute, value, null as DateTime?))
            xmlListDevice.setInternal("NAME", "name")

            return FhemDevice(xmlListDevice)
        }

        @DataProvider
        @JvmStatic
        fun offStateNameProvider() =
                listOf(
                        OffStateNameTestCase(setList = "", offStateName = "", expectedState = null),
                        OffStateNameTestCase(setList = "on", offStateName = "off", expectedState = "off"),
                        OffStateNameTestCase(setList = "off", offStateName = "OFF", expectedState = "OFF")
                )

        @DataProvider
        @JvmStatic
        fun onStateNameProvider() = listOf(
                OnStateNameTestCase(setList = "", onStateName = "", expectedState = null),
                OnStateNameTestCase(setList = "off", onStateName = "on", expectedState = "on"),
                OnStateNameTestCase(setList = "on", onStateName = "ON", expectedState = "ON")
        )
    }
}