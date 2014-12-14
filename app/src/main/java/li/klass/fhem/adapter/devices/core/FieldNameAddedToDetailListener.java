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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.ToggleableDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.ALL;

public abstract class FieldNameAddedToDetailListener<T extends Device> {
    protected enum NotificationDeviceType {
        ALL,
        TOGGLEABLE_AND_NOT_DIMMABLE,
        DIMMER
    }

    private NotificationDeviceType notificationDeviceType;

    protected FieldNameAddedToDetailListener() {
        notificationDeviceType = ALL;
    }

    protected FieldNameAddedToDetailListener(NotificationDeviceType notificationDeviceType) {
        this.notificationDeviceType = notificationDeviceType;
    }

    void setNotificationDeviceType(NotificationDeviceType notificationDeviceType) {
        this.notificationDeviceType = notificationDeviceType;
    }

    protected abstract void onFieldNameAdded(Context context, TableLayout tableLayout, String field, T device, TableRow fieldTableRow);

    public boolean supportsDevice(T device) {
        switch (notificationDeviceType) {
            case DIMMER:
                return device instanceof DimmableDevice && ((DimmableDevice) device).supportsDim();
            case TOGGLEABLE_AND_NOT_DIMMABLE:
                return device instanceof ToggleableDevice && ((ToggleableDevice) device).supportsToggle() &&
                        (!(device instanceof DimmableDevice) || !((DimmableDevice) device).supportsDim());
            case ALL:
            default:
                return true;
        }
    }
}
