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

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingNumericText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@RunWith(DataProviderRunner.class)
public class ValueExtractUtilTest {

    @DataProvider
    public static Object[][] NUMERIC_TEXT_PROVIDER() {
        return new Object[][]{
                {"abc def", -1, ""},
                {"5 abc def ds", 1, "5.0"},
                {"5.0 abc def ds", 3, "5.0"},
                {"abc", 0, ""},
                {"1.0e-39", 0, "1.0E-39"},
                {"1.0e39", 0, "1.0E39"},
                {"1.0e39%", 0, "1.0E39"},
                {"1.0e-39 abcdef", 0, "1.0E-39"},
                {"5e", 0, "5"},
                {"5.e-39", 0, "5.0E-39"},
                {"5.", 0, "5.0"},
                {"-1.5", 0, "-1.5"},
                {"-53", 0, "-53"},
                {"-", -1, ""},
                {"-nan", -1, ""},
        };
    }

    @Test
    @UseDataProvider("NUMERIC_TEXT_PROVIDER")
    public void testExtractLeadingNumericText(String input, int digits, String expectedOutput) {
        // when
        String result = extractLeadingNumericText(input, digits);

        // then
        assertThat(result).isEqualTo(expectedOutput);
    }

    @Test
    public void testExtractLeadingInt() {
        assertThat(extractLeadingInt("1 abc")).isEqualTo(1);
        assertThat(extractLeadingInt("1 23")).isEqualTo(1);
    }

    @Test
    public void testExtractLeadingDouble() {
        assertThat(extractLeadingDouble("1.0 abc")).isCloseTo(1.0, offset(0.001));
        assertThat(extractLeadingDouble("2.5 23")).isCloseTo(2.5, offset(0.001));
    }
}
