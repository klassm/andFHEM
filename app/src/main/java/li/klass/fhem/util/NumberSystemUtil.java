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

public class NumberSystemUtil {
    public static String hexToQuaternary(String input, int padding) {
        input = input.toUpperCase(Locale.getDefault());
        if (! validateHex(input)) throw new IllegalArgumentException("hex may only contain 0-9A-F");

        int totalDecimal = toDecimal(input, 16);
        String result = toNumberSystem(totalDecimal, 4);

        while (result.length() < padding) {
            result = "0" + result;
        }
        return result;
    }

    private static boolean validateHex(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (! (c >= '0' && c <= '9' || c >= 'A' && c <= 'F')) {
                return false;
            }
        }

        return true;
    }

    public static String quaternaryToHex(String input) {
        if (! validateQuaternary(input)) throw new IllegalArgumentException("quaternary may only contain 0-3");

        int totalDecimal = toDecimal(input, 4);
        return toNumberSystem(totalDecimal, 16);
    }

    public static int hexToDecimal(String hex) {
        return toDecimal(hex, 16);
    }

    private static String toNumberSystem(int totalDecimal, int targetSystem) {
        StringBuilder result = new StringBuilder();
        do {
            int rest = (totalDecimal % targetSystem);
            totalDecimal = totalDecimal / targetSystem;
            if (rest >= 0 && rest <= 9) result.append((char) ('0' + rest));
            else result.append((char) ('A' + (rest - 10)));
        } while (totalDecimal > 0);

        return result.reverse().toString();
    }

    private static boolean validateQuaternary(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (! (c >= '0' && c <= '3')) {
                return false;
            }
        }
        return true;
    }

    private static int toDecimal(String input, int base) {
        input = input.toUpperCase(Locale.getDefault());

        int totalDecimal = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int letterValue;
            if (c >= '0' && c <= '9') letterValue = c - '0';
            else letterValue = c - 'A' + 10;

            totalDecimal = base * totalDecimal + letterValue;
        }
        return totalDecimal;
    }
}
