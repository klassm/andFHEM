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

import li.klass.fhem.domain.setlist.typeEntry.*
import li.klass.fhem.domain.setlist.typeEntry.DateTimeSetListEntry.Companion.parseConfig

enum class SetListItemType(private val supportsType: SupportsType) {
    NO_ARG(SupportsType("noArg", 1)) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            NoArgSetListEntry(key)
    },
    RGB(object : SupportsType("colorpicker") {
        override fun supports(parts: List<String>): Boolean =
                super.supports(parts) && parts.size > 1 && parts[1].equals("RGB", ignoreCase = true)
    }) {
        override fun entryFor(key: String, parts: List<String>): SetListItem = RGBSetListEntry(key)
    },
    TIME(SupportsType("time", 1)) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            TimeSetListEntry(key)
    },
    TEXT_FIELD(SupportsType("textField", 1)) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            TextFieldSetListEntry(key)
    },
    TEXT_FIELD_LONG(SupportsType("textField-long", 1)) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            TextFieldLongSetListEntry(key)
    },
    SLIDER(SupportsType("slider", 4)) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            SliderSetListEntry(key, parts)
    },
    COLORPICKER_SLIDER(object : SupportsType("colorpicker", 5) {
        override fun supports(parts: List<String>): Boolean =
                super.supports(parts) && !RGB.supports(parts)
    }) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            SliderSetListEntry(key, parts.subList(1, parts.size))
    },
    MULTIPLE(SupportsType("multiple")) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            MultipleSetListEntry(key, parts)
    },
    MULTIPLE_STRICT(SupportsType("multiple-strict")) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            MultipleStrictSetListEntry(key, parts)
    },
    KNOB(SupportsType("knob")) {
        override fun entryFor(key: String, parts: List<String>): SetListItem? = null
    },
    DATETIME(SupportsType("datetime.*")) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            DateTimeSetListEntry(key, parseConfig(parts))
    },
    GROUP(SupportsType("group_dummy_key")) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            GroupSetListEntry(key, parts)
    },
    NOT_FOUND(SupportsType("not_found")) {
        override fun entryFor(key: String, parts: List<String>): SetListItem =
            NotFoundSetListEntry(key)
    };

    val type: String
        get() = supportsType.type

    fun getSetListItemFor(key: String, parts: List<String>): SetListItem? = entryFor(key, parts)

    fun supports(parts: List<String>): Boolean = supportsType.supports(parts)

    abstract fun entryFor(key: String, parts: List<String>): SetListItem?
}
