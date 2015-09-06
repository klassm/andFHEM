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

import java.lang.reflect.Method;
import java.util.Locale;

import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.genericview.ShowFieldCache;

public class AnnotatedDeviceViewMethod extends AnnotatedDeviceViewItem {
    public final Method method;
    private final String sortName;
    private ShowField showField;

    public AnnotatedDeviceViewMethod(Method method) {
        this.method = method;
        method.setAccessible(true);

        this.sortName = getterNameToName(method.getName());

        this.showField = method.getAnnotation(ShowField.class);
        if (showField != null) {
            showField = new ShowFieldCache(showField);
        }
    }

    static String getterNameToName(String name) {
        if (!name.startsWith("get")) return name;

        return name
                .replace("get", "")
                .toLowerCase(Locale.getDefault());
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
    public String getSortKey() {
        return sortName;
    }

    @Override
    public ShowField getShowFieldAnnotation() {
        return showField;
    }
}
