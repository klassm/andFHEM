package li.klass.fhem.activities.fhtControl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.fhtControl.FHTTimetableControlListAdapter;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.device.FHTService;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.widget.NestedListView;

public class FHTTimetableControlListActivity extends BaseActivity<FHTTimetableControlListAdapter> {
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        this.deviceName = extras.getString("deviceName");

        setTitle(getResources().getString(R.string.timetable) + " " + deviceName);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected FHTTimetableControlListAdapter initializeLayoutAndReturnAdapter() {
        FHTTimetableControlListAdapter fhtControlListAdapter = new FHTTimetableControlListAdapter(this);
        NestedListView nestedListView = (NestedListView) findViewById(R.id.control_fht_list);
        nestedListView.setAdapter(fhtControlListAdapter);

        return fhtControlListAdapter;
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.control_fht_list);
        
        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoomDeviceListListener listener = new RoomDeviceListListener() {
                    @Override
                    public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                        FHTDevice device = roomDeviceList.getDeviceFor(deviceName);
                        FHTService.INSTANCE.setTimetableFor(FHTTimetableControlListActivity.this, device);
                    }
                };
                RoomListService.INSTANCE.getAllRoomsDeviceList(FHTTimetableControlListActivity.this, false, listener);
            }
        });
        
        Button resetButton = (Button) findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoomDeviceListListener listener = new RoomDeviceListListener() {
                    @Override
                    public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                        FHTDevice device = roomDeviceList.getDeviceFor(deviceName);
                        device.resetDayControlMapValues();
                        update(false);
                    }
                };
                RoomListService.INSTANCE.getAllRoomsDeviceList(FHTTimetableControlListActivity.this, false, listener);
            }
        });
    }

    @Override
    public void update(boolean doUpdate) {

        if (adapter == null) return;

        RoomListService.INSTANCE.getAllRoomsDeviceList(this, doUpdate, new RoomDeviceListListener() {
            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                FHTDevice fhtDevice = roomDeviceList.getDeviceFor(deviceName);
                adapter.updateData(fhtDevice.getDayControlMap());

                View holder = findViewById(R.id.changeValueButtonHolder);
                if (fhtDevice.hasChangedDayControlMapValues()) {
                    holder.setVisibility(View.VISIBLE);
                } else {
                    holder.setVisibility(View.GONE);
                }
            }
        });


    }
}
