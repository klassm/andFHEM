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

import static li.klass.fhem.util.ColorUtil.extractBlue;
import static li.klass.fhem.util.ColorUtil.extractGreen;
import static li.klass.fhem.util.ColorUtil.extractRed;
import static li.klass.fhem.util.ColorUtil.rgbToXY;
import static li.klass.fhem.util.ColorUtil.xyToRgb;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class ColorUtilTest {
    @Test
    public void testXY_RGB_Conversion() {
        assertBidirectionalConvert(0xFFFFFF);
        assertBidirectionalConvert(0xAAAAAA);
        assertBidirectionalConvert(0x537645);
    }

    private void assertBidirectionalConvert(int color) {
        int red = extractRed(color);
        int green = extractGreen(color);
        int blue = extractBlue(color);

        ColorUtil.XYColor xyColor = rgbToXY(color);
        int rgb = xyToRgb(xyColor.xy, xyColor.brightness);

        assertThat(extractRed(rgb)).isBetween(red - 2, red + 2);
        assertThat(extractBlue(rgb)).isBetween(blue - 2, blue + 2);
        assertThat(extractGreen(rgb)).isBetween(green - 2, green + 2);
    }

    @DataProvider
    public static Object[][] rgbProvider() {
        return new Object[][]{
                {"FFFFFF", 0xFFFFFF},
                {"ABCEFF", 0xABCEFF},
                {"AbCEFf", 0xABCEFF},
                {"0xAbCEFf", 0xABCEFF},
                {"0x00AbCEFf", 0xABCEFF},
        };
    }

    @UseDataProvider("rgbProvider")
    @Test
    public void should_convert_rgb(String in, int expected) {
        assertThat(ColorUtil.fromRgbString(in)).isEqualTo(expected);
    }
}
