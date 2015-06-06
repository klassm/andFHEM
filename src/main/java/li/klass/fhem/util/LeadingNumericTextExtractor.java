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

import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.newHashSet;

public class LeadingNumericTextExtractor {

    private enum State {
        START, NEGATIVE, NUMBER, COMMA, MANTISSA_AFTER_COMMA, EXPONENT_DELIMITER, NEGATIVE_EXPONENT, EXPONENT, EXIT_WITHOUT_TEXT, EXIT, ERROR
    }

    private static final Set<State> END_STATES = newHashSet(State.NUMBER, State.COMMA, State.MANTISSA_AFTER_COMMA, State.EXPONENT, State.EXIT, State.ERROR);

    private State state = State.START;
    StringBuilder output = new StringBuilder();

    public LeadingNumericTextExtractor(String input) {
        parse(input);
    }

    private void parse(String input) {
        input = input.trim();
        if (isNullOrEmpty(input)) {
            return;
        }

        char[] chars = input.toCharArray();

        for (char c : chars) {
            if (!parse(c)) {
                return;
            }
        }
        if (state == State.COMMA) {
            output.append('0');
        }
    }

    private boolean parse(char c) {
        switch (state) {
            case EXIT:
            case EXIT_WITHOUT_TEXT:
            case ERROR:
                return false;
            case START:
                if (c == '-') {
                    output.append(c);
                    state = State.NEGATIVE;
                } else if (isDecimal(c)) {
                    output.append(c);
                    state = State.NUMBER;
                } else {
                    state = State.EXIT_WITHOUT_TEXT;
                }
                break;
            case NEGATIVE:
                if (isDecimal(c)) {
                    output.append(c);
                    state = State.NUMBER;
                } else {
                    state = State.EXIT_WITHOUT_TEXT;
                }
                break;
            case NUMBER:
                if (isDecimal(c)) {
                    output.append(c);
                    state = State.NUMBER;
                } else if (isComma(c)) {
                    output.append(".");
                    state = State.COMMA;
                } else {
                    state = State.EXIT;
                }
                break;
            case COMMA:
                if (isDecimal(c)) {
                    output.append(c);
                    state = State.MANTISSA_AFTER_COMMA;
                } else if (isExponentDelimiter(c)) {
                    output.append('0');
                    output.append('E');
                    state = State.EXPONENT_DELIMITER;
                } else {
                    state = State.EXIT;
                }
                break;
            case MANTISSA_AFTER_COMMA:
                if (isDecimal(c)) {
                    output.append(c);
                    state = State.MANTISSA_AFTER_COMMA;
                } else if (isExponentDelimiter(c)) {
                    output.append('E');
                    state = State.EXPONENT_DELIMITER;
                } else {
                    state = State.ERROR;
                }
                break;
            case EXPONENT_DELIMITER:
                if (c == '-') {
                    output.append(c);
                    state = State.NEGATIVE_EXPONENT;
                } else if (isDecimal(c)) {
                    output.append(c);
                    state = State.EXPONENT;
                } else {
                    state = State.ERROR;
                }
                break;
            case NEGATIVE_EXPONENT:
                if (isDecimal(c)) {
                    output.append(c);
                    state = State.EXPONENT;
                }
                break;
            case EXPONENT:
                if (isDecimal(c)) {
                    output.append(c);
                    state = State.EXPONENT;
                } else {
                    state = State.EXIT;
                }
                break;
        }
        return true;
    }

    private boolean isComma(char c) {
        return c == ',' || c == '.';
    }

    private boolean isDecimal(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isExponentDelimiter(char c) {
        return c == 'e' || c == 'E';
    }

    public String numericText() {
        if (!END_STATES.contains(state)) {
            return "";
        }
        return output.toString();
    }
}
