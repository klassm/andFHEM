package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.KS300DetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.KS300Device;
import li.klass.fhem.graph.TimePlot;

public class KS300Adapter extends DeviceAdapter<KS300Device> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, KS300Device device) {
        View view = layoutInflater.inflate(R.layout.room_detail_ks300, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        TextView wind = (TextView) view.findViewById(R.id.wind);
        TextView humidity = (TextView) view.findViewById(R.id.humidity);
        TextView rain = (TextView) view.findViewById(R.id.rain);
        TextView temperature = (TextView) view.findViewById(R.id.temperature);

        deviceName.setText(device.getName());
        wind.setText(device.getWind());
        temperature.setText(device.getTemperature());
        humidity.setText(device.getHumidity());
        rain.setText(device.getRain());

        return view;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return KS300Device.class;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_ks300;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final KS300Device device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        TextView wind = (TextView) view.findViewById(R.id.wind);
        TextView humidity = (TextView) view.findViewById(R.id.humidity);
        TextView rain = (TextView) view.findViewById(R.id.rain);
        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        TextView isRaining = (TextView) view.findViewById(R.id.isRaining);
        TextView avgDay = (TextView) view.findViewById(R.id.avgDay);
        TextView avgMonth = (TextView) view.findViewById(R.id.avgMonth);

        temperature.setText(device.getTemperature());
        wind.setText(device.getWind());
        isRaining.setText(device.getRaining());
        humidity.setText(device.getHumidity());
        rain.setText(device.getRain());
        avgDay.setText(device.getAverageDay());
        avgMonth.setText(device.getAverageMonth());

        
        if (device.getFileLog() == null) {
            LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphLayout);
            graphLayout.setVisibility(View.INVISIBLE);
        }

        Button temperatureGraph = (Button) view.findViewById(R.id.temperatureGraph);
        temperatureGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisTemperature);
                TimePlot.INSTANCE.execute(context, device, yTitle, KS300Device.COLUMN_SPEC_TEMPERATURE);
            }
        });

        Button humidityGraph = (Button) view.findViewById(R.id.humidityGraph);
        humidityGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisHumidity);
                TimePlot.INSTANCE.execute(context, device, yTitle, KS300Device.COLUMN_SPEC_HUMIDITY);
            }
        });

        Button windGraph = (Button) view.findViewById(R.id.windGraph);
        windGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisWind);
                TimePlot.INSTANCE.execute(context, device, yTitle, KS300Device.COLUMN_SPEC_WIND);
            }
        });

        Button rainGraph = (Button) view.findViewById(R.id.rainGraph);
        rainGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisRain);
                TimePlot.INSTANCE.execute(context, device, yTitle, KS300Device.COLUMN_SPEC_RAIN);
            }
        });

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, KS300DetailActivity.class);

        return intent;
    }
}
