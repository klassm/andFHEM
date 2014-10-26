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
import android.widget.LinearLayout;

import java.io.Serializable;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.activities.device.DeviceNameListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.widget.GridViewWithSections;

import static li.klass.fhem.adapter.rooms.DeviceGridAdapter.DEFAULT_COLUMN_WIDTH;
import static li.klass.fhem.constants.BundleExtraKeys.COLUMN_WIDTH;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_FILTER;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.EMPTY_TEXT_ID;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;

public abstract class DeviceNameListFragment extends BaseFragment {

    protected ResultReceiver resultReceiver;
    @Inject
    ApplicationProperties applicationProperties;
    private int columnWidth;
    private String roomName;
    private String deviceName;
    private DeviceFilter deviceFilter;
    private int emptyText;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args.containsKey(COLUMN_WIDTH)) {
            columnWidth = args.getInt(COLUMN_WIDTH);
        } else {
            columnWidth = DEFAULT_COLUMN_WIDTH;
        }
        roomName = args.getString(ROOM_NAME);
        resultReceiver = args.getParcelable(RESULT_RECEIVER);
        deviceName = args.getString(DEVICE_NAME);
        deviceFilter = (DeviceFilter) args.getSerializable(DEVICE_FILTER);
        emptyText = args.containsKey(EMPTY_TEXT_ID) ? args.getInt(EMPTY_TEXT_ID) : R.string.devicelist_empty;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        View view = inflater.inflate(R.layout.device_name_list, container, false);
        assert view != null;
        GridViewWithSections deviceList = (GridViewWithSections) view.findViewById(R.id.deviceMap1);

        DeviceNameListAdapter adapter = new DeviceNameListAdapter(inflater.getContext(),
                new RoomDeviceList(""), columnWidth, applicationProperties);
        deviceList.setOnClickListener(new GridViewWithSections.OnClickListener<String, Device<?>>() {
            @Override
            public boolean onItemClick(View view, String parent, Device<?> child, int parentPosition, int childPosition) {
                onDeviceNameClick(parent, child);
                return true;
            }
        });
        deviceList.setLongClickable(true);
        deviceList.setAdapter(adapter);

        LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.emptyView);
        fillEmptyView(emptyView, getEmptyTextId(), container);

        return view;
    }

    protected abstract void onDeviceNameClick(String parent, Device<?> child);

    protected int getEmptyTextId() {
        return emptyText;
    }

    @Override
    public void update(boolean doUpdate) {
        Intent loadIntent;
        if (roomName == null) {
            loadIntent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST);
        } else {
            loadIntent = new Intent(Actions.GET_ROOM_DEVICE_LIST);
            loadIntent.putExtra(ROOM_NAME, roomName);
        }
        loadIntent.setClass(getActivity(), RoomListIntentService.class);
        loadIntent.putExtra(BundleExtraKeys.DO_REFRESH, false);
        loadIntent.putExtra(RESULT_RECEIVER, new ResultReceiver(new Handler()) {
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

        if (!filteredList.isEmptyOrOnlyContainsDoNotShowDevices()) {
            adapter.updateData(filteredList, deviceName, lastUpdate);

            int selectedDevicePosition = adapter.getSelectedDevicePosition();
            getGridView().setSelection(selectedDevicePosition);
            hideEmptyView();
        } else {
            showEmptyView();
        }

        return filteredList;
    }

    protected DeviceNameListAdapter getAdapter() {
        GridViewWithSections gridViewWithSections = getGridView();
        if (gridViewWithSections == null) return null;

        return (DeviceNameListAdapter) gridViewWithSections.getGridViewWithSectionsAdapter();
    }

    private RoomDeviceList filterDevices(RoomDeviceList roomDeviceList) {
        RoomDeviceList filteredList = new RoomDeviceList(roomDeviceList.getRoomName());

        for (Device<?> device : roomDeviceList.getAllDevices()) {
            if (deviceFilter == null || deviceFilter.isSelectable(device)) {
                filteredList.addDevice(device);
            }
        }

        return filteredList;
    }

    protected GridViewWithSections getGridView() {
        View view = getView();
        if (view == null) return null;

        return (GridViewWithSections) view.findViewById(R.id.deviceMap1);
    }

    public interface DeviceFilter extends Serializable {
        boolean isSelectable(Device<?> device);
    }
}
