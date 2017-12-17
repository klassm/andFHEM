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

import javax.inject.Inject;

import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.domain.core.FhemDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.ALL;

public abstract class FieldNameAddedToDetailListener {
    @Inject
    OnOffBehavior onOffBehavior;

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

    protected abstract void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, String connectionId, TableRow fieldTableRow);

    public boolean supportsDevice(FhemDevice device) {
        switch (notificationDeviceType) {
            case DIMMER:
                return DimmableBehavior.Companion.supports(device.getXmlListDevice());
            case TOGGLEABLE_AND_NOT_DIMMABLE:
                return onOffBehavior.supports(device) &&
                        !DimmableBehavior.Companion.supports(device.getXmlListDevice());
            case ALL:
            default:
                return true;
        }
    }
}
