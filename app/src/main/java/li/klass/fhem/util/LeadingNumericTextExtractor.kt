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

import org.apache.commons.lang3.StringUtils

class LeadingNumericTextExtractor(input: String) {
    private var state = State.START
    var output = StringBuilder()

    init {
        parse(input)
    }

    private enum class State {
        START, NEGATIVE, NUMBER, COMMA, MANTISSA_AFTER_COMMA, EXPONENT_DELIMITER, NEGATIVE_EXPONENT, EXPONENT, EXIT_WITHOUT_TEXT, EXIT, ERROR
    }

    private fun parse(input: String) {
        val chars = StringUtils.trimToNull(input)?.toCharArray() ?: return
        for (c in chars) {
            if (!parse(c)) {
                return
            }
        }
        if (state == State.COMMA) {
            output.append('0')
        }
    }

    private fun parse(c: Char): Boolean {
        when (state) {
            State.EXIT, State.EXIT_WITHOUT_TEXT, State.ERROR -> return false

            State.START -> state = when {
                c == '-' -> {
                    output.append(c)
                    State.NEGATIVE
                }
                isDecimal(c) -> {
                    output.append(c)
                    State.NUMBER
                }
                else -> {
                    State.EXIT_WITHOUT_TEXT
                }
            }

            State.NEGATIVE -> state = if (isDecimal(c)) {
                output.append(c)
                State.NUMBER
            } else {
                State.EXIT_WITHOUT_TEXT
            }

            State.NUMBER -> state = if (isDecimal(c)) {
                output.append(c)
                State.NUMBER
            } else if (isComma(c)) {
                output.append(".")
                State.COMMA
            } else {
                State.EXIT
            }

            State.COMMA -> state = if (isDecimal(c)) {
                output.append(c)
                State.MANTISSA_AFTER_COMMA
            } else if (isExponentDelimiter(c)) {
                output.append('0')
                output.append('E')
                State.EXPONENT_DELIMITER
            } else {
                State.EXIT
            }
            State.MANTISSA_AFTER_COMMA -> state = if (isDecimal(c)) {
                output.append(c)
                State.MANTISSA_AFTER_COMMA
            } else if (isExponentDelimiter(c)) {
                output.append('E')
                State.EXPONENT_DELIMITER
            } else {
                State.ERROR
            }

            State.EXPONENT_DELIMITER -> state = if (c == '-') {
                output.append(c)
                State.NEGATIVE_EXPONENT
            } else if (isDecimal(c)) {
                output.append(c)
                State.EXPONENT
            } else {
                State.ERROR
            }

            State.NEGATIVE_EXPONENT -> if (isDecimal(c)) {
                output.append(c)
                state = State.EXPONENT
            }

            State.EXPONENT -> state = if (isDecimal(c)) {
                output.append(c)
                State.EXPONENT
            } else {
                State.EXIT
            }
        }
        return true
    }

    private fun isComma(c: Char): Boolean {
        return c == ',' || c == '.'
    }

    private fun isDecimal(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isExponentDelimiter(c: Char): Boolean {
        return c == 'e' || c == 'E'
    }

    fun numericText(): String {
        return if (!END_STATES.contains(state)) {
            ""
        } else output.toString()
    }

    companion object {
        private val END_STATES: Set<State> = setOf(State.NUMBER, State.COMMA, State.MANTISSA_AFTER_COMMA, State.EXPONENT, State.EXIT, State.ERROR)
    }
}