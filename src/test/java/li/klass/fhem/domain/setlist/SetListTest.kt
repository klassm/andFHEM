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
import com.tngtech.java.junit.dataprovider.DataProviders
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.domain.setlist.typeEntry.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.assertj.core.data.MapEntry
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(DataProviderRunner::class)
class SetListTest {

    @UseDataProvider("setListProvider")
    @Test
    fun should_parse_set_list(testCase: TestCase) {
        val setList = SetList.parse(testCase.setList)

        assertThat(setList.entries).containsOnly(*testCase.expected)
    }

    class TestCase internal constructor(private val desc: String) {
        lateinit var setList: String
        lateinit var expected: Array<out MapEntry>

        internal fun withSetList(setList: String): TestCase {
            this.setList = setList
            return this
        }

        internal fun thenExpect(vararg expected: MapEntry): TestCase {
            this.expected = expected
            return this
        }

        internal fun thenExpectEmptySetList(): TestCase {
            this.expected = emptyArray()
            return this
        }

        override fun toString(): String {
            return desc + " {" +
                    "setList='" + setList + '\'' +
                    ", expected=" + Arrays.toString(expected) +
                    '}'
        }
    }

    companion object {
        @DataProvider
        @JvmStatic
        fun setListProvider(): Array<Array<Any>> {
            return DataProviders.testForEach(
                    TestCase("only colon")
                            .withSetList(" : ")
                            .thenExpectEmptySetList(),
                    TestCase("noArg")
                            .withSetList("on:noArg")
                            .thenExpect(entry("on", NoArgSetListEntry("on"))),
                    TestCase("empty :textField")
                            .withSetList("0:noArg 1:noArg :textField")
                            .thenExpect(entry("0", NoArgSetListEntry("0")), entry("1", NoArgSetListEntry("1")), entry("state", TextFieldSetListEntry("state"))),
                    TestCase("empty set list")
                            .withSetList("")
                            .thenExpectEmptySetList(),
                    TestCase("leading trailing whitepace")
                            .withSetList(" on off ")
                            .thenExpect(entry("on", NoArgSetListEntry("on")), entry("off", NoArgSetListEntry("off"))),
                    TestCase("colon without values leads to group")
                            .withSetList("state:")
                            .thenExpectEmptySetList(),
                    TestCase("slider")
                            .withSetList("state:slider,1,2,3 dim:slider,0,5,100")
                            .thenExpect(entry("state", SliderSetListEntry("state", 1f, 2f, 3f)), entry("dim", SliderSetListEntry("dim", 0f, 5f, 100f))),
                    TestCase("slider desiredTemperature")
                            .withSetList("desiredTemperature:slider,4.5,0.5,29.5,1")
                            .thenExpect(entry("desiredTemperature", SliderSetListEntry("desiredTemperature", 4.5f, 0.5f, 29.5f))),
                    TestCase("group")
                            .withSetList("level:1,2,3 state:on,off")
                            .thenExpect(entry("level", GroupSetListEntry("level", "1", "2", "3")), entry("state", GroupSetListEntry("state", "on", "off"))),
                    TestCase("time")
                            .withSetList("state:time")
                            .thenExpect(entry("state", TimeSetListEntry("state"))),
                    TestCase("multiple")
                            .withSetList("blab:multiple,bla,blub")
                            .thenExpect(entry("blab", MultipleSetListEntry("blab", "multiple", "bla", "blub"))),
                    TestCase("multiple-strict")
                            .withSetList("blab:multiple-strict,bla,blub")
                            .thenExpect(entry("blab", MultipleStrictSetListEntry("blab", "multiple-strict", "bla", "blub"))),
                    TestCase("textField")
                            .withSetList("blab:textField")
                            .thenExpect(entry("blab", TextFieldSetListEntry("blab"))),
                    TestCase("textField-long")
                            .withSetList("blab:textField-long")
                            .thenExpect(entry("blab", TextFieldLongSetListEntry("blab"))),
                    TestCase("RGB")
                            .withSetList("blab:colorpicker,RGB")
                            .thenExpect(entry("blab", RGBSetListEntry("blab"))),
                    TestCase("colorPicker with group")
                            .withSetList("ct:colorpicker,CT,154,1,500")
                            .thenExpect(entry("ct", SliderSetListEntry("ct", 154f, 1f, 500f))),
                    TestCase("colorPicker without argument")
                            .withSetList("ct:colorpicker")
                            .thenExpect(entry("ct", NoArgSetListEntry("ct"))),
                    TestCase("colorPicker non RGB")
                            .withSetList("pct:colorpicker,BRI,0,1,100")
                            .thenExpect(entry("pct", SliderSetListEntry("pct", 0f, 1f, 100f))),
                    TestCase("state is group")
                            .withSetList("state:Manuell,Sonnenaufgang_real,Sonnenaufgang_zivil,05:00,06:00,07:00,08:00")
                            .thenExpect(entry("state", GroupSetListEntry("state", "Manuell", "Sonnenaufgang_real", "Sonnenaufgang_zivil", "05:00", "06:00", "07:00", "08:00")))

            )
        }
    }
}
