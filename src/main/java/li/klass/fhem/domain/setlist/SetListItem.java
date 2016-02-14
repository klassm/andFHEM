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

import org.apache.commons.lang3.StringUtils;

public abstract class SetListItem implements SetListEntry {
    protected final String key;
    protected final SetListItemType type;


    public SetListItem(String key, SetListItemType type) {
        key = StringUtils.trimToNull(key);
        this.key = key == null ? "state" : key;
        this.type = type;
    }

    @Override
    public String getKey() {
        return key;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetListItem that = (SetListItem) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SetListItem{" +
                "key='" + key + '\'' +
                ", type=" + type +
                '}';
    }
}
