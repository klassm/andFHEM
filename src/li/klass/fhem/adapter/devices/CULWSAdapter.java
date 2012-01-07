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
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.CULWSDeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.CULWSDevice;
import li.klass.fhem.domain.Device;

public class CULWSAdapter extends DeviceDetailAvailableAdapter<CULWSDevice> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, CULWSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_culws, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());

        return view;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_culws;
    }

    @Override
    protected void fillDeviceDetailView(Context context, View view, CULWSDevice device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, CULWSDevice.COLUMN_SPEC_TEMPERATURE);
        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, CULWSDevice.COLUMN_SPEC_HUMIDITY);
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULWSDevice.class;
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return CULWSDeviceDetailActivity.class;
    }
}
