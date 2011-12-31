package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.OregonDeviceDetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.OregonDevice;

public class OregonAdapter extends DeviceAdapter<OregonDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return OregonDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, OregonDevice device) {

        View view = layoutInflater.inflate(R.layout.room_detail_oregon, null);

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

        return view;
    }


    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_oregon;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final OregonDevice device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowForecast, R.id.forecast, device.getForecast());
        setTextViewOrHideTableRow(view, R.id.tableRowPressure, R.id.pressure, device.getPressure());
        setTextViewOrHideTableRow(view, R.id.tableRowDewpoint, R.id.pressure, device.getDewpoint());
        setTextViewOrHideTableRow(view, R.id.tableRowBattery, R.id.battery, device.getBattery());
        setTextViewOrHideTableRow(view, R.id.tableRowRainRate, R.id.rainRate, device.getRainRate());
        setTextViewOrHideTableRow(view, R.id.tableRowRainTotal, R.id.rainTotal, device.getRainTotal());
        setTextViewOrHideTableRow(view, R.id.tableRowWindAvg, R.id.windAvgSpeed, device.getWindAvgSpeed());
        setTextViewOrHideTableRow(view, R.id.tableRowWindDirection, R.id.windDirection, device.getWindDirection());
        setTextViewOrHideTableRow(view, R.id.tableRowWindSpeed, R.id.windSpeed, device.getWindSpeed());
        setTextViewOrHideTableRow(view, R.id.tableRowUVValue, R.id.uvValue, device.getUvValue());
        setTextViewOrHideTableRow(view, R.id.tableRowUVRisk, R.id.uvRisk, device.getUvRisk());


        hideIfNull(view, R.id.graphLayout, device.getFileLog());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, OregonDevice.COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, OregonDevice.COLUMN_SPEC_HUMIDITY);

        createPlotButton(context, view, R.id.pressureGraph, device.getPressure(),
                device, R.string.yAxisPressure, OregonDevice.COLUMN_SPEC_PRESSURE);

        createPlotButton(context, view, R.id.rainRateGraph, device.getRainRate(),
                device, R.string.yAxisRainRate, OregonDevice.COLUMN_SPEC_RAIN_RATE);

        createPlotButton(context, view, R.id.rainTotalGraph, device.getRainTotal(),
                device, R.string.yAxisRainTotal, OregonDevice.COLUMN_SPEC_RAIN_TOTAL);

        createPlotButton(context, view, R.id.windSpeedGraph, device.getWindSpeed(),
                device, R.string.yAxisWindSpeed, OregonDevice.COLUMN_SPEC_WIND_SPEED);

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, OregonDeviceDetailActivity.class);
        return intent;
    }

}
