package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.FHTDeviceDetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.graph.TimePlot;

public class FHTAdapter extends DeviceAdapter<FHTDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, FHTDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_fht, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        TextView actuator = (TextView) view.findViewById(R.id.actuator);


        deviceName.setText(device.getAliasOrName());
        temperature.setText(device.getTemperature());
        actuator.setText(device.getActuator());

        return view;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return FHTDevice.class;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_fht;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final FHTDevice device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        TextView actuator = (TextView) view.findViewById(R.id.actuator);
        TextView desiredTemperature = (TextView) view.findViewById(R.id.desiredTemperature);
        TextView warnings = (TextView) view.findViewById(R.id.warnings);

        temperature.setText(device.getTemperature());
        actuator.setText(device.getActuator());
        desiredTemperature.setText(device.getDesiredTemp());
        warnings.setText(device.getWarnings());

        if (device.getFileLog() == null) {
            LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphLayout);
            graphLayout.setVisibility(View.INVISIBLE);
        }

        Button temperatureGraph = (Button) view.findViewById(R.id.temperatureGraph);
        temperatureGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisTemperature);
                TimePlot.INSTANCE.execute(context, device, yTitle, FHTDevice.COLUMN_SPEC_TEMPERATURE);
            }
        });

        Button humidityGraph = (Button) view.findViewById(R.id.actuatorGraph);
        humidityGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yTitle = context.getResources().getString(R.string.yAxisActuator);
                TimePlot.INSTANCE.execute(context, device, yTitle, FHTDevice.COLUMN_SPEC_ACTUATOR);
            }
        });

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, FHTDeviceDetailActivity.class);

        return intent;
    }
}
