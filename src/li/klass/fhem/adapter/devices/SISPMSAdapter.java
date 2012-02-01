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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.SISPMSDevice;

public class SISPMSAdapter extends DeviceListOnlyAdapter<SISPMSDevice> {

    @Override
    public void fillDeviceOverviewView(View view, final SISPMSDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());

        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_TOGGLE_STATE);
                intent.putExtras(new Bundle());
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                AndFHEMApplication.getContext().startService(intent);
            }
        });
        switchButton.setChecked(device.isOn());
    }

    @Override
    public int getOverviewLayout(SISPMSDevice device) {
        return R.layout.room_detail_sispms;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return SISPMSDevice.class;
    }
}
