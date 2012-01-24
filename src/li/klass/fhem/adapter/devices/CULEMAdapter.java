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
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.CULEMDeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.CULEMDevice;
import li.klass.fhem.domain.Device;

public class CULEMAdapter extends DeviceDetailAvailableAdapter<CULEMDevice> {
    @Override
    public int getOverviewLayout(CULEMDevice device) {
        return R.layout.room_detail_culem;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULEMDevice.class;
    }

    @Override
    protected void fillDeviceOverviewView(View view, CULEMDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowCurrentUsage, R.id.currentUsage, device.getCurrentUsage());
        setTextViewOrHideTableRow(view, R.id.tableRowDayUsage, R.id.dayUsage, device.getDayUsage());
        setTextViewOrHideTableRow(view, R.id.tableRowMonthUsage, R.id.monthUsage, device.getMonthUsage());
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_culem;
    }

    @Override
    protected void fillDeviceDetailView(Context context, View view, CULEMDevice device) {
        setTextViewOrHideTableRow(view, R.id.tableRowCurrentUsage, R.id.currentUsage, device.getCurrentUsage());
        setTextViewOrHideTableRow(view, R.id.tableRowDayUsage, R.id.dayUsage, device.getDayUsage());
        setTextViewOrHideTableRow(view, R.id.tableRowMonthUsage, R.id.monthUsage, device.getMonthUsage());

        createPlotButton(context, view, R.id.usageGraph, device.getCurrentUsage(),
                device, R.string.yAxisUsage, CULEMDevice.COLUMN_SPEC_CURRENT_USAGE);
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return CULEMDeviceDetailActivity.class;
    }
}
