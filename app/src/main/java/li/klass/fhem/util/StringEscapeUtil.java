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

public class StringEscapeUtil {

    public static String unescape(String content) {

        // We replace UTF characters.
        // The underlying table can be found on various pages, i.e.
        // http://www.utf8-zeichentabelle.de/
        content = content
                .replaceAll("\u00c3\u00bc", "ü")
                .replaceAll("\u00c3\u00a4", "ä")
                .replaceAll("\u00c3\u00b6", "ö")
                .replaceAll("\u00c3\u0096", "Ö")
                .replaceAll("\u00c3\u0084", "Ä")
                .replaceAll("\u00c3\u009c", "Ü")
                .replaceAll("\u00c3\u009f", "ß")
        ;
        return content.trim();
    }
}
