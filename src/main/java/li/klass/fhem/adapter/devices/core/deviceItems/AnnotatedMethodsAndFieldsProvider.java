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

package li.klass.fhem.adapter.devices.core.deviceItems;

import com.google.common.collect.Sets;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.genericview.ShowField;

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
@Singleton
public class AnnotatedMethodsAndFieldsProvider {
    @Inject
    public AnnotatedMethodsAndFieldsProvider() {
    }

    /**
     * Generates a list of annotated methods and fields of a given clazz. We only consider
     * fields and methods annotated by the {@link li.klass.fhem.domain.genericview.ShowField}
     * annotation. Superclasses are also considered.
     *
     * @param clazz class to handle.
     * @return list of annotated fields and methods.
     */
    public Set<DeviceViewItem> generateAnnotatedClassItemsList(Class<?> clazz) {
        Set<DeviceViewItem> items = Sets.newHashSet();
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
    private void handleClassForAnnotatedClassItems(Class<?> clazz,
                                                   Set<DeviceViewItem> items) {
        if (clazz == null) return;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ShowField.class)) {
                items.add(new AnnotatedDeviceViewField(field));
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ShowField.class)) {
                items.add(new AnnotatedDeviceViewMethod(method));
            }
        }

        handleClassForAnnotatedClassItems(clazz.getSuperclass(), items);
    }
}
