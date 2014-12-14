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

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SetListTest {
    private SetList setList;
    
    @Before
    public void setUp() {
        setList = new SetList();
    }

    @Test
    public void testStateValuesSetList() {
        setList.parse("on off");
        assertThat(setList.getEntries()).hasSize(2);
        assertThat(setList.getEntries().get("on")).isEqualTo(SetListEmptyValue.INSTANCE);
        assertThat(setList.getEntries().get("off")).isEqualTo(SetListEmptyValue.INSTANCE);

        assertThat(setList.toString()).isEqualTo("off on");
    }

    @Test
    public void testTypedValueSetList() {
        setList.parse("state:time");
        assertThat(setList.getEntries()).hasSize(1);

        SetListGroupValue state = (SetListGroupValue) setList.getEntries().get("state");
        assertThat(state).isEqualTo(new SetListGroupValue("time"));
        assertThat(state.asText()).isEqualTo("time");
    }

    @Test
    public void testGroupValueSetList() {
        setList.parse("level:1,2,3 state:on,off");
        assertThat(setList.getEntries()).hasSize(2);
        assertThat(setList.getEntries().get("level")).isEqualTo(new SetListGroupValue("1", "2", "3"));
        assertThat(setList.getEntries().get("state")).isEqualTo(new SetListGroupValue("on", "off"));
    }

    @Test
    public void testSliderValueSetList() {
        setList.parse("state:slider,1,2,3 dim:slider,0,5,100");
        assertThat(setList.getEntries()).hasSize(2);
        assertThat(setList.getEntries().get("state")).isEqualTo(new SetListSliderValue(1, 2, 3));
        assertThat(setList.getEntries().get("dim")).isEqualTo(new SetListSliderValue(0, 5, 100));
    }

    @Test
    public void testColonWithoutValuesLeadsToGroup() {
        setList.parse("state:");
        assertThat(setList.getEntries().get("state")).isInstanceOf(SetListGroupValue.class);
    }

    @Test
    public void testTrailingWhitespace() {
        setList.parse("on off ");
        assertThat(setList.getEntries()).hasSize(2);
        assertThat(setList.getEntries()).containsKey("on");
        assertThat(setList.getEntries()).containsKey("off");
    }

    @Test
    public void testLeadingWhitespace() {
        setList.parse(" on off");
        assertThat(setList.getEntries()).hasSize(2);
        assertThat(setList.getEntries()).containsKey("on");
        assertThat(setList.getEntries()).containsKey("off");
    }

    @Test
    public void testEmptySetList() {
        setList.parse("");
        assertThat(setList.size()).isEqualTo(0);
    }
}
