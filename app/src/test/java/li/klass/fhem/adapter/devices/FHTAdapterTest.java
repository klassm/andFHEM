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

import org.joda.time.DateTime;
import org.junit.Test;

import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;

import static org.fest.assertions.api.Assertions.assertThat;

public class FHTAdapterTest extends RobolectricBaseTestCase {

    private FHTAdapter fhtAdapter = new FHTAdapter();

    @Test
    public void testHolidayShortTimeToFHEMHoliday1Time() {
        verifyTimePickerHoliday1Time(0, 0, 0);
        verifyTimePickerHoliday1Time(24, 0, 144);
        verifyTimePickerHoliday1Time(23, 0, 138);
        verifyTimePickerHoliday1Time(12, 10, 73);
        verifyTimePickerHoliday1Time(21, 40, 130);
    }

    @Test
    public void testHolidayShortIsTomorrow() {
        DateTime baseline = new DateTime(2014, 5, 10, 12, 0);
        assertThat(fhtAdapter.holidayShortIsTomorrow(baseline.minusMinutes(1), baseline)).isTrue();
        assertThat(fhtAdapter.holidayShortIsTomorrow(baseline.plusMinutes(1), baseline)).isFalse();
    }

    private void verifyTimePickerHoliday1Time(int currentHour, int currentMinute, int expectedHoliday1Value) {
        assertThat(fhtAdapter.calculateHolidayShortHoliday1ValueFrom(currentHour, currentMinute)).isEqualTo(expectedHoliday1Value);
    }
}
