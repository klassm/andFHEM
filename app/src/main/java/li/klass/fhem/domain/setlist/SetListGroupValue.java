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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.join;

public class SetListGroupValue implements SetListValue {
    private final List<String> groupStates;

    public SetListGroupValue(String... groupStates) {
        checkNotNull(groupStates);
        this.groupStates = Lists.newArrayList(groupStates);
    }

    @Override
    public String asText() {
        return join(groupStates, ",");
    }

    public List<String> getGroupStates() {
        return Collections.unmodifiableList(groupStates);
    }

    public String asType() {
        Preconditions.checkArgument(groupStates.size() == 1);
        return groupStates.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetListGroupValue that = (SetListGroupValue) o;

        if (!groupStates.equals(that.groupStates)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return groupStates != null ? groupStates.hashCode() : 0;
    }
}
