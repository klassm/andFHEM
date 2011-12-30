package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.CULWSDeviceDetailActivity;
import li.klass.fhem.domain.CULWSDevice;
import li.klass.fhem.domain.Device;
import li.klass.fhem.graph.TimePlot;

public class CULWSAdapter extends DeviceAdapter<CULWSDevice> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, CULWSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_culws, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getAliasOrName());

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView battery = (TextView) view.findViewById(R.id.humidity);
        battery.setText(device.getHumidity());

        return view;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_culws;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final CULWSDevice device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView battery = (TextView) view.findViewById(R.id.humidity);
        battery.setText(device.getHumidity());

        if (device.getFileLog() == null) {
            LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphLayout);
            graphLayout.setVisibility(View.INVISIBLE);
        }

        Button temperatureGraph = (Button) view.findViewById(R.id.temperatureGraph);
        temperatureGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisTemperature);
                TimePlot.INSTANCE.execute(context, device, yTitle, CULWSDevice.COLUMN_SPEC_TEMPERATURE);
            }
        });

        Button humidityGraph = (Button) view.findViewById(R.id.humidityGraph);
        humidityGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisActuator);
                TimePlot.INSTANCE.execute(context, device, yTitle, CULWSDevice.COLUMN_SPEC_HUMIDITY);
            }
        });

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, CULWSDeviceDetailActivity.class);
        return intent;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULWSDevice.class;
    }
}
