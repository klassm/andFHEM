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

package li.klass.fhem.adapter.rooms;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.widget.NestedListViewAdapter;

public class DeviceListAdapter<DEVICE extends Device<DEVICE>> extends
        NestedListViewAdapter<DeviceFunctionality, DEVICE> {

    private RoomDeviceList roomDeviceList;
    private long lastUpdate;

    public DeviceListAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context);
        if (roomDeviceList != null) {
            updateData(roomDeviceList, -1);
        }
    }

    @Override
    protected DEVICE getChildForParentAndChildPosition(DeviceFunctionality parent, int childPosition) {
        if (childPosition == -1) return null;

        List<DEVICE> childrenForDeviceType = getChildrenForDeviceFunctionality(parent);
        if (childPosition >= childrenForDeviceType.size()) {
            return null;
        } else {
            return childrenForDeviceType.get(childPosition);
        }
    }

    @Override
    protected int getChildrenCountForParent(DeviceFunctionality parent) {
        return getChildrenForDeviceFunctionality(parent).size();
    }

    private List<DEVICE> getChildrenForDeviceFunctionality(DeviceFunctionality deviceFunctionality) {
        if (roomDeviceList == null) return new ArrayList<DEVICE>();

        return new ArrayList<DEVICE>(roomDeviceList.<DEVICE>getDevicesOfFunctionality(deviceFunctionality));
    }

    @Override
    protected View getParentView(DeviceFunctionality parent, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.room_detail_parent, null);

        ParentViewHolder viewHolder = new ParentViewHolder();
        viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceType);
        view.setTag(viewHolder);

        viewHolder.deviceName.setText(parent.toString());

        return view;
    }

    @Override
    protected View getChildView(final DeviceFunctionality parent, int parentPosition, DEVICE child,
                                View view, ViewGroup viewGroup, int relativeChildPosition) {
        final DeviceAdapter<? extends Device<?>> deviceAdapter = DeviceType.getAdapterFor(child);
        if (deviceAdapter == null) {
            Log.e(DeviceListAdapter.class.getName(), "unsupported device type " + child);
            return layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        if (!deviceAdapter.supports(child.getClass())) {
            Log.e(DeviceListAdapter.class.getName(), "adapter was found for device type, but it will not support the device: " + child);
            throw new IllegalArgumentException("adapter was found for device type, but it will not support the device: " + child);
        }

        view = deviceAdapter.createOverviewView(layoutInflater, child, lastUpdate);
        view.setTag(child);

        return view;
    }

    @Override
    protected List<DeviceFunctionality> getParents() {
        List<DeviceFunctionality> parents = new ArrayList<DeviceFunctionality>();
        for (DeviceFunctionality deviceFunctionality : DeviceFunctionality.values()) {
            if (getChildrenCountForParent(deviceFunctionality) > 0) {
                parents.add(deviceFunctionality);
            }
        }
        return parents;
    }

    public void updateData(RoomDeviceList roomDeviceList, long lastUpdate) {
        if (roomDeviceList == null) return;

        this.lastUpdate = lastUpdate;
        this.roomDeviceList = roomDeviceList;
        super.updateData();
    }

    private static class ParentViewHolder {
        TextView deviceName;
    }
}
