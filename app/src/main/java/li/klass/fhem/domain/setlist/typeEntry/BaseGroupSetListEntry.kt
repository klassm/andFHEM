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

package li.klass.fhem.domain.setlist.typeEntry

import com.google.common.base.Preconditions
import li.klass.fhem.domain.setlist.SetListItem
import li.klass.fhem.domain.setlist.SetListItemType
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.join

abstract class BaseGroupSetListEntry(
        key: String?,
        type: SetListItemType,
        groupStates: List<String>
) : SetListItem(key, type) {
    val groupStates: List<String> = groupStates.filter {
        StringUtils.trimToNull(it) != null
    }

    override fun asText(): String {
        return key + ":" + join(groupStates, ",")
    }


    fun asType(): String {
        Preconditions.checkArgument(groupStates.size == 1)
        return groupStates[0]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false

        val that = other as BaseGroupSetListEntry?

        return groupStates == that!!.groupStates

    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + groupStates.hashCode()
        return result
    }

    override fun toString(): String {
        return "GroupSetListEntry{" +
                "groupStates=" + groupStates +
                ",key=" + key +
                ",type=" + type +
                "}"
    }
}
