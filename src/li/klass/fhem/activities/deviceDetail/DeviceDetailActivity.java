package li.klass.fhem.activities.deviceDetail;

import android.content.Intent;
import android.os.Bundle;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

public abstract class DeviceDetailActivity<D extends Device> extends BaseActivity<DeviceAdapter<D>> {

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
        update(false);
        return adapter;
    }

    @Override
    protected void setLayout() {
    }

    @Override
    public void update(boolean doUpdate) {
        RoomListService.INSTANCE.getAllRoomsDeviceList(this, doUpdate, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                D device = roomDeviceList.getDeviceFor(deviceName);
                DeviceAdapter<D> adapter = DeviceType.getAdapterFor(device);
                setContentView(adapter.getDetailView(DeviceDetailActivity.this, device));
            }
        });
    }
}
