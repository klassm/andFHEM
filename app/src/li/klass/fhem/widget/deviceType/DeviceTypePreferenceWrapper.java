/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.widget.deviceType;

import li.klass.fhem.domain.core.DeviceType;

public class DeviceTypePreferenceWrapper implements Comparable<DeviceTypePreferenceWrapper> {
    private final DeviceType deviceType;
    private boolean isVisible = true;

    public DeviceTypePreferenceWrapper(DeviceType deviceType, boolean visible) {
        this.deviceType = deviceType;
        isVisible = visible;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void invertVisibility() {
        isVisible = ! isVisible;
    }

    @Override
    public int compareTo(DeviceTypePreferenceWrapper other) {
        return deviceType.name().compareTo(other.getDeviceType().name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceTypePreferenceWrapper that = (DeviceTypePreferenceWrapper) o;

        if (deviceType != that.deviceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return deviceType != null ? deviceType.hashCode() : 0;
    }
}
