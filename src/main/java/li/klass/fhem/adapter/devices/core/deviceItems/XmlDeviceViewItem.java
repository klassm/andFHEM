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

import java.util.Locale;

import li.klass.fhem.service.deviceConfiguration.DeviceDescMapping;

public class XmlDeviceViewItem implements DeviceViewItem {
    private final String key;
    private final String value;
    private final String showAfter;
    private final boolean isShowInDetail;
    private final boolean isShowInOverview;
    private final String desc;

    public XmlDeviceViewItem(String key, String desc, String value, String showAfter, boolean isShowInDetail,
                             boolean isShowInOverview) {
        this.key = key;
        this.value = value;
        this.showAfter = showAfter;
        this.isShowInDetail = isShowInDetail;
        this.isShowInOverview = isShowInOverview;
        this.desc = desc;
    }

    @Override
    public String getName(DeviceDescMapping deviceDescMapping) {
        return desc;
    }

    @Override
    public String getValueFor(Object object) {
        return value;
    }

    @Override
    public String getShowAfterValue() {
        return showAfter;
    }

    @Override
    public boolean isShowInDetail() {
        return isShowInDetail;
    }

    @Override
    public boolean isShowInOverview() {
        return isShowInOverview;
    }

    @Override
    public String getSortKey() {
        return key.toLowerCase(Locale.getDefault());
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "XmlDeviceViewItem{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", showAfter='" + showAfter + '\'' +
                ", isShowInDetail=" + isShowInDetail +
                ", isShowInOverview=" + isShowInOverview +
                '}';
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
