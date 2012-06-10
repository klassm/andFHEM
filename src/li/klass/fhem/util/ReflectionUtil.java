/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.util;

import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.domain.FHTDevice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil {
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> result = new ArrayList<Field>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(annotation)) {
                result.add(field);
            }
        }
        return result;
    }

    public static String getStringForAnnotation(Object object, Class<? extends Annotation> annotation) {
        List<Field> fields = getFieldsWithAnnotation(object.getClass(), annotation);
        if (fields.size() > 1 || fields.size() == 0) {
            throw new IllegalArgumentException("expected exactly one occurence for annotation " + annotation.getName() +
                    " in object " + object.toString() + ", but found " + fields.size());
        }
        return getFieldValue(fields.get(0), object);
    }

    public static String getFieldValue(Field field, Object object) {
        try {
            return String.valueOf(field.get(object));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(getFieldsWithAnnotation(FHTDevice.class, WidgetTemperatureField.class));
    }
}
