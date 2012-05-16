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
import li.klass.fhem.domain.OregonDevice;

public class OregonAdapter extends DeviceDetailAvailableAdapter<OregonDevice> {
    @Override
    protected void fillDeviceOverviewView(View view, OregonDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowForecast, R.id.forecast, device.getForecast());
        setTextViewOrHideTableRow(view, R.id.tableRowRainRate, R.id.rainRate, device.getRainRate());
        setTextViewOrHideTableRow(view, R.id.tableRowRainTotal, R.id.rainTotal, device.getRainTotal());
        setTextViewOrHideTableRow(view, R.id.tableRowWindAvg, R.id.windAvgSpeed, device.getWindAvgSpeed());
        setTextViewOrHideTableRow(view, R.id.tableRowWindDirection, R.id.windDirection, device.getWindDirection());
        setTextViewOrHideTableRow(view, R.id.tableRowWindSpeed, R.id.windSpeed, device.getWindSpeed());
        setTextViewOrHideTableRow(view, R.id.tableRowUVValue, R.id.uvValue, device.getUvValue());
        setTextViewOrHideTableRow(view, R.id.tableRowUVRisk, R.id.uvRisk, device.getUvRisk());
    }

    @Override
    protected void fillDeviceDetailView(final Context context, View view, final OregonDevice device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowForecast, R.id.forecast, device.getForecast());
        setTextViewOrHideTableRow(view, R.id.tableRowPressure, R.id.pressure, device.getPressure());
        setTextViewOrHideTableRow(view, R.id.tableRowDewpoint, R.id.dewpoint, device.getDewpoint());
        setTextViewOrHideTableRow(view, R.id.tableRowBattery, R.id.battery, device.getBattery());
        setTextViewOrHideTableRow(view, R.id.tableRowRainRate, R.id.rainRate, device.getRainRate());
        setTextViewOrHideTableRow(view, R.id.tableRowRainTotal, R.id.rainTotal, device.getRainTotal());
        setTextViewOrHideTableRow(view, R.id.tableRowWindAvg, R.id.windAvgSpeed, device.getWindAvgSpeed());
        setTextViewOrHideTableRow(view, R.id.tableRowWindDirection, R.id.windDirection, device.getWindDirection());
        setTextViewOrHideTableRow(view, R.id.tableRowWindSpeed, R.id.windSpeed, device.getWindSpeed());
        setTextViewOrHideTableRow(view, R.id.tableRowUVValue, R.id.uvValue, device.getUvValue());
        setTextViewOrHideTableRow(view, R.id.tableRowUVRisk, R.id.uvRisk, device.getUvRisk());

        fillGraphButtonAndHideIfNull(context, view, R.id.temperatureGraph, device,
                device.getDeviceChartForButtonStringId(R.string.temperatureGraph));
        fillGraphButtonAndHideIfNull(context, view, R.id.humidityGraph, device,
                device.getDeviceChartForButtonStringId(R.string.humidityGraph));
        fillGraphButtonAndHideIfNull(context, view, R.id.pressureGraph, device,
                device.getDeviceChartForButtonStringId(R.string.pressureGraph));
        fillGraphButtonAndHideIfNull(context, view, R.id.rainRateGraph, device,
                device.getDeviceChartForButtonStringId(R.string.rainRateGraph));
        fillGraphButtonAndHideIfNull(context, view, R.id.rainTotalGraph, device,
                device.getDeviceChartForButtonStringId(R.string.rainTotal));
        fillGraphButtonAndHideIfNull(context, view, R.id.windSpeedGraph, device,
                device.getDeviceChartForButtonStringId(R.string.windSpeed));
    }


    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return OregonDevice.class;
    }

    @Override
    public int getOverviewLayout(OregonDevice device) {
        return R.layout.room_detail_oregon;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_oregon;
    }
}
