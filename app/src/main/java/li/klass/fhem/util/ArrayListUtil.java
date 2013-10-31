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

import java.util.ArrayList;

public class ArrayListUtil {
    public static <T> void moveUp(ArrayList<T> list, int elementPosition) {
        Reject.ifNull(list);

        if (elementPosition == 0) return;

        swap(list, elementPosition, elementPosition - 1);
    }

    public static <T> void moveDown(ArrayList<T> list, int elementPosition) {
        Reject.ifNull(list);

        if (elementPosition == list.size() - 1) return;

        swap(list, elementPosition, elementPosition + 1);
    }

    public static <T> void swap(ArrayList<T> list, int firstIndex, int secondIndex) {
        Reject.ifNull(list);
        if (! isInRange(list, firstIndex)) throw new IllegalArgumentException("firstIndex " + firstIndex + " is not in range");
        if (! isInRange(list, secondIndex)) throw new IllegalArgumentException("secondIndex " + secondIndex + " is not in range");

        T firstElement = list.get(firstIndex);
        T secondElement = list.get(secondIndex);

        list.set(firstIndex, secondElement);
        list.set(secondIndex, firstElement);
    }

    private static <T> boolean isInRange(ArrayList<T> list, int position) {
        return !(position < 0 || position >= list.size());
    }

    public static <T> ArrayList<T> filter(ArrayList<T> toFilter, Filter<T> filter) {
        Reject.ifNull(toFilter);
        Reject.ifNull(filter);

        ArrayList<T> result = new ArrayList<T>();
        for (T element : toFilter) {
            if (filter.doFilter(element)) {
                result.add(element);
            }
        }
        return result;
    }
}
