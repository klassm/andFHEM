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

object ValueDescriptionUtil {
    const val C = "°C"
    private const val PERCENT = "%"
    const val L = "l"

    @JvmStatic
    fun appendPercent(text: Any): String {
        return append(text, PERCENT)
    }

    fun append(text: Any, appendix: String): String {
        return "$text ($appendix)"
    }

    fun appendTemperature(text: Any): String {
        return append(text, C)
    }

    @JvmStatic
    fun secondsToTimeString(inputSeconds: Int): String {
        var seconds = inputSeconds
        val hours: Int
        val minutes: Int
        hours = seconds / 3600
        seconds -= hours * 3600
        minutes = seconds / 60
        seconds -= minutes * 60
        var out = ""
        if (hours > 0) {
            out = appendToString(out, "$hours (h)")
        }
        if (minutes > 0) {
            out = appendToString(out, "$minutes (m)")
        }
        if (seconds > 0) {
            out = appendToString(out, "$seconds (s)")
        }
        return out
    }

    private fun appendToString(string: String, toAppend: String): String {
        var result = string
        if (result.isNotEmpty()) {
            result += " "
        }
        return result + toAppend
    }
}