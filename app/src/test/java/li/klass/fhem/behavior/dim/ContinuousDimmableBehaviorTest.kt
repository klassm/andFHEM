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

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetList
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.util.*

@RunWith(DataProviderRunner::class)
class ContinuousDimmableBehaviorTest {

    @Test
    @UseDataProvider("continuousProvider")
    fun should_extract_dim_attribute(testCase: ContinuousProviderTestCase) {
        val behavior = ContinuousDimmableBehavior.behaviorFor(testCase.setList)

        assertThat(behavior!!.slider).isEqualToComparingFieldByField(testCase.expectedSlider)
        assertThat(behavior.getDimLowerBound()).isEqualTo(testCase.expectedSlider.start)
        assertThat(behavior.getDimStep()).isEqualTo(testCase.expectedSlider.step)
        assertThat(behavior.getDimUpperBound()).isEqualTo(testCase.expectedSlider.stop)
        assertThat(behavior.getStateName()).isEqualTo(testCase.expectedSlider.key)
    }

    @Test
    @UseDataProvider("stateProvider")
    fun should_calculate_dim_state_for_position_and_position_for_dim_state(testCase: StateTestCase) {
        val behavior = ContinuousDimmableBehavior.behaviorFor(SetList.parse("position:slider,0,5,100"))

        assertThat(behavior!!.getPositionForDimState(testCase.text)).isEqualTo(testCase.position.toDouble())
        assertThat(behavior.getDimStateForPosition(mock(FhemDevice::class.java), testCase.position.toDouble()))
                .isEqualTo(testCase.state)
    }

    @Test
    @UseDataProvider("prefixDimProvider")
    fun should_handle_states_with_prefix(testCase: PrefixTestCase) {
        val behavior = ContinuousDimmableBehavior.behaviorFor(SetList.parse("position:slider,0,5,100"))
        val device = mock(FhemDevice::class.java)
        val xmlListDevice = XmlListDevice("BLA", HashMap(), HashMap(), HashMap(), HashMap())
        xmlListDevice.setState("state", testCase.state)
        given(device.xmlListDevice).willReturn(xmlListDevice)

        val position = behavior!!.getCurrentDimPosition(device)

        assertThat(position).isCloseTo(testCase.expectedPosition.toDouble(), Offset.offset(0.1))
    }

    companion object {
        @JvmStatic
        @DataProvider
        fun continuousProvider() = listOf(
                ContinuousProviderTestCase(SetList.parse("dim:slider,0,5,100"), SliderSetListEntry("dim", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("state:slider,0,5,100"), SliderSetListEntry("state", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("pct:slider,0,5,100"), SliderSetListEntry("pct", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("value:slider,0,5,100"), SliderSetListEntry("value", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("position:slider,0,5,100"), SliderSetListEntry("position", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("level:slider,0,5,100"), SliderSetListEntry("level", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("state:slider,0,5,100 dim:slider,1,2,100"), SliderSetListEntry("state", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("dim:slider,0,5,100 level:slider,1,2,100"), SliderSetListEntry("dim", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("level:slider,0,5,100 pct:slider,1,2,100"), SliderSetListEntry("level", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("pct:slider,0,5,100 position:slider,1,2,100"), SliderSetListEntry("pct", 0f, 5f, 100f)),
                ContinuousProviderTestCase(SetList.parse("position:slider,0,5,100 value:slider,1,2,100"), SliderSetListEntry("position", 0f, 5f, 100f)))

        @JvmStatic
        @DataProvider
        fun stateProvider() = listOf(
                StateTestCase(1, "1", "1"),
                StateTestCase(5, "5", "5"),
                StateTestCase(5, "5a", "5"),
                StateTestCase(100, "100", "100")
        )

        @JvmStatic
        @DataProvider
        fun prefixDimProvider() = listOf(
                PrefixTestCase("dim 30", 30f),
                PrefixTestCase("position 30", 30f),
                PrefixTestCase("position 30%", 30f)
        )
    }

    data class PrefixTestCase(val state: String, val expectedPosition: Float)
    data class StateTestCase(val position: Int, val text: String, val state: String)
    data class ContinuousProviderTestCase(val setList: SetList, val expectedSlider: SliderSetListEntry)
}