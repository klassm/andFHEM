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

package li.klass.fhem.domain.setlist

import org.apache.commons.lang3.StringUtils

abstract class SetListItem(key: String?, protected val type: SetListItemType) : SetListEntry {
    override val key: String = StringUtils.trimToNull(key) ?: "state"


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as SetListItem?

        return key == that?.key && type == that.type

    }

    override fun hashCode(): Int {
        var result = if (true) key.hashCode() else 0
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "SetListItem{" +
                "key='" + key + '\''.toString() +
                ", type=" + type +
                '}'.toString()
    }
}
