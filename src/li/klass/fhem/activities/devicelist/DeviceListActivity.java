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

package li.klass.fhem.activities.devicelist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.rooms.RoomDetailAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.*;
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
        String deviceName = (String) view.getTag();

        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                FS20Device device = (FS20Device) resultData.getSerializable(BundleExtraKeys.DEVICE);
                FS20Service.INSTANCE.toggleState(DeviceListActivity.this, device, updateOnSuccessAction);
            }
        });

        startService(intent);
    }

    public void onSISPMSClick(final View view) {
        String deviceName = (String) view.getTag();

        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                SISPMSDevice device = (SISPMSDevice) resultData.getSerializable(BundleExtraKeys.DEVICE);
                SISPMSService.INSTANCE.toggleState(DeviceListActivity.this, device, updateOnSuccessAction);
            }
        });

        startService(intent);
    }
}
