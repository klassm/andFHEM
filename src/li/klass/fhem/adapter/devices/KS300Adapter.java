package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.KS300DeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.KS300Device;

public class KS300Adapter extends DeviceDetailAvailableAdapter<KS300Device> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, KS300Device device) {
        View view = layoutInflater.inflate(R.layout.room_detail_ks300, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowRain, R.id.rain, device.getRain());

        return view;
    }

    @Override
    protected void fillDeviceDetailView(final Context context, View view, final KS300Device device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowRain, R.id.rain, device.getRain());
        setTextViewOrHideTableRow(view, R.id.tableRowIsRaining, R.id.isRaining, device.getRaining());
        setTextViewOrHideTableRow(view, R.id.tableRowAvgDay, R.id.avgDay, device.getAverageDay());
        setTextViewOrHideTableRow(view, R.id.tableRowAvgMonth, R.id.avgMonth, device.getAverageMonth());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, KS300Device.COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, KS300Device.COLUMN_SPEC_HUMIDITY);

        createPlotButton(context, view, R.id.windGraph, device.getWind(),
                device, R.string.yAxisWind, KS300Device.COLUMN_SPEC_WIND);

        createPlotButton(context, view, R.id.rainGraph, device.getRain(),
                device, R.string.yAxisRain, KS300Device.COLUMN_SPEC_RAIN);

    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_ks300;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return KS300Device.class;
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return KS300DeviceDetailActivity.class;
    }
}
