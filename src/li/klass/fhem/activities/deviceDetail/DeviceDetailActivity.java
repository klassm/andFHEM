package li.klass.fhem.activities.deviceDetail;

import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.service.RoomListService;

public abstract class DeviceDetailActivity<D extends Device> extends BaseActivity<D, DeviceAdapter<D>> {

    protected String deviceName;
    protected String room;
    
    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        this.deviceName = extras.getString("deviceName");
        this.room = extras.getString("room");

        super.onCreate(savedInstanceState);

        String deviceDetailPrefix = getResources().getString(R.string.deviceDetailPrefix);
        setTitle(deviceDetailPrefix + " " + deviceName);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected DeviceAdapter<D> initializeLayoutAndReturnAdapter() {
        D device = getCurrentData(false);

        DeviceAdapter<D> adapter = DeviceType.getAdapterFor(device);
        setContentView(adapter.getDetailView(this, device));

        return adapter;
    }

    @Override
    protected void setLayout() {
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D getCurrentData(boolean refresh) {
        Device foundDevice = RoomListService.INSTANCE.deviceListForAllRooms(refresh).getDeviceFor(deviceName);
        if (foundDevice == null) {
            setResult(RESULT_OK);
            finish();
        }
        return (D) foundDevice;
    }

    @Override
    protected void updateData(D data) {
        setContentView(adapter.getDetailView(this, data));
    }
}
