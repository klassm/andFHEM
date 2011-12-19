package li.klass.fhem.activities.deviceDetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.DeviceAdapter;
import li.klass.fhem.adapter.devices.DeviceAdapterProvider;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;

public abstract class DeviceDetailActivity<D extends Device> extends Activity {

    protected String deviceName;
    protected String room;
    
    protected D device;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        deviceName = extras.getString("deviceName");
        room = extras.getString("room");

        RoomDeviceList roomDeviceList = FHEMService.INSTANCE.deviceListForRoom(room, false);
        device = roomDeviceList.getDeviceFor(deviceName);
        
        if (device == null) {
            setResult(RESULT_CANCELED);
            return;
        }

        DeviceAdapter<D> adapter = (DeviceAdapter<D>) DeviceAdapterProvider.INSTANCE.getAdapterFor(device);
        setContentView(adapter.getDetailView(LayoutInflater.from(this), device));

        String deviceDetailPrefix = getResources().getString(R.string.deviceDetailPrefix);
        setTitle(deviceDetailPrefix + " " + deviceName);
    }
}
