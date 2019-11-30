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

package li.klass.fhem.domain.setlist

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.DataProviders.testForEach
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.domain.setlist.typeEntry.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class SetListTest {

    @UseDataProvider("setListProvider")
    @Test
    fun should_parse_set_list(testCase: TestCase) {
        val setList = SetList.parse(testCase.setList)

        assertThat(setList.entries).isEqualTo(testCase.expected)
    }

    @Test
    fun should_get_first_present_element_if_element_is_not_present() {
        val setList = SetList.parse("bla")

        assertThat(setList.getFirstPresentStateOf("blub")).isNull()
    }

    data class TestCase internal constructor(val desc: String, val setList: String, val expected: Map<String, SetListEntry>) {
        override fun toString(): String {
            return "$desc {setList='$setList', expected=$expected}"
        }
    }

    companion object {
        @DataProvider
        @JvmStatic
        fun setListProvider(): Array<Array<Any>> {
            return testForEach(
                    TestCase(desc = "only colon",
                            setList = " : ",
                            expected = emptyMap()),
                    TestCase(desc = "noArg",
                            setList = "on:noArg",
                            expected = mapOf("on" to NoArgSetListEntry("on"))),
                    TestCase(desc = "no arg defaults to textField item type", setList = "on",
                             expected = mapOf("on" to TextFieldSetListEntry("on"))),
                    TestCase(desc = "empty :textField",
                            setList = "0:noArg 1:noArg :textField",
                            expected = mapOf("0" to NoArgSetListEntry("0"), "1" to NoArgSetListEntry("1"), "state" to TextFieldSetListEntry("state"))),
                    TestCase(desc = "empty set list",
                            setList = "",
                            expected = emptyMap()),
                    TestCase(desc = "leading trailing whitepace",
                             setList = " on off ",
                             expected = mapOf("on" to TextFieldSetListEntry("on"),
                                              "off" to TextFieldSetListEntry("off"))),
                    TestCase(desc = "colon without values leads to group",
                            setList = "internalState:",
                            expected = emptyMap()),
                    TestCase(desc = "slider",
                            setList = "internalState:slider,1,2,3 dim:slider,0,5,100",
                            expected = mapOf("internalState" to SliderSetListEntry("internalState", 1.0, 2.0, 3.0), "dim" to SliderSetListEntry("dim", 0.0, 5.0, 100.0))),
                    TestCase(desc = "slider desiredTemperature",
                            setList = "desiredTemperature:slider,4.5,0.5,29.5,1",
                            expected = mapOf("desiredTemperature" to SliderSetListEntry("desiredTemperature", 4.5, 0.5, 29.5))),
                    TestCase(desc = "group",
                            setList = "level:1,2,3 internalState:on,off",
                            expected = mapOf("level" to GroupSetListEntry("level", listOf("1", "2", "3")), "internalState" to GroupSetListEntry("internalState", listOf("on", "off")))),
                    TestCase(desc = "time",
                            setList = "internalState:time",
                            expected = mapOf("internalState" to TimeSetListEntry("internalState"))),
                    TestCase(desc = "multiple",
                            setList = "blab:multiple,bla,blub",
                            expected = mapOf("blab" to MultipleSetListEntry("blab", listOf("multiple", "bla", "blub")))),
                    TestCase(desc = "multiple-strict",
                            setList = "blab:multiple-strict,bla,blub",
                            expected = mapOf("blab" to MultipleStrictSetListEntry("blab", listOf("multiple-strict", "bla", "blub")))),
                    TestCase(desc = "textField",
                            setList = "blab:textField",
                            expected = mapOf("blab" to TextFieldSetListEntry("blab"))),
                    TestCase(desc = "textField-long",
                            setList = "blab:textField-long",
                            expected = mapOf("blab" to TextFieldLongSetListEntry("blab"))),
                    TestCase(desc = "RGB",
                            setList = "blab:colorpicker,RGB",
                            expected = mapOf("blab" to RGBSetListEntry("blab"))),
                    TestCase(desc = "colorPicker with group",
                            setList = "ct:colorpicker,CT,154,1,500",
                            expected = mapOf("ct" to SliderSetListEntry("ct", 154.0, 1.0, 500.0))),
                    TestCase(desc = "colorPicker without argument",
                            setList = "ct:colorpicker",
                            expected = mapOf("ct" to NoArgSetListEntry("ct"))),
                    TestCase(desc = "colorPicker non RGB",
                            setList = "pct:colorpicker,BRI,0,1,100",
                            expected = mapOf("pct" to SliderSetListEntry("pct", 0.0, 1.0, 100.0))),
                    TestCase(desc = "internalState is group",
                            setList = "internalState:Manuell,Sonnenaufgang_real,Sonnenaufgang_zivil,05:00,06:00,07:00,08:00",
                            expected = mapOf("internalState" to GroupSetListEntry("internalState", listOf("Manuell", "Sonnenaufgang_real", "Sonnenaufgang_zivil", "05:00", "06:00", "07:00", "08:00")))),
                    TestCase(desc = "including a question mark producing an invalid regexp",
                            setList = "attrTemplate:?,A_01_tasmota_basic_noprefix,A_01b_tasmota_1ch+motion+SI7021_noprefix,A_02a_tasmota_2channel_2devices_noprefix,A_04a_tasmota_sonoff_4ch_noprefix,A_10_shelly1,A_10a_shellyplug,A_11a_shelly2,A_14_shelly4pro,A_15_shellybulb,L_01a_zigbee2mqtt_bridge,L_01b_zigbee2mqtt_bridge_V2_speaking_names,L_01x_zigbee2mqtt_bridge_outdated,L_02a_zigbee2mqtt_bulb,L_02b_zigbee2mqtt_colorbulb,L_02b_zigbee2mqtt_colorbulbWithoutColorTemp,L_03_zigbee2mqtt_smokeDetector,L_04_zigbee2mqtt_hueMotionSensor,L_05_zigbee2mqtt_smart+plug,X_01_esp_milight_hub_bridge,X_01_esp_milight_hub_rgbw_bulb",
                            expected = mapOf("attrTemplate" to GroupSetListEntry("attrTemplate", listOf("?", "A_01_tasmota_basic_noprefix", "A_01b_tasmota_1ch+motion+SI7021_noprefix", "A_02a_tasmota_2channel_2devices_noprefix", "A_04a_tasmota_sonoff_4ch_noprefix", "A_10_shelly1", "A_10a_shellyplug", "A_11a_shelly2", "A_14_shelly4pro", "A_15_shellybulb", "L_01a_zigbee2mqtt_bridge", "L_01b_zigbee2mqtt_bridge_V2_speaking_names", "L_01x_zigbee2mqtt_bridge_outdated", "L_02a_zigbee2mqtt_bulb", "L_02b_zigbee2mqtt_colorbulb", "L_02b_zigbee2mqtt_colorbulbWithoutColorTemp", "L_03_zigbee2mqtt_smokeDetector", "L_04_zigbee2mqtt_hueMotionSensor", "L_05_zigbee2mqtt_smart+plug", "X_01_esp_milight_hub_bridge", "X_01_esp_milight_hub_rgbw_bulb"))))
            )
        }
    }
}
