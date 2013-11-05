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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.StringUtil;

/**
 * <p>DeviceField ordering is somewhat special, as we do not only need to sort by name
 * but also by using the showAfter annotation value.
 * This is especially difficult to handle, as the showAfter values can directly refer to
 * other showAfter annotation values.</p>
 *
 * <p>Example: <br />
 * <code>
 *     nightTemp must be after dayTemp
 *     dayTemp must be after temp
 * </code>
 * </p>
 *
 * <p>The following sort method tries to handle this mapping by recursively rewriting the
 * field names to the child's annotation value. For respecting the hierarchy level, we
 * append some underscores.</p>
 *
 * <p>After creating the mappings, we can use those to create a "normal" comparator to
 * sort the fields.</p>
 */
public class DeviceFields {
    public static void sort(List<Field> fields) {
        Map<String, String> fieldNameMapping = new HashMap<String, String>();
        for (Field field : fields) {
            ShowField annotation = field.getAnnotation(ShowField.class);

            if (annotation != null && !StringUtil.isBlank(annotation.showAfter())) {
                fieldNameMapping.put(field.getName().toLowerCase(),
                        annotation.showAfter().toLowerCase());
            }
        }

        final Map<String, String> fieldNameMappingRecursive = handleRecursiveMappings(fieldNameMapping);
        Collections.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                String lhsName = lhs.getName().toLowerCase();
                String rhsName = rhs.getName().toLowerCase();

                if (fieldNameMappingRecursive.containsKey(lhsName)) {
                    lhsName = fieldNameMappingRecursive.get(lhsName);
                }
                if (fieldNameMappingRecursive.containsKey(rhsName)) {
                    rhsName = fieldNameMappingRecursive.get(rhsName);
                }
                return lhsName.compareTo(rhsName);
            }
        });
    }

    private static Map<String, String> handleRecursiveMappings(Map<String, String> fieldNameMapping) {
        Map<String, String> newMapping = new HashMap<String, String>();
        for (String key : fieldNameMapping.keySet()) {
            String value = fieldNameMapping.get(key);

            int level = 0;

            String newValue = value;
            while (fieldNameMapping.containsKey(newValue)) {
                newValue = fieldNameMapping.get(newValue);
                level++;
            }

            for (int i = 0; i <= level; i++) {
                newValue += "_";
            }
            newMapping.put(key, newValue);
        }

        return newMapping;
    }
}
