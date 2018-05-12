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

package li.klass.fhem.adapter.devices.toggle

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.adapter.devices.hook.ButtonHook
import li.klass.fhem.adapter.devices.hook.ButtonHook.*
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.testutil.MockitoRule
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`

@RunWith(DataProviderRunner::class)
class OnOffBehaviorTest {
    @get:Rule
    val mockitoRule = MockitoRule()

    @InjectMocks
    private lateinit var onOffBehavior: OnOffBehavior

    @Mock
    private lateinit var deviceHookProvider: DeviceHookProvider

    @UseDataProvider("isOnProvider")
    @Test
    fun should_recognize_on_and_off_states_correctly(testCase: IsOnTestCase) {

        //  given
        val xmlListDevice = XmlListDevice("BLUB")
                .apply {
                    setInternal("NAME", "Name")
                    setAttribute("eventMap", testCase.eventMap)
                    setHeader("sets", testCase.setList)
                }

        val device = FhemDevice(xmlListDevice)
        device.xmlListDevice.setInternal("STATE", testCase.state)
        `when`(deviceHookProvider.getOffStateName(device)).thenReturn("off")

        // expect
        assertThat(onOffBehavior.isOnByState(device)).isEqualTo(testCase.expected)
        assertThat(onOffBehavior.isOffByState(device)).isEqualTo(!testCase.expected)
    }

    @UseDataProvider("isOnProvider")
    @Test
    fun should_calculate_on_off(testCase: IsOnTestCase) {
        //  given
        val xmlListDevice = XmlListDevice("BLUB")
                .apply {
                    setInternal("NAME", "Name")
                    setAttribute("eventMap", testCase.eventMap)
                    setHeader("sets", testCase.setList)
                }

        val device = FhemDevice(xmlListDevice)
        device.xmlListDevice.setInternal("STATE", testCase.state)
        `when`(deviceHookProvider.getOffStateName(device)).thenReturn("off")
        `when`(deviceHookProvider.invertState(device)).thenReturn(false)

        assertThat(onOffBehavior.isOn(device)).isEqualTo(testCase.expected)
    }

    @UseDataProvider("isOnProvider")
    @Test
    fun should_handle_invert_state(testCase: IsOnTestCase) {
        val xmlListDevice2 = XmlListDevice("BLA")
                .apply {
                    setInternal("NAME", "Name")
                    setAttribute("eventMap", testCase.eventMap)
                    setHeader("sets", testCase.setList)
                }
        val device2 = FhemDevice(xmlListDevice2)
        device2.xmlListDevice.setInternal("STATE", testCase.state)
        `when`(deviceHookProvider.getOffStateName(device2)).thenReturn("off")
        `when`(deviceHookProvider.invertState(device2)).thenReturn(true)

        // expect
        assertThat(onOffBehavior.isOn(device2)).isEqualTo(!testCase.expected)
    }

    @Test
    @UseDataProvider("hookProvider")
    @Throws(Exception::class)
    fun isOnConsideringHooks(testCase: HookProviderTestCase) {
        val device = FhemDevice(XmlListDevice("GENERIC", HashMap(), HashMap(), HashMap(), HashMap()))
        `when`(deviceHookProvider.buttonHookFor(device)).thenReturn(testCase.hook)
        `when`(deviceHookProvider.getOffStateName(device)).thenReturn("off")
        device.xmlListDevice.setInternal("STATE", if (testCase.isOn) "on" else "off")

        val result = onOffBehavior.isOnConsideringHooks(device)

        assertThat(result).isEqualTo(testCase.expected)
    }

    @Test
    @UseDataProvider("supportsProvider")
    fun calculateSupports(testCase: SupportsTestCase) {
        val sets = testCase.setList.joinToString(" ")
        val headers = mapOf("sets" to DeviceNode(DeviceNode.DeviceNodeType.HEADER, "sets", sets, DateTime.now()))
                .toMutableMap()
        val device = FhemDevice(XmlListDevice("GENERIC", HashMap(), HashMap(), HashMap(), headers))

        `when`(deviceHookProvider.getOffStateName(device)).thenReturn(testCase.offStateNameHook)
        `when`(deviceHookProvider.getOnStateName(device)).thenReturn(testCase.onStateNameHook)

        val supports = onOffBehavior.supports(device)

        assertThat(supports).isEqualTo(testCase.expectedSupports)
    }

    data class IsOnTestCase(val state: String,
                            val eventMap: String = "",
                            val setList: String = "",
                            val expected: Boolean)

    data class HookProviderTestCase(val hook: ButtonHook, val isOn: Boolean, val expected: Boolean)

    data class SupportsTestCase(val setList: Set<String>,
                                val onStateNameHook: String?,
                                val offStateNameHook: String?,
                                val expectedSupports: Boolean)

    companion object {
        @DataProvider
        @JvmStatic
        fun supportsProvider(): List<SupportsTestCase> {
            return listOf(
                    SupportsTestCase(
                            setList = setOf("on", "off"),
                            onStateNameHook = null,
                            offStateNameHook = null,
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("ON", "OFF"),
                            onStateNameHook = null,
                            offStateNameHook = null,
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("on", "bla"),
                            onStateNameHook = null,
                            offStateNameHook = "bla",
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("blub", "bla"),
                            onStateNameHook = "blub",
                            offStateNameHook = "bla",
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("bla", "blub"),
                            onStateNameHook = null,
                            offStateNameHook = null,
                            expectedSupports = false
                    )
            )
        }

        @DataProvider
        @JvmStatic
        fun isOnProvider(): List<IsOnTestCase> {
            return listOf(
                    IsOnTestCase(state = "on", expected = true),
                    IsOnTestCase(state = "off", expected = false),
                    IsOnTestCase(state = "dim20%", expected = true),
                    IsOnTestCase(state = "off-for-timer 2000", expected = false),
                    IsOnTestCase(state = "on-for-timer 2000", expected = true),
                    IsOnTestCase(state = "B0", eventMap = "BI:on B0:off", setList = "state:BI,B0 on off", expected = false),
                    IsOnTestCase(state = "B1", eventMap = "BI:on B0:off", setList = "state:BI,B0 on off", expected = true)
            )
        }

        @DataProvider
        @JvmStatic
        fun hookProvider(): List<HookProviderTestCase> {
            return listOf(
                    HookProviderTestCase(hook = ON_DEVICE, isOn = true, expected = true),
                    HookProviderTestCase(hook = ON_DEVICE, isOn = false, expected = true),
                    HookProviderTestCase(hook = OFF_DEVICE, isOn = true, expected = false),
                    HookProviderTestCase(hook = OFF_DEVICE, isOn = false, expected = false),
                    HookProviderTestCase(hook = NORMAL, isOn = true, expected = true),
                    HookProviderTestCase(hook = NORMAL, isOn = false, expected = false)
            )
        }
    }
}