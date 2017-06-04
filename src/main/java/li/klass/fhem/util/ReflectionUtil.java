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

package li.klass.fhem.util;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ReflectionUtil {
    public static final String TAG = ReflectionUtil.class.getName();

    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> result = newArrayList();
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
        if (fields.size() != 1) {
            throw new IllegalArgumentException("expected exactly one occurence for annotation " + annotation.getName() +
                    " in object " + object.toString() + ", but found " + fields.size());
        }
        return getFieldValueAsString(object, fields.get(0));
    }

    public static <T extends Annotation> String getValueAndDescriptionForAnnotation(Object object, Class<T> annotationCls, Context context) {
        List<Field> fields = getFieldsWithAnnotation(object.getClass(), annotationCls);
        if (fields.size() == 0) return null;

        if (fields.size() != 1) {
            throw new IllegalArgumentException("expected exactly one occurence for annotationCls " + annotationCls.getName() +
                    " in object " + object.toString() + ", but found " + fields.size());
        }
        Field field = fields.get(0);
        field.setAccessible(true);
        T annotation = field.getAnnotation(annotationCls);

        String fieldValue = "";
        try {
            Object value = field.get(object);
            fieldValue = value == null ? "" : value.toString();
        } catch (IllegalAccessException e) {
            Log.e(ReflectionUtil.class.getName(), "this should never ever happen", e);
        }

        try {
            Method descriptionMethod = annotationCls.getDeclaredMethod("description");
            Object result = descriptionMethod.invoke(annotation);
            if (result != null) {
                int stringId = Integer.valueOf(result.toString());
                if (stringId != -1) fieldValue += " " + context.getString(stringId);
            }
        } catch (Exception e) {
            // don't care. This might be a common case, unfortunately ...
        }

        return fieldValue;
    }


    public static Object getFieldValue(Object object, Field field) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "cannot read field " + field.getName(), e);
            return null;
        }
    }

    public static void setFieldValue(Object object, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "cannot set field " + field.getName(), e);
        }
    }

    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            Log.e(TAG, "cannot set field " + fieldName, e);
        }
    }

    public static String getFieldValueAsString(Object object, Field field) {
        Object value = getFieldValue(object, field);
        return String.valueOf(value);
    }

    public static String methodNameToFieldName(String methodName) {
        if (methodName.startsWith("get")) {
            methodName = methodName.substring("get".length());
        }
        char firstChar = methodName.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            methodName = ((char) (firstChar - 'A' + 'a')) + methodName.substring(1);
        }
        return methodName;
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field;
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
        }
        return null;
    }

    public static List<Field> getAllDeclaredFields(Class<?> type) {
        return getAllDeclaredFields(Lists.<Field>newArrayList(), type);
    }

    private static List<Field> getAllDeclaredFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllDeclaredFields(fields, type.getSuperclass());
        }

        return fields;
    }


    public static List<Method> getAllDeclaredMethods(Class<?> type) {
        return getAllDeclaredMethods(Lists.<Method>newArrayList(), type);
    }

    private static List<Method> getAllDeclaredMethods(List<Method> methods, Class<?> type) {
        methods.addAll(Arrays.asList(type.getDeclaredMethods()));

        if (type.getSuperclass() != null) {
            methods = getAllDeclaredMethods(methods, type.getSuperclass());
        }

        return methods;
    }


    public static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("cannot find " + className, e);
        }
    }
}
