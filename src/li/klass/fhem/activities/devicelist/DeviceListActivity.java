package li.klass.fhem.activities.devicelist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.RoomDetailAdapter;
import li.klass.fhem.adapter.devices.DeviceAdapter;
import li.klass.fhem.adapter.devices.DeviceAdapterProvider;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.SISPMSDevice;
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
                    DeviceAdapter<? extends Device<?>> adapter = DeviceAdapterProvider.INSTANCE.getAdapterFor(device);
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
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                String deviceName = (String) view.getTag();
                FS20Device device = FHEMService.INSTANCE.deviceListForAllRooms(false).getDeviceFor(deviceName);
                device.toggleState(DeviceListActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                update(false);
            }
        }.execute(null);

    }

    public void onSISPMSClick(final View view) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                SISPMSDevice device = (SISPMSDevice) view.getTag();
                device.toggleState(DeviceListActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                update(false);
            }
        }.execute(null);
    }
}
