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

package li.klass.fhem.behavior.dim;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListSliderValue;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class ContinuousDimmableBehaviorTest {
    @DataProvider
    public static Object[][] continuousProvider() {
        SetListSliderValue slider = new SetListSliderValue(0, 5, 100);
        return new Object[][]{
                {new SetList().parse("dim:slider,0,5,100"), slider, "dim"},
                {new SetList().parse("state:slider,0,5,100"), slider, "state"},
                {new SetList().parse("pct:slider,0,5,100"), slider, "pct"},
                {new SetList().parse("value:slider,0,5,100"), slider, "value"},
                {new SetList().parse("position:slider,0,5,100"), slider, "position"},
                {new SetList().parse("level:slider,0,5,100"), slider, "level"},
                {new SetList().parse("state:slider,0,5,100 dim:slider,1,2,100"), slider, "state"},
                {new SetList().parse("dim:slider,0,5,100 level:slider,1,2,100"), slider, "dim"},
                {new SetList().parse("level:slider,0,5,100 pct:slider,1,2,100"), slider, "level"},
                {new SetList().parse("pct:slider,0,5,100 position:slider,1,2,100"), slider, "pct"},
                {new SetList().parse("position:slider,0,5,100 value:slider,1,2,100"), slider, "position"},
        };
    }

    @Test
    @UseDataProvider("continuousProvider")
    public void should_extract_dim_attribute(SetList setList, SetListSliderValue expectedSlider, String expectedAttribute) {
        ContinuousDimmableBehavior behavior = ContinuousDimmableBehavior.behaviorFor(setList).get();

        assertThat(behavior.getSlider()).isEqualToComparingFieldByField(expectedSlider);
        assertThat(behavior.getDimLowerBound()).isEqualTo(expectedSlider.getStart());
        assertThat(behavior.getDimStep()).isEqualTo(expectedSlider.getStep());
        assertThat(behavior.getDimUpperBound()).isEqualTo(expectedSlider.getStop());
        assertThat(behavior.getStateName()).isEqualTo(expectedAttribute);
    }

    @DataProvider
    public static Object[][] stateProvider() {
        return new Object[][]{
                {1, "1", "1"},
                {5, "5", "5"},
                {5, "5a", "5"},
                {100, "100", "100"}
        };
    }

    @Test
    @UseDataProvider("stateProvider")
    public void should_calculate_dim_state_for_position_and_position_for_dim_state(int position, String text, String state) {
        ContinuousDimmableBehavior behavior = ContinuousDimmableBehavior.behaviorFor(new SetList().parse("position:slider,0,5,100")).get();

        assertThat(behavior.getPositionForDimState(text)).isEqualTo(position);
        assertThat(behavior.getDimStateForPosition(position)).isEqualTo(state);
    }
}