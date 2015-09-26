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

package li.klass.fhem.util;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import static li.klass.fhem.util.DimConversionUtil.toDimState;
import static li.klass.fhem.util.DimConversionUtil.toSeekbarProgress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@RunWith(DataProviderRunner.class)
public class DimConversionUtilTest {
    @DataProvider
    public static Object[][] conversionProvider() {
        return new Object[][]{
                // defaults (zero lower bound, step 1)
                {new TestCase().withLowerBound(0f).withStep(1f).withProgress(50f).withSeekbarProgress(50)},
                {new TestCase().withLowerBound(0f).withStep(1f).withProgress(100f).withSeekbarProgress(100)},
                {new TestCase().withLowerBound(0f).withStep(1f).withProgress(0f).withSeekbarProgress(0)},
                {new TestCase().withLowerBound(5f).withStep(1f).withProgress(5f).withSeekbarProgress(0)},
                {new TestCase().withLowerBound(5f).withStep(1f).withProgress(24f).withSeekbarProgress(19)},

                // shifted lower bound
                {new TestCase().withLowerBound(20f).withStep(1f).withProgress(50f).withSeekbarProgress(30)},

                // step different from 1
                {new TestCase().withLowerBound(0f).withStep(2f).withProgress(20f).withSeekbarProgress(10)},
                {new TestCase().withLowerBound(0f).withStep(3f).withProgress(21f).withSeekbarProgress(7)},

                // shifted lower bound and step different from 1
                {new TestCase().withLowerBound(10f).withStep(2f).withProgress(20f).withSeekbarProgress(5)},

                // floating point states
                {new TestCase().withLowerBound(0f).withStep(0.5f).withProgress(0f).withSeekbarProgress(0)},
                {new TestCase().withLowerBound(0f).withStep(0.5f).withProgress(1f).withSeekbarProgress(2)},
                {new TestCase().withLowerBound(0f).withStep(0.5f).withProgress(4f).withSeekbarProgress(8)},
        };
    }

    @Test
    @UseDataProvider("conversionProvider")
    public void should_convert_to_seekbar_progress(TestCase testCase) {
        assertThat(toSeekbarProgress(testCase.progress, testCase.lowerBound, testCase.step)).isEqualTo(testCase.seekbarProgress);
    }

    @Test
    @UseDataProvider("conversionProvider")
    public void should_convert_from_seekbar_progress(TestCase testCase) {
        assertThat(toDimState(testCase.seekbarProgress, testCase.lowerBound, testCase.step)).isCloseTo(testCase.progress, offset(0.001f));
    }

    static class TestCase {
        public float lowerBound;
        public float step;
        public float progress;
        public int seekbarProgress;

        public TestCase withLowerBound(float lowerBound) {
            this.lowerBound = lowerBound;
            return this;
        }

        public TestCase withStep(float step) {
            this.step = step;
            return this;
        }

        public TestCase withProgress(float progress) {
            this.progress = progress;
            return this;
        }

        public TestCase withSeekbarProgress(int seekbarProgress) {
            this.seekbarProgress = seekbarProgress;
            return this;
        }

        @Override
        public String toString() {
            return "TestCase{" +
                    "lowerBound=" + lowerBound +
                    ", step=" + step +
                    ", progress=" + progress +
                    ", seekbarProgress=" + seekbarProgress +
                    '}';
        }
    }
}