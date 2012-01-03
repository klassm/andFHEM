package li.klass.fhem.activities.devicelist;

import android.os.Bundle;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.rooms.RoomDetailAdapter;
import li.klass.fhem.domain.*;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.service.device.FS20Service;
import li.klass.fhem.service.device.SISPMSService;
import li.klass.fhem.widget.NestedListView;

public abstract class DeviceListActivity extends BaseActivity<RoomDetailAdapter> {

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
                    Device<?> device = (Device<?>) child;
                    DeviceAdapter<? extends Device<?>> adapter = DeviceType.getAdapterFor(device);
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

    public void onFS20Click(final View view) {

        RoomListService.INSTANCE.getAllRoomsDeviceList(this, false, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {

                String deviceName = (String) view.getTag();
                FS20Device device = roomDeviceList.getDeviceFor(deviceName);
                FS20Service.INSTANCE.toggleState(DeviceListActivity.this, device, updateOnSuccessAction);

                update(false);
            }
        });


    }

    public void onSISPMSClick(final View view) {
        RoomListService.INSTANCE.getAllRoomsDeviceList(this, false, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {

                String deviceName = (String) view.getTag();
                SISPMSDevice device = roomDeviceList.getDeviceFor(deviceName);
                SISPMSService.INSTANCE.toggleState(DeviceListActivity.this, device, updateOnSuccessAction);

                update(false);
            }
        });
    }
}
