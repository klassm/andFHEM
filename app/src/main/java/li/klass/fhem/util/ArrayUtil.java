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
import java.util.Arrays;
import java.util.List;

public class ArrayUtil {
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    @SafeVarargs
    public static <T> boolean contains(T[] array, T... toLookFor) {
        if (array == null) return false;

        List<T> searchList = new ArrayList<>(Arrays.asList(toLookFor));
        for (T element : array) {
            if (searchList.contains(element)) {
                searchList.remove(element);
            }
            if (searchList.size() == 0) {
                return true;
            }
        }
        return false;
    }

    public static String join(String[] array, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) builder.append(delimiter);
            builder.append(array[i]);
        }

        return builder.toString();
    }

    public static String[] addToArray(String[] array, String newElement) {
        int length = array == null ? 1 : array.length + 1;
        String[] newArray = new String[length];

        if (array != null) {
            System.arraycopy(array, 0, newArray, 0, array.length);
            newArray[array.length] = newElement;
        } else {
            newArray[0] = newElement;
        }

        return newArray;
    }

    public static String[] removeFromArray(String[] array, String toRemove) {
        int counter = 0;

        String[] newArray = new String[array.length];
        for (String element : array) {
            if (!element.equals(toRemove)) {
                newArray[counter++] = element;
            }
        }

        String[] ret = new String[counter];
        System.arraycopy(newArray, 0, ret, 0, ret.length);

        return ret;
    }

    /**
     * Copy an array. Note that we cannot use {@link Arrays#copyOf}, as this is not supported in all Android versions.
     *
     * @param toCopy array to copy
     * @return copy
     */
    public static String[] copyOf(String[] toCopy) {
        String[] copy = new String[toCopy.length];
        System.arraycopy(toCopy, 0, copy, 0, toCopy.length);
        return copy;
    }
}
