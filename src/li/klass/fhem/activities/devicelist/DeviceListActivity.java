package li.klass.fhem.activities.devicelist;

import android.os.Bundle;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.rooms.RoomDetailAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.SISPMSDevice;
import li.klass.fhem.service.FS20Service;
import li.klass.fhem.service.RoomListService;
import li.klass.fhem.service.SISPMSService;
import li.klass.fhem.widget.NestedListView;

public abstract class DeviceListActivity extends BaseActivity<RoomDeviceList, RoomDetailAdapter> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        update(false);
    }

    @Override
    protected RoomDetailAdapter initializeLayoutAndReturnAdapter() {
        RoomDetailAdapter roomDetailAdapter = new RoomDetailAdapter(this, new RoomDeviceList(""));
        NestedListView nestedListView = (NestedListView) findViewById(R.id.deviceMap);
        nestedListView.setAdapter(roomDetailAdapter);

        registerForContextMenu(nestedListView);

        roomDetailAdapter.addParentChildObserver(new NestedListView.NestedListViewOnClickObserver() {
            @Override
            public void onItemClick(View view, Object parent, Object child, int parentPosition, int childPosition) {
                if (child != null) {
                    Device device = (Device) child;
                    DeviceAdapter<? extends Device<?>> adapter = device.getDeviceType().getAdapter();
                    if (adapter != null && adapter.supportsDetailView()) {
                        adapter.gotoDetailView(DeviceListActivity.this, device);
                    }
                }
            }
        });

        return roomDetailAdapter;
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.room_detail);
    }

    @Override
    protected void updateData(RoomDeviceList roomDeviceList) {
        adapter.updateData(roomDeviceList);
    }

    public void onFS20Click(final View view) {
        String deviceName = (String) view.getTag();
        FS20Device device = RoomListService.INSTANCE.deviceListForAllRooms(false).getDeviceFor(deviceName);
        FS20Service.INSTANCE.toggleState(DeviceListActivity.this, device, updateOnSuccessAction);

    }

    public void onSISPMSClick(final View view) {
        SISPMSDevice device = (SISPMSDevice) view.getTag();
        SISPMSService.INSTANCE.toggleState(DeviceListActivity.this, device, updateOnSuccessAction);
        update(false);
    }
}
