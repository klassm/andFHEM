/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.util;

import java.util.List;

public class StringUtil {
    public static String concatenate(String[] elements, String delimiter) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];

            if (i > 0) {
                out.append(delimiter);
            }
            out.append(element);
        }
        return out.toString();
    }

    public static String concatenate(List<String> elements, String delimiter) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);

            if (i > 0) {
                out.append(delimiter);
            }
            out.append(element);
        }
        return out.toString();
    }

    public static boolean endsWith(StringBuilder stringBuilder, String suffix) {
        if (suffix.length() > stringBuilder.length()) return false;

        for (int i = 0; i < suffix.length(); i++) {
            char mustCharacter = suffix.charAt(i);
            char isCharacter = stringBuilder.charAt(stringBuilder.length() - suffix.length() + i);

            if (mustCharacter != isCharacter) {
                return false;
            }
        }

        return true;
    }

    public static String prefixPad(String toPad, String padLetter, int targetLength) {
        if (toPad == null) toPad = "";
        while(toPad.length() < targetLength) {
            toPad = padLetter + toPad;
        }

        return toPad;
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().equals("");
    }
}
