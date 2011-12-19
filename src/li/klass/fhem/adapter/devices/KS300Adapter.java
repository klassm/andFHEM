package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.KS300DetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.KS300Device;

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
        return R.layout.plot;
    }

    @Override
    public Class<? extends Activity> getDetailActivityClass() {
        return KS300DetailActivity.class;
    }

    @Override
    protected View getDeviceDetailView(LayoutInflater layoutInflater, KS300Device device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

//        TextView wind = (TextView) view.findViewById(R.id.wind);
//        TextView humidity = (TextView) view.findViewById(R.id.humidity);
//        TextView rain = (TextView) view.findViewById(R.id.rain);
//        TextView temperature = (TextView) view.findViewById(R.id.temperature);
//
//        wind.setText(device.getWind());
//        temperature.setText(device.getTemperature());
//        humidity.setText(device.getHumidity());
//        rain.setText(device.getRain());

        return view;
    }
}
