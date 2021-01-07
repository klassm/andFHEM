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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.fht

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import li.klass.fhem.testutil.MockRule
import li.klass.fhem.util.DateTimeProvider
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.JANUARY
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class HolidayShortCalculatorTest {

    @MockK
    private lateinit var dateTimeProvider: DateTimeProvider

    @InjectMockKs
    private lateinit var holidayShortCalculator: HolidayShortCalculator

    @Rule
    @JvmField
    var mockitoRule = MockRule()

    @Test
    @UseDataProvider("holiday1TimeProvider")
    fun should_calculate_holidayShort_holiday1_time(testCase: CalculationTestCase) {
        assertThat(holidayShortCalculator.calculateHoliday1ValueFrom(testCase.currentHour, testCase.currentMinute))
                .isEqualTo(testCase.expectedHoliday1Value)
    }

    @Test
    fun should_recognize_date_as_tomorrow_or_today_based_on_the_selected_time() {
        val baseline = DateTime(2014, 5, 10, 12, 0)
        assertThat(holidayShortCalculator.holidayShortIsTomorrow(baseline.minusMinutes(1), baseline)).isTrue()
        assertThat(holidayShortCalculator.holidayShortIsTomorrow(baseline.plusMinutes(1), baseline)).isFalse()
    }

    @Test
    @UseDataProvider("switchTimeCalculationProvider")
    fun should_calculate_switch_time(testCase: SwitchTimeTestCase) {
        every { dateTimeProvider.now() } returns testCase.now

        val time = holidayShortCalculator.holiday1SwitchTimeFor(testCase.switchTimeHour, testCase.switchTimeMinute)

        assertThat(time).isEqualTo(testCase.expectedTime)
    }

    @Test
    fun should_handle_24_hours_when_creating_the_DateTime_object() {
        every { dateTimeProvider.now() } returns DateTime(2014, JANUARY, 1, 12, 0)

        val time = holidayShortCalculator.holiday1SwitchTimeFor(24, 30)

        assertThat(time).isEqualTo(DateTime(2014, JANUARY, 2, 0, 30))
    }

    data class CalculationTestCase(
            val currentHour: Int,
            val currentMinute: Int,
            val expectedHoliday1Value: Int
    )

    data class SwitchTimeTestCase(val now: DateTime, val switchTimeHour: Int, val switchTimeMinute: Int, val expectedTime: DateTime)

    companion object {
        @DataProvider
        @JvmStatic
        fun holiday1TimeProvider() =
                listOf(CalculationTestCase(currentHour = 0, currentMinute = 0, expectedHoliday1Value = 0),
                        CalculationTestCase(currentHour = 24, currentMinute = 0, expectedHoliday1Value = 144),
                        CalculationTestCase(currentHour = 23, currentMinute = 0, expectedHoliday1Value = 138),
                        CalculationTestCase(currentHour = 12, currentMinute = 10, expectedHoliday1Value = 73),
                        CalculationTestCase(currentHour = 21, currentMinute = 40, expectedHoliday1Value = 130)
                )

        @DataProvider
        @JvmStatic
        fun switchTimeCalculationProvider() =
                listOf(
                        SwitchTimeTestCase(
                                now = DateTime(2014, JANUARY, 1, 12, 0),
                                switchTimeHour = 13,
                                switchTimeMinute = 50,
                                expectedTime = DateTime(2014, JANUARY, 1, 13, 50)
                        ),
                        SwitchTimeTestCase(
                                now = DateTime(2014, JANUARY, 1, 12, 0),
                                switchTimeHour = 13,
                                switchTimeMinute = 46,
                                expectedTime = DateTime(2014, JANUARY, 1, 13, 50)),
                        SwitchTimeTestCase(now = DateTime(2014, JANUARY, 1, 12, 0),
                                switchTimeHour = 13,
                                switchTimeMinute = 44,
                                expectedTime = DateTime(2014, JANUARY, 1, 13, 40)),
                        SwitchTimeTestCase(now = DateTime(2014, JANUARY, 1, 12, 0),
                                switchTimeHour = 13,
                                switchTimeMinute = 60,
                                expectedTime = DateTime(2014, JANUARY, 1, 14, 0))
                )
    }
}