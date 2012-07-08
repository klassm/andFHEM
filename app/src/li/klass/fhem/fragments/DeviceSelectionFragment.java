/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.activities.device.DeviceSelectionAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.widget.NestedListView;

import java.io.Serializable;

public class DeviceSelectionFragment extends BaseFragment {

    private DeviceFilter deviceFilter;
    private ResultReceiver resultReceiver;
    private transient DeviceSelectionAdapter adapter;

    public interface DeviceFilter extends Serializable {
        boolean isSelectable(Device<?> device);
    }

    @SuppressWarnings("unused")
    public DeviceSelectionFragment(Bundle bundle) {
        super(bundle);

        if (bundle.containsKey(BundleExtraKeys.DEVICE_FILTER)) {
            Serializable deviceFilterSerializable = bundle.getSerializable(BundleExtraKeys.DEVICE_FILTER);
            if (deviceFilterSerializable instanceof DeviceFilter) {
                this.deviceFilter = (DeviceFilter) deviceFilterSerializable;
            }
        }

        resultReceiver = (ResultReceiver) bundle.getParcelable(BundleExtraKeys.RESULT_RECEIVER);
    }

    @SuppressWarnings("unused")
    public DeviceSelectionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        NestedListView nestedListView = new NestedListView(inflater.getContext());
        nestedListView.setId(android.R.id.list);

        adapter = new DeviceSelectionAdapter(inflater.getContext(), new RoomDeviceList(""));
        adapter.addParentChildObserver(new NestedListView.NestedListViewOnClickObserver() {
            @Override
            public void onItemClick(View view, Object parent, Object child, int parentPosition, int childPosition) {
                if (childPosition == -1) return;

                if (resultReceiver != null) {
                    Bundle result = new Bundle();
                    result.putSerializable(BundleExtraKeys.CLICKED_DEVICE, (Device<?>) child);
                    resultReceiver.send(ResultCodes.SUCCESS, result);
                }

                Intent intent = new Intent(Actions.BACK);
                intent.putExtra(BundleExtraKeys.CLICKED_DEVICE, (Device<?>) child);
                getActivity().sendBroadcast(intent);
            }
        });
        nestedListView.setAdapter(adapter);

        update(false);

        return nestedListView;
    }

    private void finish() {
        Intent backIntent = new Intent(Actions.BACK);
        AndFHEMApplication.getContext().sendBroadcast(backIntent);
    }

    private void filterDevices(RoomDeviceList roomDeviceList) {
        if (deviceFilter == null) return;

        for (Device<?> device : roomDeviceList.getAllDevices()) {
            if (! deviceFilter.isSelectable(device)) {
                roomDeviceList.removeDevice(device);
            }
        }
    }

    private void showNoDevicesAvailableDialog() {
        DialogUtil.showAlertDialog(getActivity(), -1, R.string.deviceListEmpty,
                new DialogUtil.AlertOnClickListener() {
                    @Override
                    public void onClick() {
                        finish();
                    }
                });
    }

    @Override
    public void update(boolean doUpdate) {
        Intent allDevicesIntent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST);
        allDevicesIntent.putExtras(new Bundle());
        allDevicesIntent.putExtra(BundleExtraKeys.DO_REFRESH, false);
        allDevicesIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(BundleExtraKeys.DEVICE_LIST)) {
                    return;
                }

                RoomDeviceList roomDeviceList = (RoomDeviceList) resultData.getSerializable(BundleExtraKeys.DEVICE_LIST);
                filterDevices(roomDeviceList);

                if (roomDeviceList.getAllDevices().size() == 0) {
                    showNoDevicesAvailableDialog();
                } else if (adapter != null) {
                    adapter.updateData(roomDeviceList);
                }
            }
        });
        getActivity().startService(allDevicesIntent);
    }
}
