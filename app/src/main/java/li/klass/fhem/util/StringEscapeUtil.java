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

import java.util.LinkedHashMap;
import java.util.Map;

public class StringEscapeUtil {

    private static Map<String, String> replacements = null;

    public static String unescape(String content) {
        if(replacements == null) {
            synchronized(StringEscapeUtil.class) {
                if(replacements == null) {
                    LinkedHashMap<String, String> temp = new LinkedHashMap<>(7);
                    temp.put("\u00c3\u00bc", "ü");
                    temp.put("\u00c3\u00a4", "ä");
                    temp.put("\u00c3\u00b6", "ö");
                    temp.put("\u00c3\u0096", "Ö");
                    temp.put("\u00c3\u0084", "Ä");
                    temp.put("\u00c3\u009c", "Ü");
                    temp.put("\u00c3\u009f", "ß");
                    replacements = temp;
                }
            }
        }
        content = replaceFromMap(content,replacements);
        return content.trim();
    }

    public static String replaceFromMap(String string,
                                        Map<String, String> replacements) {
        StringBuilder sb = new StringBuilder(string);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int start = sb.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }
}
