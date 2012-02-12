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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.adapter.devices.core.HOLDevice;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;

public class HOLAdapter extends DeviceListOnlyAdapter<HOLDevice> {
    @Override
    protected int getOverviewLayout(HOLDevice device) {
        return R.layout.room_detail_hol;
    }

    @Override
    protected void fillDeviceOverviewView(View view, final HOLDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());

        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        switchButton.setChecked(device.isOn());
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_TOGGLE_STATE);
                intent.putExtras(new Bundle());
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                AndFHEMApplication.getContext().startService(intent);
            }
        });
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return HOLDevice.class;
    }
}
