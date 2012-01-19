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

package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.LGTVDevice;

public class LGTVAdapter extends DeviceListOnlyAdapter<LGTVDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return LGTVDevice.class;
    }

    @Override
    protected View getDeviceOverviewView(LayoutInflater layoutInflater, LGTVDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_lgtv, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowPower, R.id.power, device.getPower());
        setTextViewOrHideTableRow(view, R.id.tableRowAudio, R.id.audio, device.getAudio());
        setTextViewOrHideTableRow(view, R.id.tableRowInput, R.id.input, device.getInput());

        return view;
    }
}
