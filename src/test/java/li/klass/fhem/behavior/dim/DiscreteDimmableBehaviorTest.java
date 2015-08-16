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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(DataProviderRunner.class)
public class DiscreteDimmableBehaviorTest {

    @DataProvider
    public static Object[][] discreteBehaviorProvider() {
        return new Object[][]{
                {new SetList().parse("dim10% dim20% dim30%"), ImmutableList.of("dim10%", "dim20%", "dim30%")},
                {new SetList().parse("dim1 dim20 dim30"), ImmutableList.of("dim1", "dim20", "dim30")},
                {new SetList().parse("dim30 dim10 dim80"), ImmutableList.of("dim10", "dim30", "dim80")},
        };
    }

    @Test
    @UseDataProvider("discreteBehaviorProvider")
    public void should_return_discrete_behavior(SetList setList, ImmutableList<String> expectedStates) {
        Optional<DiscreteDimmableBehavior> result = DiscreteDimmableBehavior.behaviorFor(setList);

        DiscreteDimmableBehavior behavior = result.get();
        assertThat(behavior.getFoundDimStates()).isEqualTo(expectedStates);
    }


    @DataProvider
    public static Object[][] nonDiscreteBehaviorProvider() {
        return new Object[][]{
                {new SetList().parse("dim10% dim_all")},
                {new SetList().parse("dim_all")},
                {new SetList().parse("dim:0,1,100")},
        };
    }

    @UseDataProvider("nonDiscreteBehaviorProvider")
    public void should_return_absent_if_the_set_list_does_not_contain_multiple_dim_states(SetList setList) {
        assertThat(DiscreteDimmableBehavior.behaviorFor(setList).isPresent()).isFalse();
    }

    @DataProvider
    public static Object[][] upperBoundProvider() {
        return new Object[][]{
                {new SetList().parse("dim10% dim20% dim30%"), 4},
                {new SetList().parse("dim10% dim20% dim30% dim40%"), 5},
                {new SetList().parse("dim10% dim20% dim30% dim40% dim50%"), 6}
        };
    }

    @Test
    @UseDataProvider("upperBoundProvider")
    public void should_calculate_upper_bound(SetList setList, int expectedUpperBound) {
        DiscreteDimmableBehavior behavior = DiscreteDimmableBehavior.behaviorFor(setList).get();

        assertThat(behavior.getDimUpperBound()).isEqualTo(expectedUpperBound);
    }

    @DataProvider
    public static Object[][] positionProvider() {
        return new Object[][]{
                {new SetList().parse("dim10% dim20% dim30%"), "dim20%", 2},
                {new SetList().parse("dim10% dim20% dim30% dim40%"), "dim30%", 3},
                {new SetList().parse("dim10% dim20% dim30% dim40% dim50%"), "dim50%", 5},
                {new SetList().parse("dim20% dim10%"), "dim10%", 1},
                {new SetList().parse("dim20% dim10%"), "dim20%", 2}
        };
    }

    @Test
    @UseDataProvider("positionProvider")
    public void should_calculate_position(SetList setList, String state, int position) {
        DiscreteDimmableBehavior behavior = DiscreteDimmableBehavior.behaviorFor(setList).get();

        assertThat(behavior.getDimStateForPosition(mock(FhemDevice.class), position)).isEqualTo(state);
        assertThat(behavior.getPositionForDimState(state)).isEqualTo(position);
    }
}