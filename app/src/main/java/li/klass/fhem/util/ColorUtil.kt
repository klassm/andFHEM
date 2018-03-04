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

import java.lang.Math.pow
import java.util.*

object ColorUtil {
    class XYColor(val xy: DoubleArray, val brightness: Int)

    /**
     * Converts a given RGB colorAttribute to the xy system. See
     * [](https://github.com/PhilipsHue/PhilipsHueSDK-iOS-OSX/blob/master/ApplicationDesignNotes/RGB%20to%20xy%20Color%20conversion.md) for the algorithm.
     *
     * @param rgb given RGB value.
     * @return array of doubles containing the x and y values. xy is also called the
     * "colorAttribute point" (see [
 * everyhue](http://www.everyhue.com/vanilla/discussion/94/rgb-to-xy-or-hue-sat-values/p1) for details. Note that the returned array has size 3, as the third
     * element represents the brightness of the colorAttribute (step 7 within the algorithm).
     */
    fun rgbToXY(rgb: Int): XYColor {
        // Extract colors normed to 0...1
        var r = extractRed(rgb) / 255.0
        var g = extractGreen(rgb) / 255.0
        var b = extractBlue(rgb) / 255.0

        // Apply a gamma correction.
        r = if (r > 0.04045f) pow((r + 0.055f) / (1.0f + 0.055f), 2.4) else r / 12.92f
        g = if (g > 0.04045f) pow((g + 0.055f) / (1.0f + 0.055f), 2.4) else g / 12.92f
        b = if (b > 0.04045f) pow((b + 0.055f) / (1.0f + 0.055f), 2.4) else b / 12.92f

        // Convert to XYZ using the Wide RGB D65 conversion formula.
        val X = r * 0.649926f + g * 0.103455f + b * 0.197109f
        val Y = r * 0.234327f + g * 0.743075f + b * 0.022598f
        val Z = r * 0.0000000f + g * 0.053077f + b * 1.035763f

        // Calculate x and y values from the XYZ values.
        val x = X / (X + Y + Z)
        val y = Y / (X + Y + Z)

        // Return the colorAttribute point and the brightness.
        return XYColor(doubleArrayOf(x, y), (Y * 255).toInt())
    }

    /**
     * Converts a given xy colorAttribute to the RGB system. See
     * [](https://github.com/PhilipsHue/PhilipsHueSDK-iOS-OSX/blob/master/ApplicationDesignNotes/RGB%20to%20xy%20Color%20conversion.md) for the algorithm.
     *
     * @param xy         given x and y values.
     * @param brightness brightness of the colour.
     * @return an rgb colour matching xy and brightness.
     */
    fun xyToRgb(xy: DoubleArray, brightness: Int): Int {
        val x = xy[0]
        val y = xy[1]
        val z = 1.0 - x - y

        val Y = brightness / 255.0
        val X = Y / y * x
        val Z = Y / y * z

        // convert to RGB using Wide RGB D65 conversion
        var r = X * 1.612 - Y * 0.203 - Z * 0.302
        var g = -X * 0.509 + Y * 1.412 + Z * 0.066
        var b = X * 0.026 - Y * 0.072 + Z * 0.962

        // Apply a reverse gamma correction
        r = if (r <= 0.0031308) 12.92 * r else (1.0 + 0.055) * pow(r, 1.0 / 2.4) - 0.055
        g = if (g <= 0.0031308) 12.92 * g else (1.0 + 0.055) * pow(g, 1.0 / 2.4) - 0.055
        b = if (b <= 0.0031308) 12.92 * b else (1.0 + 0.055) * pow(b, 1.0 / 2.4) - 0.055

        r *= 255.0
        g *= 255.0
        b *= 255.0

        if (r > 255) r = 255.0
        if (g > 255) g = 255.0
        if (b > 255) b = 255.0

        return r.toInt() shl 16 or (g.toInt() shl 8) or b.toInt()
    }

    fun toHexString(color: Int, digits: Int): String {
        val asHex = Integer.toHexString(color).toUpperCase(Locale.getDefault())
        return "0x" + StringUtil.prefixPad(asHex, "0", digits)
    }

    fun extractBlue(rgb: Int): Int = rgb and 0xFF

    fun extractGreen(rgb: Int): Int = rgb shr 8 and 0xFF

    fun extractRed(rgb: Int): Int = rgb shr 16

    fun fromRgbString(rgb: String): Int {
        val toDecode = if (rgb.startsWith("0x")) rgb.substring(2) else rgb
        val withSixLetters = if (toDecode.length == 8) toDecode.substring(2) else toDecode

        return if (!withSixLetters.matches("[0-9a-fA-F]{6}".toRegex())) {
            0
        } else Integer.decode("0x" + withSixLetters) and 0xFFFFFF
    }

    fun toHexStringWithoutPrefix(color: Int): String {
        return StringUtil.prefixPad(
                Integer.toHexString(color),
                "0", 6
        )
    }
}
