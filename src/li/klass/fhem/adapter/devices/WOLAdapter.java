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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.WOLDeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.WOLDevice;

public class WOLAdapter extends DeviceDetailAvailableAdapter<WOLDevice> {
    @Override
    protected void fillDeviceOverviewView(View view, WOLDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        int isRunningText = device.isRunning() ? R.string.on : R.string.off;
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, isRunningText);
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return WOLDeviceDetailActivity.class;
    }

    @Override
    protected void fillDeviceDetailView(final Context context, View view, final WOLDevice device) {
        int isRunningText = device.isRunning() ? R.string.on : R.string.off;
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, isRunningText);
        setTextViewOrHideTableRow(view, R.id.tableRowMac, R.id.mac, device.getMac());
        setTextViewOrHideTableRow(view, R.id.tableRowIP, R.id.ip, device.getIp());

        Button wakeButton = (Button) view.findViewById(R.id.wakeButton);
        wakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_WAKE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                context.startService(intent);
            }
        });

        Button refreshButton = (Button) view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_REFRESH_STATE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                context.startService(intent);
                
                context.startService(new Intent(Actions.DO_UPDATE));
            }
        });
    }

    @Override
    public int getOverviewLayout(WOLDevice device) {
        return R.layout.room_detail_wol;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_wol;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return WOLDevice.class;
    }
}
