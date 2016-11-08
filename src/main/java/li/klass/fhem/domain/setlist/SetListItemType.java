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

package li.klass.fhem.domain.setlist;

import com.google.common.base.Optional;

import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.MultipleSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.MultipleStrictSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.NotFoundSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.RGBSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.TextFieldLongSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.TextFieldSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.TimeSetListEntry;

public enum SetListItemType {
    NO_ARG(new SupportsType("noArg", 1), (key, parts) -> new NoArgSetListEntry(key)),
    RGB(new SupportsType("colorpicker") {
        @Override
        public boolean supports(String[] parts) {
            return super.supports(parts) && parts[1].equalsIgnoreCase("RGB");
        }
    }, (key, parts) -> new RGBSetListEntry(key)),
    TIME(new SupportsType("time", 1), (key, parts) -> new TimeSetListEntry(key)),
    TEXT_FIELD(new SupportsType("textField", 1), (key, parts) -> new TextFieldSetListEntry(key)),
    TEXT_FIELD_LONG(new SupportsType("textField-long", 1), (key, parts) -> new TextFieldLongSetListEntry(key)),
    SLIDER(new SupportsType("slider", 4), SliderSetListEntry::new),
    MULTIPLE(new SupportsType("multiple"), MultipleSetListEntry::new),
    MULTIPLE_STRICT(new SupportsType("multiple-strict"), MultipleStrictSetListEntry::new),
    KNOB(new SupportsType("knob"), (key, parts) -> null),
    GROUP(new SupportsType("group_dummy_key") {
    }, GroupSetListEntry::new),
    NOT_FOUND(new SupportsType("not_found"), (key, parts) -> new NotFoundSetListEntry(key));

    private final EntryProvider entryProvider;

    SetListItemType(SupportsType supportsType, EntryProvider entryProvider) {
        this.supportsType = supportsType;
        this.entryProvider = entryProvider;
    }

    public String getType() {
        return supportsType.getType();
    }

    public Optional<SetListItem> getSetListItemFor(String key, String[] parts) {
        return Optional.fromNullable(entryProvider.entryFor(key, parts));
    }

    private final SupportsType supportsType;

    public boolean supports(String[] parts) {
        return supportsType.supports(parts);
    }

    private interface EntryProvider {
        SetListItem entryFor(String key, String[] parts);
    }
}
