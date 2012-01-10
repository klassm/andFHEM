/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.activities.fhtControl;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.fhtControl.FHTTimetableControlListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
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
                Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
                intent.putExtras(new Bundle());
                intent.putExtra(BundleExtraKeys.DO_REFRESH, false);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        FHTDevice device = (FHTDevice) resultData.getSerializable(BundleExtraKeys.DEVICE);
                        FHTService.INSTANCE.setTimetableFor(FHTTimetableControlListActivity.this, device);
                    }
                });
                startService(intent);
//
//
//                RoomDeviceListListener listener = new RoomDeviceListListener() {
//                    @Override
//                    public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
//                        FHTDevice device = roomDeviceList.getDeviceFor(deviceName);
//                        FHTService.INSTANCE.setTimetableFor(FHTTimetableControlListActivity.this, device);
//                    }
//                };
//                RoomListService.INSTANCE.getAllRoomsDeviceList(FHTTimetableControlListActivity.this, false, listener);
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
