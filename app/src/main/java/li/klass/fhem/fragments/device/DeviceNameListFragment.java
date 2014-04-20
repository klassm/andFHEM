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

package li.klass.fhem.fragments.device;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

import li.klass.fhem.R;
import li.klass.fhem.activities.device.DeviceNameListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.widget.GridViewWithSections;

import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;

public abstract class DeviceNameListFragment extends BaseFragment {

    private int columnWidth = Integer.MAX_VALUE;
    private String roomName = null;
    protected ResultReceiver resultReceiver;

    public interface DeviceFilter extends Serializable {
        boolean isSelectable(Device<?> device);
    }

    @SuppressWarnings("unused")
    public DeviceNameListFragment(Bundle bundle, int columnWidth) {
        super(bundle);

        resultReceiver = bundle.getParcelable(BundleExtraKeys.RESULT_RECEIVER);

        if (columnWidth == -1) {
            this.columnWidth = ApplicationProperties.INSTANCE.getIntegerSharedPreference(
                    PreferenceKeys.DEVICE_COLUMN_WIDTH, Integer.MAX_VALUE);
        } else {
            this.columnWidth = columnWidth;
        }

        if (bundle.containsKey(ROOM_NAME)) {
            roomName = bundle.getString(ROOM_NAME);
        }
    }

    @SuppressWarnings("unused")
    public DeviceNameListFragment(Bundle bundle) {
        this(bundle, -1);
    }

    @SuppressWarnings("unused")
    public DeviceNameListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        View view = inflater.inflate(R.layout.device_name_list, null);
        assert view != null;
        GridViewWithSections deviceList = (GridViewWithSections) view.findViewById(R.id.deviceMap1);

        DeviceNameListAdapter adapter = new DeviceNameListAdapter(inflater.getContext(),
                new RoomDeviceList(""), columnWidth);
        adapter.registerOnClickObserver(new GridViewWithSections.GridViewWithSectionsOnClickObserver() {
            @Override
            public void onItemClick(View view, Object parent, Object child, int parentPosition, int childPosition) {
                onDeviceNameClick((String) parent, (Device<?>) child);
            }
        });
        deviceList.setAdapter(adapter);

        return view;
    }

    protected abstract void onDeviceNameClick(String parent, Device<?> child);

    private RoomDeviceList filterDevices(RoomDeviceList roomDeviceList) {
        RoomDeviceList filteredList = new RoomDeviceList(roomDeviceList.getRoomName());

        DeviceFilter deviceFilter = getDeviceFilter();
        for (Device<?> device : roomDeviceList.getAllDevices()) {
            if (deviceFilter == null || deviceFilter.isSelectable(device)) {
                filteredList.addDevice(device);
            }
        }

        return filteredList;
    }

    @Override
    public void update(boolean doUpdate) {
        Intent loadIntent;
        if (roomName == null) {
            loadIntent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST);
        } else {
            loadIntent = new Intent(Actions.GET_ROOM_DEVICE_LIST);
            loadIntent.putExtra(BundleExtraKeys.ROOM_NAME, roomName);
        }
        loadIntent.putExtra(BundleExtraKeys.DO_REFRESH, false);
        loadIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(BundleExtraKeys.DEVICE_LIST)) {
                    return;
                }

                RoomDeviceList roomDeviceList = (RoomDeviceList) resultData.getSerializable(BundleExtraKeys.DEVICE_LIST);
                long lastUpdate = resultData.getLong(BundleExtraKeys.LAST_UPDATE);
                deviceListReceived(roomDeviceList, lastUpdate);
            }
        });
        getActivity().startService(loadIntent);
    }

    protected RoomDeviceList deviceListReceived(RoomDeviceList roomDeviceList, long lastUpdate) {
        DeviceNameListAdapter adapter = getAdapter();
        if (adapter == null || getView() == null) return roomDeviceList;

        RoomDeviceList filteredList = filterDevices(roomDeviceList);

        String selectedDevice = creationBundle.getString(BundleExtraKeys.DEVICE_NAME);

        if (! filteredList.isEmptyOrOnlyContainsDoNotShowDevices()) {
            adapter.updateData(filteredList, selectedDevice, lastUpdate);

            int selectedDevicePosition = adapter.getSelectedDevicePosition();
            getGridView().setSelection(selectedDevicePosition);
        }

        return filteredList;
    }

    private DeviceFilter getDeviceFilter() {
        return (DeviceFilter) creationBundle.getSerializable(BundleExtraKeys.DEVICE_FILTER);
    }

    protected DeviceNameListAdapter getAdapter() {
        GridViewWithSections gridViewWithSections = getGridView();
        if (gridViewWithSections == null) return null;

        return (DeviceNameListAdapter) gridViewWithSections.getGridViewWithSectionsAdapter();
    }

    protected GridViewWithSections getGridView() {
        View view = getView();
        if (view == null) return null;

        return (GridViewWithSections) view.findViewById(R.id.deviceMap1);
    }
}
