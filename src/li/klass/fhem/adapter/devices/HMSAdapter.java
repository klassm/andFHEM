package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.HMSDeviceDetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.HMSDevice;
import li.klass.fhem.graph.TimePlot;

public class HMSAdapter extends DeviceAdapter<HMSDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, HMSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_hms, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getName());

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView humidity = (TextView) view.findViewById(R.id.humidity);
        humidity.setText(device.getHumidity());

        return view;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_hms;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final HMSDevice device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView battery = (TextView) view.findViewById(R.id.battery);
        battery.setText(device.getBattery());

        TextView humidity = (TextView) view.findViewById(R.id.humidity);
        humidity.setText(device.getHumidity());

        Button temperatureGraph = (Button) view.findViewById(R.id.temperatureGraph);
        temperatureGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisTemperature);
                TimePlot.INSTANCE.execute(context, device, yTitle, HMSDevice.COLUMN_SPEC_TEMPERATURE);
            }
        });

        Button humidityGraph = (Button) view.findViewById(R.id.humidityGraph);
        humidityGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisActuator);
                TimePlot.INSTANCE.execute(context, device, yTitle, HMSDevice.COLUMN_SPEC_HUMIDITY);
            }
        });

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, HMSDeviceDetailActivity.class);
        return intent;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return HMSDevice.class;
    }
}
