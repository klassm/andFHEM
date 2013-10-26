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

package li.klass.fhem.adapter.devices.core;

import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * Comparator enforcing a total ordering on all fields to be compared.
 * Essentially, the field names are compared. However, whenever a {@link ShowField} annotation is found, we
 * look for a {@link li.klass.fhem.domain.genericview.ShowField#showAfter()} value. This value means, that a given
 * field has to be shown after another field within the list.
 * <p/>
 * When finding a such field, we assume the field name of the show-after field to be the value of the showAfter annotation
 * value. The only exception for this algorithm is whenever the second field name equals the value of the showAfter
 * annotation value.
 * <p/>
 * We need to assume the field name to be the showAfter value to get a consistent behaviour for the comparator,
 * as the showAfter field's name does not necessarily have the same behaviour as the showAfter field.
 */
public class FieldNameComparator implements Comparator<Field> {
    public static final FieldNameComparator COMPARATOR = new FieldNameComparator();

    private FieldNameComparator() {
    }

    @Override
    public int compare(Field lhs, Field rhs) {
        ShowField rhsAnnotation = rhs.getAnnotation(ShowField.class);
        ShowField lhsAnnotation = lhs.getAnnotation(ShowField.class);

        String rhsName = rhs.getName();
        String lhsName = lhs.getName();

        if (lhsAnnotation != null && !StringUtil.isBlank(lhsAnnotation.showAfter())) {
            lhsName = lhsAnnotation.showAfter();
        }

        if (rhsAnnotation != null && !StringUtil.isBlank(rhsAnnotation.showAfter())) {
            rhsName = rhsAnnotation.showAfter();
        }


        if (fieldMatchesShowAfter(rhsAnnotation, lhs)) {
            return -1;
        }

        if (fieldMatchesShowAfter(lhsAnnotation, rhs)) {
            return 1;
        }

        return lhsName.compareTo(rhsName);
    }

    public boolean fieldMatchesShowAfter(ShowField annotation, Field field) {
        if (annotation == null) return false;
        if (StringUtil.isBlank(annotation.showAfter())) return false;

        return field.getName().equalsIgnoreCase(annotation.showAfter());
    }
}
