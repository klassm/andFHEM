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

package li.klass.fhem.adapter.rooms;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GroupComparator implements Comparator<String> {

    private final String unknownValue;
    private final List<String> deviceGroupParentsOrder;

    public GroupComparator(String unknownValue, List<String> deviceGroupParentsOrder) {
        this.unknownValue = unknownValue;
        this.deviceGroupParentsOrder = deviceGroupParentsOrder;
    }

    @Override
    public int compare(String lhs, String rhs) {
        Integer groupIndexLhs = indexFor(lhs);
        Integer groupIndexRhs = indexFor(rhs);

        if (!groupIndexLhs.equals(groupIndexRhs)) {
            return groupIndexLhs.compareTo(groupIndexRhs);
        }

        return lhs.toLowerCase(Locale.getDefault()).compareTo(rhs.toLowerCase(Locale.getDefault()));
    }

    private Integer indexFor(String groupValue) {
        if (deviceGroupParentsOrder.contains(groupValue)) {
            return deviceGroupParentsOrder.indexOf(groupValue);
        }
        return deviceGroupParentsOrder.indexOf(unknownValue);
    }
}
