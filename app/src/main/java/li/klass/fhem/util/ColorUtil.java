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

import java.util.Locale;

import static java.lang.Math.pow;

public class ColorUtil {
    public static class XYColor {
        public final double[] xy;
        public final int brightness;

        public XYColor(double[] xy, int brightness) {
            this.xy = xy;
            this.brightness = brightness;
        }
    }

    /**
     * Converts a given RGB color to the xy system. See
     * <a href="https://github.com/PhilipsHue/PhilipsHueSDK-iOS-OSX/blob/master/ApplicationDesignNotes/RGB%20to%20xy%20Color%20conversion.md"
     * PhilipsHueSDK</a> for the algorithm.
     *
     * @param rgb given RGB value.
     * @return array of doubles containing the x and y values. xy is also called the
     * "color point" (see <a href="http://www.everyhue.com/vanilla/discussion/94/rgb-to-xy-or-hue-sat-values/p1">
     *     everyhue</a> for details. Note that the returned array has size 3, as the third
     *     element represents the brightness of the color (step 7 within the algorithm).
     */
    public static XYColor rgbToXY(int rgb) {
        // Extract colors normed to 0...1
        double r = extractRed(rgb) / 255d;
        double g = extractGreen(rgb) / 255d;
        double b = extractBlue(rgb) / 255d;

        // Apply a gamma correction.
        r = (r > 0.04045f) ? pow((r + 0.055f) / (1.0f + 0.055f), 2.4f) : (r / 12.92f);
        g = (g > 0.04045f) ? pow((g + 0.055f) / (1.0f + 0.055f), 2.4f) : (g / 12.92f);
        b = (b > 0.04045f) ? pow((b + 0.055f) / (1.0f + 0.055f), 2.4f) : (b / 12.92f);

        // Convert to XYZ using the Wide RGB D65 conversion formula.
        double X = r * 0.649926f + g * 0.103455f + b * 0.197109f;
        double Y = r * 0.234327f + g * 0.743075f + b * 0.022598f;
        double Z = r * 0.0000000f + g * 0.053077f + b * 1.035763f;

        // Calculate x and y values from the XYZ values.
        double x = X / (X + Y + Z);
        double y = Y / (X + Y + Z);

        // Return the color point and the brightness.
        return new XYColor(new double[] {x, y}, (int) (Y * 255));
    }

    /**
     * Converts a given xy color to the RGB system. See
     * <a href="https://github.com/PhilipsHue/PhilipsHueSDK-iOS-OSX/blob/master/ApplicationDesignNotes/RGB%20to%20xy%20Color%20conversion.md"
     * PhilipsHueSDK</a> for the algorithm.
     *
     * @param xy given x and y values.
     * @param brightness brightness of the colour.
     * @return an rgb colour matching xy and brightness.
     */
    public static int xyToRgb(double[] xy, int brightness) {
        double x = xy[0];
        double y = xy[1];
        double z = 1.0f - x - y;

        double Y = brightness / 255.0;
        double X = (Y / y) * x;
        double Z = (Y / y) * z;

        // convert to RGB using Wide RGB D65 conversion
        double r = X * 1.612 - Y * 0.203 - Z * 0.302;
        double g = -X * 0.509 + Y * 1.412 + Z * 0.066;
        double b = X * 0.026 - Y * 0.072 + Z * 0.962;

        // Apply a reverse gamma correction
        r = r <= 0.0031308 ? 12.92 * r : (1.0 + 0.055) * pow(r, (1.0 / 2.4)) - 0.055;
        g = g <= 0.0031308 ? 12.92 * g : (1.0 + 0.055) * pow(g, (1.0 / 2.4)) - 0.055;
        b = b <= 0.0031308 ? 12.92 * b : (1.0 + 0.055) * pow(b, (1.0 / 2.4)) - 0.055;

        r *= 255;
        g *= 255;
        b *= 255;

        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;

        return ((int) r) << 16 | ((int) g) << 8 | (int) b;
    }

    public static String toHexString(int color, int digits) {
        String asHex = Integer.toHexString(color).toUpperCase(Locale.getDefault());
        return "0x" + StringUtil.prefixPad(asHex, "0", digits);
    }

    static int extractBlue(int rgb) {
        return (rgb & 0xFF);
    }

    static int extractGreen(int rgb) {
        return ((rgb >> 8) & 0xFF);
    }

    static int extractRed(int rgb) {
        return (rgb >> 16);
    }
}
