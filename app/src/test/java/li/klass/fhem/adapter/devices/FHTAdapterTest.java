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

package li.klass.fhem.adapter.devices;

import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.DateService;
import li.klass.fhem.testutil.MockitoTestRule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.joda.time.DateTimeConstants.JANUARY;
import static org.mockito.BDDMockito.given;

public class FHTAdapterTest extends RobolectricBaseTestCase {

    @Mock
    private DateService dateService;

    @Mock
    private TimePicker timePicker;

    @InjectMocks
    private FHTAdapter fhtAdapter;

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Test
    public void should_calculate_holidayShort_holiday1_time() {
        verifyTimePickerHoliday1Time(0, 0, 0);
        verifyTimePickerHoliday1Time(24, 0, 144);
        verifyTimePickerHoliday1Time(23, 0, 138);
        verifyTimePickerHoliday1Time(12, 10, 73);
        verifyTimePickerHoliday1Time(21, 40, 130);
    }

    @Test
    public void should_recognize_date_as_tomorrow_or_today_based_on_the_selected_time() {
        DateTime baseline = new DateTime(2014, 5, 10, 12, 0);
        assertThat(fhtAdapter.holidayShortIsTomorrow(baseline.minusMinutes(1), baseline)).isTrue();
        assertThat(fhtAdapter.holidayShortIsTomorrow(baseline.plusMinutes(1), baseline)).isFalse();
    }

    private void verifyTimePickerHoliday1Time(int currentHour, int currentMinute, int expectedHoliday1Value) {
        assertThat(fhtAdapter.calculateHolidayShortHoliday1ValueFrom(currentHour, currentMinute)).isEqualTo(expectedHoliday1Value);
    }

    @Test
    public void should_calculate_the_holiday_short_switch_time() {
        // given
        given(timePicker.getCurrentHour()).willReturn(13);
        given(timePicker.getCurrentMinute()).willReturn(50);
        given(dateService.now()).willReturn(new DateTime(2014, JANUARY, 1, 12, 0));

        // when
        DateTime time = fhtAdapter.holiday1SwitchTimeFor(timePicker);

        // then
        assertThat(time).isEqualTo(new DateTime(2014, JANUARY, 1, 13, 50));
    }

    @Test
    public void should_round_up_minutes_in_holiday_short_switch_time() {
        // given
        given(timePicker.getCurrentHour()).willReturn(13);
        given(timePicker.getCurrentMinute()).willReturn(46);
        given(dateService.now()).willReturn(new DateTime(2014, JANUARY, 1, 12, 0));

        // when
        DateTime time = fhtAdapter.holiday1SwitchTimeFor(timePicker);

        // then
        assertThat(time).isEqualTo(new DateTime(2014, JANUARY, 1, 13, 50));
    }

    @Test
    public void should_floor_minutes_in_holiday_short_switch_time() {
        // given
        given(timePicker.getCurrentHour()).willReturn(13);
        given(timePicker.getCurrentMinute()).willReturn(44);
        given(dateService.now()).willReturn(new DateTime(2014, JANUARY, 1, 12, 0));

        // when
        DateTime time = fhtAdapter.holiday1SwitchTimeFor(timePicker);

        // then
        assertThat(time).isEqualTo(new DateTime(2014, JANUARY, 1, 13, 40));
    }

    @Test
    public void should_handle_60_minutes_as_1_hour_when_creating_the_DateTime_object() {
        // given
        given(timePicker.getCurrentHour()).willReturn(13);
        given(timePicker.getCurrentMinute()).willReturn(60);
        given(dateService.now()).willReturn(new DateTime(2014, JANUARY, 1, 12, 0));

        // when
        DateTime time = fhtAdapter.holiday1SwitchTimeFor(timePicker);

        // then
        assertThat(time).isEqualTo(new DateTime(2014, JANUARY, 1, 14, 0));
    }

    @Test
    public void should_handle_24_hours_when_creating_the_DateTime_object() {
        // given
        given(timePicker.getCurrentHour()).willReturn(24);
        given(timePicker.getCurrentMinute()).willReturn(30);
        given(dateService.now()).willReturn(new DateTime(2014, JANUARY, 1, 12, 0));

        // when
        DateTime time = fhtAdapter.holiday1SwitchTimeFor(timePicker);

        // then
        assertThat(time).isEqualTo(new DateTime(2014, JANUARY, 2, 0, 30));
    }
}
