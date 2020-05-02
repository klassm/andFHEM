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

import kotlin.math.pow

object ValueExtractUtil {
    fun extractLeadingFloat(text: String?): Float {
        return extractLeadingDouble(text, -1).toFloat()
    }

    @JvmStatic
    @JvmOverloads
    fun extractLeadingDouble(text: String?, digits: Int = -1): Double {
        val leading = extractLeadingNumericText(text, digits)
        return if (leading.isEmpty()) 0.0 else leading.toDouble()
    }

    @JvmStatic
    fun extractLeadingInt(text: String?): Int {
        return extractLeadingDouble(text, 0).toInt()
    }

    @JvmStatic
    fun extractLeadingNumericText(text: String?, digits: Int): String {
        if (text.isNullOrEmpty()) return ""
        val numericText = LeadingNumericTextExtractor(text).numericText()
        return if (digits > 0) {
            val number = java.lang.Double.valueOf(numericText)
            val roundFactor = 10.0.pow(digits.toDouble())
            val rounded = (number * roundFactor).toInt() / roundFactor
            rounded.toString() + ""
        } else {
            numericText
        }
    }

    fun onOffToTrueFalse(value: String): Boolean {
        return value.equals("on", ignoreCase = true) || value.equals("true", ignoreCase = true)
    }
}