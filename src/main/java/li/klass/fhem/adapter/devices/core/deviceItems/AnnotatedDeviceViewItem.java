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

import android.content.Context;

import com.google.common.base.Strings;

import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.update.backend.deviceConfiguration.DeviceDescMapping;

public abstract class AnnotatedDeviceViewItem implements DeviceViewItem {

    public abstract ShowField getShowFieldAnnotation();

    @Override
    public String getShowAfterValue() {
        ShowField annotation = getShowFieldAnnotation();
        if (annotation == null) return null;

        String showAfter = annotation.showAfter();
        if (Strings.isNullOrEmpty(showAfter)) return null;

        return showAfter;
    }

    @Override
    public String getName(DeviceDescMapping deviceDescMapping, Context context) {
        return deviceDescMapping.descFor(getShowFieldAnnotation().description(), context);
    }

    @Override
    public boolean isShowInDetail() {
        return getShowFieldAnnotation().showInDetail();
    }

    @Override
    public boolean isShowInOverview() {
        return getShowFieldAnnotation().showInOverview();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "::" + getSortKey();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceViewItem)) {
            return false;
        }
        DeviceViewItem other = (DeviceViewItem) o;
        return other.getSortKey().equals(getSortKey());
    }

    @Override
    public int hashCode() {
        return getSortKey().hashCode();
    }
}
