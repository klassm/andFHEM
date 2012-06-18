/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.widget.TableLayout;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.genericui.OnOffActionRow;
import li.klass.fhem.adapter.devices.genericui.ToggleActionRow;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.ToggleableDevice;

public abstract class ToggleableAdapter<D extends Device<D>> extends GenericDeviceAdapter<D> {
    public ToggleableAdapter(Class<D> deviceClass) {
        super(deviceClass);
    }

    protected <T extends ToggleableDevice<D>> void addOverviewSwitchActionRow(Context context, T device, TableLayout layout) {
        if (device.isOnOffDevice()) {
            addSwitchActionRow(context, device, layout, OnOffActionRow.LAYOUT_OVERVIEW);
        } else {
            addSwitchActionRow(context, device, layout, ToggleActionRow.LAYOUT_OVERVIEW);
        }
    }

    protected <T extends ToggleableDevice<D>> void addDetailSwitchActionRow(Context context, T device, TableLayout layout) {
        if (device.isOnOffDevice()) {
            addSwitchActionRow(context, device, layout, OnOffActionRow.LAYOUT_DETAIL);
        } else {
            addSwitchActionRow(context, device, layout, ToggleActionRow.LAYOUT_DETAIL);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ToggleableDevice<D>> void addSwitchActionRow(Context context, T device, TableLayout layout, int rowId) {
        if (device.isOnOffDevice()) {
            layout.addView(new OnOffActionRow<T>(device.getName(), rowId)
                    .createRow(context, inflater, (T) device));
        } else {
            layout.addView(new ToggleActionRow<T>(device.getName(), rowId)
                    .createRow(context, inflater, (T) device));
        }
    }
}
