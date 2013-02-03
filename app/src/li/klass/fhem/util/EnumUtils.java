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

import java.util.ArrayList;
import java.util.List;

public class EnumUtils {
    public static <T extends Enum<T>> List<String> toStringList(T[] values) {
        List<String> result = new ArrayList<String>();
        for (T value : values) {
            result.add(value.name());
        }
        return result;
    }

    public static <T extends Enum<T>> int positionOf(T[] values, T mode) {
        for (int i = 0; i < values.length; i++) {
            T value = values[i];
            if (value == mode) {
                return i;
            }
        }
        return -1;
    }

    public static <T extends Enum<T>> T valueOf(T[] values, String toSearch) {
        for (T value : values) {
            if (toSearch.equalsIgnoreCase(value.name())) {
                return value;
            }
        }

        return null;
    }
}
