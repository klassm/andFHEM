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

package li.klass.fhem.adapter.devices.core.showFieldAnnotation;

import java.lang.reflect.Method;

import li.klass.fhem.domain.genericview.ShowField;

public class AnnotatedDeviceClassMethod extends AnnotatedDeviceClassItem {
    public final Method method;
    private final String sortName;

    public AnnotatedDeviceClassMethod(Method method) {
        this.method = method;
        method.setAccessible(true);

        String name = method.getName();
        name = getterNameToName(name);

        this.sortName = name;
    }

    static String getterNameToName(String name) {
        if (! name.startsWith("get")) return name;

        name = name.replace("get", "");

        int firstChar = name.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            firstChar = firstChar - 'A' + 'a';
        }

        name = "" + ((char) firstChar) + name.substring(1);
        return name;
    }

    @Override
    public String getName() {
        return sortName;
    }

    @Override
    public String getValueFor(Object object) {
        try {
            Object value = method.invoke(object);
            if (value == null) return null;

            return String.valueOf(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ShowField getShowFieldAnnotation() {
        if (! method.isAnnotationPresent(ShowField.class)) return null;
        return method.getAnnotation(ShowField.class);
    }
}
