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

    class TestCase internal constructor(private val desc: String) {
        lateinit var setList: String
        lateinit var expected: Map<String, SetListEntry>

        internal fun withSetList(setList: String): TestCase {
            this.setList = setList
            return this
        }

        internal fun thenExpect(vararg expected: Pair<String, SetListEntry>): TestCase {
            this.expected = expected.toMap()
            return this
        }

        internal fun thenExpectEmptySetList(): TestCase {
            this.expected = emptyMap()
            return this
        }

        override fun toString(): String {
            return desc + " {" +
                    "setList='" + setList + '\'' +
                    ", expected=" + expected +
                    '}'
        }
    }

    companion object {
        @DataProvider
        @JvmStatic
        fun setListProvider(): Array<Array<Any>> {
            return testForEach(
                    TestCase("only colon")
                            .withSetList(" : ")
                            .thenExpectEmptySetList(),
                    TestCase("noArg")
                            .withSetList("on:noArg")
                            .thenExpect("on" to NoArgSetListEntry("on")),
                    TestCase("empty :textField")
                            .withSetList("0:noArg 1:noArg :textField")
                            .thenExpect("0" to NoArgSetListEntry("0"), "1" to NoArgSetListEntry("1"), "state" to TextFieldSetListEntry("state")),
                    TestCase("empty set list")
                            .withSetList("")
                            .thenExpectEmptySetList(),
                    TestCase("leading trailing whitepace")
                            .withSetList(" on off ")
                            .thenExpect("on" to NoArgSetListEntry("on"), "off" to NoArgSetListEntry("off")),
                    TestCase("colon without values leads to group")
                            .withSetList("state:")
                            .thenExpectEmptySetList(),
                    TestCase("slider")
                            .withSetList("state:slider,1,2,3 dim:slider,0,5,100")
                            .thenExpect("state" to SliderSetListEntry("state", 1f, 2f, 3f), "dim" to SliderSetListEntry("dim", 0f, 5f, 100f)),
                    TestCase("slider desiredTemperature")
                            .withSetList("desiredTemperature:slider,4.5,0.5,29.5,1")
                            .thenExpect("desiredTemperature" to SliderSetListEntry("desiredTemperature", 4.5f, 0.5f, 29.5f)),
                    TestCase("group")
                            .withSetList("level:1,2,3 state:on,off")
                            .thenExpect("level" to GroupSetListEntry("level", "1", "2", "3"), "state" to GroupSetListEntry("state", "on", "off")),
                    TestCase("time")
                            .withSetList("state:time")
                            .thenExpect("state" to TimeSetListEntry("state")),
                    TestCase("multiple")
                            .withSetList("blab:multiple,bla,blub")
                            .thenExpect("blab" to MultipleSetListEntry("blab", "multiple", "bla", "blub")),
                    TestCase("multiple-strict")
                            .withSetList("blab:multiple-strict,bla,blub")
                            .thenExpect("blab" to MultipleStrictSetListEntry("blab", "multiple-strict", "bla", "blub")),
                    TestCase("textField")
                            .withSetList("blab:textField")
                            .thenExpect("blab" to TextFieldSetListEntry("blab")),
                    TestCase("textField-long")
                            .withSetList("blab:textField-long")
                            .thenExpect("blab" to TextFieldLongSetListEntry("blab")),
                    TestCase("RGB")
                            .withSetList("blab:colorpicker,RGB")
                            .thenExpect("blab" to RGBSetListEntry("blab")),
                    TestCase("colorPicker with group")
                            .withSetList("ct:colorpicker,CT,154,1,500")
                            .thenExpect("ct" to SliderSetListEntry("ct", 154f, 1f, 500f)),
                    TestCase("colorPicker without argument")
                            .withSetList("ct:colorpicker")
                            .thenExpect("ct" to NoArgSetListEntry("ct")),
                    TestCase("colorPicker non RGB")
                            .withSetList("pct:colorpicker,BRI,0,1,100")
                            .thenExpect("pct" to SliderSetListEntry("pct", 0f, 1f, 100f)),
                    TestCase("state is group")
                            .withSetList("state:Manuell,Sonnenaufgang_real,Sonnenaufgang_zivil,05:00,06:00,07:00,08:00")
                            .thenExpect("state" to GroupSetListEntry("state", "Manuell", "Sonnenaufgang_real", "Sonnenaufgang_zivil", "05:00", "06:00", "07:00", "08:00"))

            )
        }
    }
}
