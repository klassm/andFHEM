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

import li.klass.fhem.resources.ResourceIdMapper;

public class XmlDeviceViewItem implements DeviceViewItem {
    private final String name;
    private final String value;
    private final String showAfter;
    private final boolean isShowInDetail;
    private final boolean isShowInOverview;
    private final ResourceIdMapper resourceIdMapper;

    public XmlDeviceViewItem(String name, String value, String showAfter, boolean isShowInDetail,
                             boolean isShowInOverview, ResourceIdMapper resourceIdMapper) {
        this.name = name;
        this.value = value;
        this.showAfter = showAfter;
        this.isShowInDetail = isShowInDetail;
        this.isShowInOverview = isShowInOverview;
        this.resourceIdMapper = resourceIdMapper;
    }

    @Override
    public String getName() {
        return name;
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
    public String getDescription(Context context) {
        return context.getString(resourceIdMapper.getId());
    }

    @Override
    public String toString() {
        return "XmlDeviceViewItem{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", showAfter='" + showAfter + '\'' +
                ", isShowInDetail=" + isShowInDetail +
                ", isShowInOverview=" + isShowInOverview +
                ", resourceIdMapper=" + resourceIdMapper +
                '}';
    }
}
