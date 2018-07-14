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

package li.klass.fhem.behavior.dim

import com.google.common.collect.ImmutableList
import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetList
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(DataProviderRunner::class)
class DiscreteDimmableBehaviorTest {

    @Test
    @UseDataProvider("discreteBehaviorProvider")
    fun should_return_discrete_behavior(testCase: DiscreteBehaviorProviderTestCase) {
        val result = DiscreteDimmableBehavior.behaviorFor(testCase.setList)
        assertThat(result!!.foundDimStates).isEqualTo(testCase.expectedStates)
    }

    @UseDataProvider("nonDiscreteBehaviorProvider")
    fun should_return_absent_if_the_set_list_does_not_contain_multiple_dim_states(testCase: NonDiscreteBehaviorProviderTestCase) {
        assertThat(DiscreteDimmableBehavior.behaviorFor(testCase.setList)).isNull()
    }

    @Test
    @UseDataProvider("upperBoundProvider")
    fun should_calculate_upper_bound(testCase: UpperBoundProviderTestCase) {
        val behavior = DiscreteDimmableBehavior.behaviorFor(testCase.setList)
        assertThat(behavior!!.getDimUpperBound()).isEqualTo(testCase.expectedUpperBound.toDouble())
    }

    @Test
    @UseDataProvider("positionProvider")
    fun should_calculate_position(testCase: PositionProviderTestCase) {
        val behavior = DiscreteDimmableBehavior.behaviorFor(testCase.setList)

        assertThat(behavior!!.getDimStateForPosition(mock(FhemDevice::class.java), testCase.position.toDouble())).isEqualTo(testCase.state)
        assertThat(behavior.getPositionForDimState(testCase.state)).isEqualTo(testCase.position.toDouble())
    }

    companion object {
        @JvmStatic
        @DataProvider
        fun discreteBehaviorProvider() = listOf(
                DiscreteBehaviorProviderTestCase(SetList.parse("dim10% dim20% dim30%"), ImmutableList.of("dim10%", "dim20%", "dim30%")),
                DiscreteBehaviorProviderTestCase(SetList.parse("dim1 dim20 dim30"), ImmutableList.of("dim1", "dim20", "dim30")),
                DiscreteBehaviorProviderTestCase(SetList.parse("dim30 dim10 dim80"), ImmutableList.of("dim10", "dim30", "dim80"))
        )

        @JvmStatic
        @DataProvider
        fun nonDiscreteBehaviorProvider() = listOf(
                NonDiscreteBehaviorProviderTestCase(SetList.parse("dim10% dim_all")),
                NonDiscreteBehaviorProviderTestCase(SetList.parse("dim_all")),
                NonDiscreteBehaviorProviderTestCase(SetList.parse("dim:0,1,100"))
        )

        @JvmStatic
        @DataProvider
        fun upperBoundProvider() = listOf(
                UpperBoundProviderTestCase(SetList.parse("dim10% dim20% dim30%"), 3),
                UpperBoundProviderTestCase(SetList.parse("dim10% dim20% dim30% dim40%"), 4),
                UpperBoundProviderTestCase(SetList.parse("dim10% dim20% dim30% dim40% dim50%"), 5)
        )

        @JvmStatic
        @DataProvider
        fun positionProvider() = listOf(
                PositionProviderTestCase(SetList.parse("dim10% dim20% dim30%"), "dim20%", 2),
                PositionProviderTestCase(SetList.parse("dim10% dim20% dim30% dim40%"), "dim30%", 3),
                PositionProviderTestCase(SetList.parse("dim10% dim20% dim30% dim40% dim50%"), "on", 5),
                PositionProviderTestCase(SetList.parse("dim20% dim10%"), "dim10%", 1),
                PositionProviderTestCase(SetList.parse("dim20% dim10%"), "on", 2)
        )
    }

    data class PositionProviderTestCase(val setList: SetList, val state: String, val position: Int)
    data class UpperBoundProviderTestCase(val setList: SetList, val expectedUpperBound: Int)
    data class NonDiscreteBehaviorProviderTestCase(val setList: SetList)
    data class DiscreteBehaviorProviderTestCase(val setList: SetList, val expectedStates: ImmutableList<String>)
}