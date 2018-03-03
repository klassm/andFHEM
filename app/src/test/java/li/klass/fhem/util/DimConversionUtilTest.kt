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

package li.klass.fhem.util

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class DimConversionUtilTest {

    @Test
    @UseDataProvider("conversionProvider")
    fun should_convert_to_seekbar_progress(testCase: TestCase) {
        assertThat(DimConversionUtil.toSeekbarProgress(testCase.progress, testCase.lowerBound, testCase.step)).isEqualTo(testCase.seekbarProgress)
    }

    @Test
    @UseDataProvider("conversionProvider")
    fun should_convert_from_seekbar_progress(testCase: TestCase) {
        assertThat(DimConversionUtil.toDimState(testCase.seekbarProgress, testCase.lowerBound, testCase.step)).isCloseTo(testCase.progress, offset(0.001))
    }

    data class TestCase(val lowerBound: Double, val step: Double, val progress: Double, val seekbarProgress: Int)

    companion object {
        @DataProvider
        @JvmStatic
        fun conversionProvider() = listOf(
                // defaults (zero lower bound, step 1)
                TestCase(lowerBound = 0.0, step = 1.0, progress = 50.0, seekbarProgress = 50),
                TestCase(lowerBound = 0.0, step = 1.0, progress = 100.0, seekbarProgress = 100),
                TestCase(lowerBound = 0.0, step = 1.0, progress = 0.0, seekbarProgress = 0),
                TestCase(lowerBound = 5.0, step = 1.0, progress = 5.0, seekbarProgress = 0),
                TestCase(lowerBound = 5.0, step = 1.0, progress = 24.0, seekbarProgress = 19),

                // shifted lower bound
                TestCase(lowerBound = 20.0, step = 1.0, progress = 50.0, seekbarProgress = 30),

                // step different from 1
                TestCase(lowerBound = 0.0, step = 2.0, progress = 20.0, seekbarProgress = 10),
                TestCase(lowerBound = 0.0, step = 3.0, progress = 21.0, seekbarProgress = 7),

                // shifted lower bound and step different from 1
                TestCase(lowerBound = 10.0, step = 2.0, progress = 20.0, seekbarProgress = 5),

                // floating point states
                TestCase(lowerBound = 0.0, step = 0.5, progress = 0.0, seekbarProgress = 0),
                TestCase(lowerBound = 0.0, step = 0.5, progress = 1.0, seekbarProgress = 2),
                TestCase(lowerBound = 0.0, step = 0.5, progress = 4.0, seekbarProgress = 8),
                TestCase(lowerBound = 1.01, step = 0.01, progress = 1.09, seekbarProgress = 8),
                TestCase(lowerBound = 1.01, step = 0.01, progress = 24.01, seekbarProgress = 2300),
                TestCase(lowerBound = 1.01, step = 0.01, progress = 24.02, seekbarProgress = 2301)
        )
    }
}