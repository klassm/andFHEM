package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.OregonDeviceDetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.OregonDevice;
import li.klass.fhem.graph.TimePlot;

public class OregonAdapter extends DeviceAdapter<OregonDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return OregonDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, OregonDevice device) {

        View view = layoutInflater.inflate(R.layout.room_detail_oregon, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getAliasOrName());

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView humidity = (TextView) view.findViewById(R.id.humidity);
        humidity.setText(device.getHumidity());

        TextView forecast = (TextView) view.findViewById(R.id.forecast);
        forecast.setText(device.getForecast());

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

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView humidity = (TextView) view.findViewById(R.id.humidity);
        humidity.setText(device.getHumidity());

        TextView forecast = (TextView) view.findViewById(R.id.forecast);
        forecast.setText(device.getForecast());

        TextView pressure = (TextView) view.findViewById(R.id.pressure);
        pressure.setText(device.getPressure());

        TextView dewpoint = (TextView) view.findViewById(R.id.dewpoint);
        dewpoint.setText(device.getDewpoint());

        TextView battery = (TextView) view.findViewById(R.id.battery);
        battery.setText(device.getBattery());



        if (device.getFileLog() == null) {
            LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphLayout);
            graphLayout.setVisibility(View.INVISIBLE);
        }

        Button temperatureGraph = (Button) view.findViewById(R.id.temperatureGraph);
        temperatureGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisTemperature);
                TimePlot.INSTANCE.execute(context, device, yTitle, OregonDevice.COLUMN_SPEC_TEMPERATURE);
            }
        });

        Button humidityGraph = (Button) view.findViewById(R.id.humidityGraph);
        humidityGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisHumidity);
                TimePlot.INSTANCE.execute(context, device, yTitle, OregonDevice.COLUMN_SPEC_HUMIDITY);
            }
        });

        Button pressureGraph = (Button) view.findViewById(R.id.pressureGraph);
        pressureGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisPressure);
                TimePlot.INSTANCE.execute(context, device, yTitle, OregonDevice.COLUMN_SPEC_PRESSURE);
            }
        });

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, OregonDeviceDetailActivity.class);
        return intent;
    }

}
