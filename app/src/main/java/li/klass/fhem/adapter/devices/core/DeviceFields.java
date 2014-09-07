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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import li.klass.fhem.adapter.devices.core.showFieldAnnotation.AnnotatedDeviceClassField;
import li.klass.fhem.adapter.devices.core.showFieldAnnotation.AnnotatedDeviceClassItem;
import li.klass.fhem.adapter.devices.core.showFieldAnnotation.AnnotatedDeviceClassMethod;
import li.klass.fhem.domain.genericview.ShowField;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * <p>DeviceField ordering is somewhat special, as we do not only need to sort by name
 * but also by using the showAfter annotation value.
 * This is especially difficult to handle, as the showAfter values can directly refer to
 * other showAfter annotation values.</p>
 * <p/>
 * <p>Example: <br />
 * <code>
 * nightTemp must be after dayTemp
 * dayTemp must be after temp
 * </code>
 * </p>
 * <p/>
 * <p>The following sort method tries to handle this mapping by recursively rewriting the
 * field names to the child's annotation value. For respecting the hierarchy level, we
 * append some underscores.</p>
 * <p/>
 * <p>After creating the mappings, we can use those to create a "normal" comparator to
 * sort the fields.</p>
 */
public class DeviceFields {
    /**
     * Generates a list of fields and methods annotated by the
     * {@link li.klass.fhem.domain.genericview.ShowField} annotation. All entries are encapsulated
     * by instances of {@link li.klass.fhem.adapter.devices.core.showFieldAnnotation.AnnotatedDeviceClassItem}.
     * <p/>
     * The sorting depends on the value of the {@link li.klass.fhem.domain.genericview.ShowField#showAfter()}
     * method and the field / method name.
     *
     * @param clazz class to handle.
     * @return sorted list of annotated fields and methods.
     */
    public static List<AnnotatedDeviceClassItem> getSortedAnnotatedClassItems(Class<?> clazz) {
        List<AnnotatedDeviceClassItem> items = generateAnnotatedClassItemsList(clazz);
        sort(items);

        return items;
    }

    /**
     * Generates a list of annotated methods and fields of a given clazz. We only consider
     * fields and methods annotated by the {@link li.klass.fhem.domain.genericview.ShowField}
     * annotation. Superclasses are also considered.
     *
     * @param clazz class to handle.
     * @return list of annotated fields and methods.
     */
    private static List<AnnotatedDeviceClassItem> generateAnnotatedClassItemsList(Class<?> clazz) {
        ArrayList<AnnotatedDeviceClassItem> items = newArrayList();
        handleClassForAnnotatedClassItems(clazz, items);

        return items;
    }

    /**
     * Recursively considers a class and its superclasses. Fields and methods annotated
     * by {@link li.klass.fhem.domain.genericview.ShowField} are wrapped and put into the items
     * list.
     *
     * @param clazz class to handle.
     * @param items list of annotated items.
     */
    private static void handleClassForAnnotatedClassItems(Class<?> clazz,
                                                          List<AnnotatedDeviceClassItem> items) {
        if (clazz == null) return;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ShowField.class)) {
                items.add(new AnnotatedDeviceClassField(field));
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ShowField.class)) {
                items.add(new AnnotatedDeviceClassMethod(method));
            }
        }

        handleClassForAnnotatedClassItems(clazz.getSuperclass(), items);
    }

    /**
     * Sorts an amount of given annotated items by comparing the showAfter-method value of
     * the {@link li.klass.fhem.domain.genericview.ShowField} annotation and by considering
     * the field / method name.
     *
     * @param items items to sort
     */
    public static void sort(List<AnnotatedDeviceClassItem> items) {
        Map<String, String> fieldNameMapping = newHashMap();
        for (AnnotatedDeviceClassItem item : items) {
            String showAfterValue = item.getShowAfterValue();
            if (showAfterValue != null) {
                String lowerCaseName = item.getName().toLowerCase(Locale.getDefault());
                if (ShowField.FIRST.equals(showAfterValue)) {
                    // make sure we are the first one!
                    fieldNameMapping.put(lowerCaseName, "___" + lowerCaseName);
                } else {
                    fieldNameMapping.put(lowerCaseName,
                            showAfterValue.toLowerCase(Locale.getDefault()));
                }
            }
        }

        final Map<String, String> fieldNameMappingRecursive = handleRecursiveMappings(fieldNameMapping);
        Collections.sort(items, new Comparator<AnnotatedDeviceClassItem>() {
            @Override
            public int compare(AnnotatedDeviceClassItem lhs, AnnotatedDeviceClassItem rhs) {
                String lhsName = lhs.getName().toLowerCase(Locale.getDefault());
                String rhsName = rhs.getName().toLowerCase(Locale.getDefault());

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

    /**
     * Generates a map of showAfter mappings. We also consider mappings pointing to other mappings
     * and so on. To represent the level, we add an underscore as suffix for each level to each
     * mapping value.
     * <p/>
     * Careful: We do not consider round-trip mappings! This will result in an infinite loop!
     *
     * @param fieldNameMapping mapping map.
     * @return new mapping map considering recursive mappings.
     */
    private static Map<String, String> handleRecursiveMappings(Map<String, String> fieldNameMapping) {
        Map<String, String> newMapping = newHashMap();
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
