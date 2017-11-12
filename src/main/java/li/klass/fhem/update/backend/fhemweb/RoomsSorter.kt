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

package li.klass.fhem.update.backend.fhemweb

import java.util.*
import javax.inject.Inject

class RoomsSorter @Inject constructor() {
    fun sort(roomsToSort: Collection<String>, sortRoomsAttribute: List<String>): List<String> =
            roomsToSort.sortedWith(sortRoomsComparator(sortRoomsAttribute))

    private fun sortRoomsComparator(sortRoomsAttribute: List<String>): Comparator<String> {
        return Comparator { lhs, rhs ->
            val lhsIndex = sortRoomsAttribute.indexOf(lhs)
            val rhsIndex = sortRoomsAttribute.indexOf(rhs)

            if (lhsIndex == rhsIndex && lhsIndex == -1) {
                // both not in sort list, compare based on names
                lhs.compareTo(rhs)
            } else if (lhsIndex != rhsIndex && lhsIndex != -1 && rhsIndex != -1) {
                // both in sort list, compare indexes
                lhsIndex.compareTo(rhsIndex)
            } else if (lhsIndex == -1) {
                // lhs not in sort list, rhs in sort list
                1
            } else {
                // rhs not in sort list, lhs in sort list
                -1
            }
        }
    }
}