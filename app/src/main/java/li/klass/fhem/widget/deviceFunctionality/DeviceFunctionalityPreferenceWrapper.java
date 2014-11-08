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

package li.klass.fhem.widget.deviceFunctionality;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.klass.fhem.domain.core.DeviceFunctionality;

public class DeviceFunctionalityPreferenceWrapper implements Comparable<DeviceFunctionalityPreferenceWrapper> {
    private final DeviceFunctionality deviceFunctionality;
    private boolean isVisible = true;

    private static final Logger LOG = LoggerFactory.getLogger(DeviceFunctionalityPreferenceWrapper.class);

    public DeviceFunctionalityPreferenceWrapper(DeviceFunctionality deviceFunctionality, boolean visible) {
        this.deviceFunctionality = deviceFunctionality;
        isVisible = visible;
    }

    public DeviceFunctionality getDeviceFunctionality() {
        return deviceFunctionality;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void invertVisibility() {
        isVisible = !isVisible;
        LOG.info("changed visibility for {} to {}", deviceFunctionality.name(), isVisible);
    }

    @Override
    public int compareTo(@NotNull DeviceFunctionalityPreferenceWrapper other) {
        return deviceFunctionality.name().compareTo(other.getDeviceFunctionality().name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceFunctionalityPreferenceWrapper that = (DeviceFunctionalityPreferenceWrapper) o;

        return deviceFunctionality == that.deviceFunctionality;

    }

    @Override
    public int hashCode() {
        return deviceFunctionality != null ? deviceFunctionality.hashCode() : 0;
    }
}
