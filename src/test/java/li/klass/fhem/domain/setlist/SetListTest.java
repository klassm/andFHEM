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

package li.klass.fhem.domain.setlist;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.DataProviders;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.MultipleSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.MultipleStrictSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.RGBSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.TextFieldLongSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.TextFieldSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.TimeSetListEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@RunWith(DataProviderRunner.class)
public class SetListTest {
    @DataProvider
    public static Object[][] setListProvider() {
        return DataProviders.testForEach(

                new TestCase("only colon")
                        .withSetList(" : ")
                        .thenExpectEmptySetList(),
                new TestCase("noArg")
                        .withSetList("on:noArg")
                        .thenExpect(entry("on", new NoArgSetListEntry("on"))),
                new TestCase("empty :textField")
                        .withSetList("0:noArg 1:noArg :textField")
                        .thenExpect(entry("0", new NoArgSetListEntry("0")), entry("1", new NoArgSetListEntry("1")), entry("state", new TextFieldSetListEntry("state"))),
                new TestCase("empty set list")
                        .withSetList("")
                        .thenExpectEmptySetList(),
                new TestCase("leading trailing whitepace")
                        .withSetList(" on off ")
                        .thenExpect(entry("on", new NoArgSetListEntry("on")), entry("off", new NoArgSetListEntry("off"))),
                new TestCase("colon without values leads to group")
                        .withSetList("state:")
                        .thenExpect(entry("state", new GroupSetListEntry("state"))),
                new TestCase("slider")
                        .withSetList("state:slider,1,2,3 dim:slider,0,5,100")
                        .thenExpect(entry("state", new SliderSetListEntry("state", 1, 2, 3)), entry("dim", new SliderSetListEntry("dim", 0, 5, 100))),
                new TestCase("slider desiredTemperature")
                        .withSetList("desiredTemperature:slider,4.5,0.5,29.5,1")
                        .thenExpect(entry("desiredTemperature", new SliderSetListEntry("desiredTemperature", 4.5f, 0.5f, 29.5f))),
                new TestCase("group")
                        .withSetList("level:1,2,3 state:on,off")
                        .thenExpect(entry("level", new GroupSetListEntry("level", "1", "2", "3")), entry("state", new GroupSetListEntry("state", "on", "off"))),
                new TestCase("time")
                        .withSetList("state:time")
                        .thenExpect(entry("state", new TimeSetListEntry("state"))),
                new TestCase("multiple")
                        .withSetList("blab:multiple,bla,blub")
                        .thenExpect(entry("blab", new MultipleSetListEntry("blab", "multiple", "bla", "blub"))),
                new TestCase("multiple-strict")
                        .withSetList("blab:multiple-strict,bla,blub")
                        .thenExpect(entry("blab", new MultipleStrictSetListEntry("blab", "multiple-strict", "bla", "blub"))),
                new TestCase("textField")
                        .withSetList("blab:textField")
                        .thenExpect(entry("blab", new TextFieldSetListEntry("blab"))),
                new TestCase("textField-long")
                        .withSetList("blab:textField-long")
                        .thenExpect(entry("blab", new TextFieldLongSetListEntry("blab"))),
                new TestCase("RGB")
                        .withSetList("blab:colorpicker,RGB")
                        .thenExpect(entry("blab", new RGBSetListEntry("blab"))),
                new TestCase("colorPicker with group")
                        .withSetList("ct:colorpicker,CT,154,1,500")
                        .thenExpect(entry("ct", new SliderSetListEntry("ct", 154, 1, 500))),
                new TestCase("colorPicker without argument")
                        .withSetList("ct:colorpicker")
                        .thenExpect(entry("ct", new NoArgSetListEntry("ct"))),
                new TestCase("colorPicker non RGB")
                        .withSetList("pct:colorpicker,BRI,0,1,100")
                        .thenExpect(entry("pct", new SliderSetListEntry("pct", 0, 1, 100)))

        );
    }

    @UseDataProvider("setListProvider")
    @Test
    public void should_parse_set_list(TestCase testCase) {
        SetList setList = new SetList();

        setList.parse(testCase.setList);

        assertThat(setList.getEntries()).containsOnly(testCase.expected);
    }

    private static class TestCase {
        private final String desc;
        private String setList;
        private MapEntry[] expected;

        TestCase(String desc) {
            this.desc = desc;
        }

        TestCase withSetList(String setList) {
            this.setList = setList;
            return this;
        }

        TestCase thenExpect(MapEntry... expected) {
            this.expected = expected;
            return this;
        }

        TestCase thenExpectEmptySetList() {
            this.expected = new MapEntry[0];
            return this;
        }

        @Override
        public String toString() {
            return desc + " {" +
                    "setList='" + setList + '\'' +
                    ", expected=" + Arrays.toString(expected) +
                    '}';
        }
    }
}
