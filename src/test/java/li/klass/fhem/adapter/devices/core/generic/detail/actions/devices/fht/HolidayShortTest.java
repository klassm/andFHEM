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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.fht;

import android.widget.TimePicker;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.service.DateService;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.util.ApplicationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTimeConstants.JANUARY;
import static org.mockito.BDDMockito.given;

@RunWith(DataProviderRunner.class)
public class HolidayShortTest {

    @Mock
    private DateService dateService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private TimePicker timePicker;

    @InjectMocks
    private HolidayShort holidayShort;

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @DataProvider
    public static Object[][] holiday1TimeProvider() {
        return new Object[][]{
                {new CalculationTestCase().withCurrentHour(0).withCurrentMinute(0).withExpectedHoliday1Value(0)},
                {new CalculationTestCase().withCurrentHour(24).withCurrentMinute(0).withExpectedHoliday1Value(144)},
                {new CalculationTestCase().withCurrentHour(23).withCurrentMinute(0).withExpectedHoliday1Value(138)},
                {new CalculationTestCase().withCurrentHour(12).withCurrentMinute(10).withExpectedHoliday1Value(73)},
                {new CalculationTestCase().withCurrentHour(21).withCurrentMinute(40).withExpectedHoliday1Value(130)},
        };
    }

    @Test
    @UseDataProvider("holiday1TimeProvider")
    public void should_calculate_holidayShort_holiday1_time(CalculationTestCase testCase) {
        assertThat(holidayShort.calculateHolidayShortHoliday1ValueFrom(testCase.currentHour, testCase.currentMinute))
                .isEqualTo(testCase.expectedHoliday1Value);
    }

    @Test
    public void should_recognize_date_as_tomorrow_or_today_based_on_the_selected_time() {
        DateTime baseline = new DateTime(2014, 5, 10, 12, 0);
        assertThat(holidayShort.holidayShortIsTomorrow(baseline.minusMinutes(1), baseline)).isTrue();
        assertThat(holidayShort.holidayShortIsTomorrow(baseline.plusMinutes(1), baseline)).isFalse();
    }

    @DataProvider
    public static Object[][] switchTimeCalculationProvider() {
        return new Object[][]{
                {new SwitchTimeTestCase()
                        .withNow(new DateTime(2014, JANUARY, 1, 12, 0))
                        .withSwitchTimeHour(13)
                        .withSwitchTimeMinute(50)
                        .withExpectedTime(new DateTime(2014, JANUARY, 1, 13, 50))},
                {new SwitchTimeTestCase()
                        .withNow(new DateTime(2014, JANUARY, 1, 12, 0))
                        .withSwitchTimeHour(13)
                        .withSwitchTimeMinute(46)
                        .withExpectedTime(new DateTime(2014, JANUARY, 1, 13, 50))},
                {new SwitchTimeTestCase()
                        .withNow(new DateTime(2014, JANUARY, 1, 12, 0))
                        .withSwitchTimeHour(13)
                        .withSwitchTimeMinute(44)
                        .withExpectedTime(new DateTime(2014, JANUARY, 1, 13, 40))},
                {new SwitchTimeTestCase()
                        .withNow(new DateTime(2014, JANUARY, 1, 12, 0))
                        .withSwitchTimeHour(13)
                        .withSwitchTimeMinute(60)
                        .withExpectedTime(new DateTime(2014, JANUARY, 1, 14, 0))},
        };
    }


    @Test
    @UseDataProvider("switchTimeCalculationProvider")
    public void should_calculate_switch_time(SwitchTimeTestCase testCase) {
        // given
        given(dateService.now()).willReturn(testCase.now);

        // when
        DateTime time = holidayShort.holiday1SwitchTimeFor(testCase.switchTimeHour, testCase.switchTimeMinute);

        // then
        assertThat(time).isEqualTo(testCase.expectedTime);
    }

    @Test
    public void should_handle_24_hours_when_creating_the_DateTime_object() {
        // given
        given(timePicker.getCurrentHour()).willReturn(24);
        given(timePicker.getCurrentMinute()).willReturn(30);
        given(dateService.now()).willReturn(new DateTime(2014, JANUARY, 1, 12, 0));

        // when
        DateTime time = holidayShort.holiday1SwitchTimeFor(24, 30);

        // then
        assertThat(time).isEqualTo(new DateTime(2014, JANUARY, 2, 0, 30));
    }

    private static class CalculationTestCase {
        public int currentHour;
        public int currentMinute;
        public int expectedHoliday1Value;

        public CalculationTestCase withCurrentHour(int currentHour) {
            this.currentHour = currentHour;
            return this;
        }

        public CalculationTestCase withCurrentMinute(int currentMinute) {
            this.currentMinute = currentMinute;
            return this;
        }

        public CalculationTestCase withExpectedHoliday1Value(int expectedHoliday1Value) {
            this.expectedHoliday1Value = expectedHoliday1Value;
            return this;
        }

        @Override
        public String toString() {
            return "CalculationTestCase{" +
                    "currentHour=" + currentHour +
                    ", currentMinute=" + currentMinute +
                    ", expectedHoliday1Value=" + expectedHoliday1Value +
                    '}';
        }
    }

    private static class SwitchTimeTestCase {
        public DateTime now;
        public int switchTimeHour;
        public int switchTimeMinute;
        public DateTime expectedTime;

        public SwitchTimeTestCase withNow(DateTime now) {
            this.now = now;
            return this;
        }

        public SwitchTimeTestCase withSwitchTimeHour(int switchTimeHour) {
            this.switchTimeHour = switchTimeHour;
            return this;
        }

        public SwitchTimeTestCase withSwitchTimeMinute(int switchTimeMinute) {
            this.switchTimeMinute = switchTimeMinute;
            return this;
        }

        public SwitchTimeTestCase withExpectedTime(DateTime expectedTime) {
            this.expectedTime = expectedTime;
            return this;
        }

        @Override
        public String toString() {
            return "SwitchTimeTestCase{" +
                    "now=" + now +
                    ", switchTimeHour=" + switchTimeHour +
                    ", switchTimeMinute=" + switchTimeMinute +
                    ", expectedTime=" + expectedTime +
                    '}';
        }
    }
}