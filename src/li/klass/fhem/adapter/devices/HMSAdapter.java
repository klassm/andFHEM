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

import android.content.Context;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.HMSDevice;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

public class HMSAdapter extends DeviceDetailAvailableAdapter<HMSDevice> {
    @Override
    public void fillDeviceOverviewView(View view, HMSDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
    }

    @Override
    protected void fillDeviceDetailView(final Context context, View view, final HMSDevice device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowBattery, R.id.battery, device.getBattery());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, new ChartSeriesDescription(HMSDevice.COLUMN_SPEC_TEMPERATURE, false, true, false));

        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, HMSDevice.COLUMN_SPEC_HUMIDITY);
    }

    @Override
    public int getOverviewLayout(HMSDevice device) {
        return R.layout.room_detail_hms;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_hms;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return HMSDevice.class;
    }
}
