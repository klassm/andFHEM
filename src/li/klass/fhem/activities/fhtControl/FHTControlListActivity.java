package li.klass.fhem.activities.fhtControl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.fhtControl.FHTControlListAdapter;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.widget.NestedListView;

public class FHTControlListActivity extends BaseActivity<FHTControlListAdapter> {
    @Override
    protected FHTControlListAdapter initializeLayoutAndReturnAdapter() {
        FHTControlListAdapter fhtControlListAdapter = new FHTControlListAdapter(this);
        NestedListView nestedListView = (NestedListView) findViewById(R.id.control_fht_list);
        nestedListView.setAdapter(fhtControlListAdapter);

        return fhtControlListAdapter;
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.control_fht_list);
    }

    @Override
    public void update(boolean doUpdate) {
        Bundle extras = getIntent().getExtras();
        final String deviceName = extras.getString("deviceName");

        RoomListService.INSTANCE.getAllRoomsDeviceList(this, doUpdate, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                FHTDevice fhtDevice = roomDeviceList.getDeviceFor(deviceName);
                adapter.updateData(fhtDevice.getDayControlMap());

                Button saveButton = (Button) findViewById(R.id.save);
                if (fhtDevice.hasChangedDayControlMapValues()) {
                    saveButton.setVisibility(View.VISIBLE);
                } else {
                    saveButton.setVisibility(View.GONE);
                }
            }
        });


    }
}
