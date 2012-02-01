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
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;

public abstract class DeviceDetailAvailableAdapter<D extends Device<D>> extends DeviceAdapter<D> {
    @Override
    public boolean supportsDetailView(Device device) {
        return true;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        return intent;
    }

    @Override
    protected final View getDeviceDetailView(Context context, D device) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(getDetailViewLayout(), null);
        fillDeviceDetailView(context, view, device);

        setTextViewOrHideTableRow(view, R.id.tableRowDeviceName, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowDef, R.id.def, device.getDefinition());
        setTextViewOrHideTableRow(view, R.id.tableRowRoom, R.id.room, device.getRoom());
        setTextViewOrHideTableRow(view, R.id.tableRowMeasured, R.id.measured, device.getMeasured());

        hideIfNull(view, R.id.graphLayout, device.getFileLog());

        return view;
    }
    
    protected abstract void fillDeviceDetailView(Context context, View view, D device);
}
