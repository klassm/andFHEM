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

import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.CULFHTTKDevice;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.TRXDevice;

public class TRXAdapter extends DeviceListOnlyAdapter<TRXDevice> {
    @Override
    public int getOverviewLayout(TRXDevice device) {
        return R.layout.room_detail_trx;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return TRXDevice.class;
    }

    @Override
    protected void fillDeviceOverviewView(View view, TRXDevice device) {

        setTextView(view, R.id.deviceName, device.getAliasOrName());

        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());
    }
}
