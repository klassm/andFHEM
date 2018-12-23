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

import li.klass.fhem.domain.setlist.SetListItem
import li.klass.fhem.domain.setlist.SetListItemType
import li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble

import li.klass.fhem.util.equalByEpsilon

class SliderSetListEntry(key: String?, val start: Double, val step: Double, val stop: Double) : SetListItem(key, SetListItemType.SLIDER) {

    constructor(key: String, parts: List<String>) : this(key, extractLeadingDouble(parts[1]), extractLeadingDouble(parts[2]), extractLeadingDouble(parts[3])) {}

    override fun toString(): String {
        return "SliderSetListEntry{" +
                "start=" + start +
                ", stop=" + stop +
                ", step=" + step +
                '}'.toString()
    }

    override fun asText(): String {
        return "slider,$start,$step,$stop"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        if (other !is SliderSetListEntry) return false

        val mStart = start.equalByEpsilon(other.start)
        val mStop = stop.equalByEpsilon(other.stop)
        val mStep = step.equalByEpsilon(other.step)

        return mStart && mStop && mStep
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + step.hashCode()
        result = 31 * result + stop.hashCode()
        return result
    }
}
