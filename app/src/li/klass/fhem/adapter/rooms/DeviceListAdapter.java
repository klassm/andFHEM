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
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.widget.NestedListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends NestedListViewAdapter<DeviceType, Device<?>> {
    private RoomDeviceList roomDeviceList;

    public DeviceListAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context);
        if (roomDeviceList != null) {
            updateData(roomDeviceList);
        }
    }

    @Override
    protected Device getChildForParentAndChildPosition(DeviceType parent, int childPosition) {
        if (childPosition == -1) return null;

        List<Device> childrenForDeviceType = getChildrenForDeviceType(parent);
        if (childPosition >= childrenForDeviceType.size()) {
            return null;
        } else {
            return childrenForDeviceType.get(childPosition);
        }
    }

    @Override
    protected int getChildrenCountForParent(DeviceType parent) {
        return getChildrenForDeviceType(parent).size();
    }

    private List<Device> getChildrenForDeviceType(DeviceType deviceType) {
        if (roomDeviceList == null) return new ArrayList<Device>();

        return new ArrayList<Device>(roomDeviceList.getDevicesOfType(deviceType));
    }

    @Override
    protected View getParentView(DeviceType parent, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.room_detail_parent, null);

        ParentViewHolder viewHolder = new ParentViewHolder();
        viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceType);
        view.setTag(viewHolder);

        viewHolder.deviceName.setText(parent.toString());

        return view;
    }

    @Override
    protected View getChildView(final DeviceType parent, int parentPosition, Device<?> child, View view, ViewGroup viewGroup) {
        final DeviceAdapter<? extends Device<?>> deviceAdapter = DeviceType.getAdapterFor(child);
        if (deviceAdapter == null) {
            Log.e(DeviceListAdapter.class.getName(), "unsupported device type " + child);
            return layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        if (!deviceAdapter.supports(child.getClass())) {
            Log.e(DeviceListAdapter.class.getName(), "adapter was found for device type, but it will not support the device: " + child);
            throw new IllegalArgumentException("adapter was found for device type, but it will not support the device: " + child);
        }

        view = deviceAdapter.createOverviewView(layoutInflater, child);
        view.setTag(child);

        return view;
    }

    @Override
    protected List<DeviceType> getParents() {
        List<DeviceType> parents = new ArrayList<DeviceType>();
        for (DeviceType deviceType : DeviceType.values()) {
            if (getChildrenCountForParent(deviceType) > 0 &&
                    deviceType.mayShowInCurrentConnectionType()) {
                parents.add(deviceType);
            }
        }
        return parents;
    }

    public void updateData(RoomDeviceList roomDeviceList) {
        if (roomDeviceList == null) return;
        this.roomDeviceList = roomDeviceList;
        super.updateData();
    }

    private static class ParentViewHolder {
        TextView deviceName;
    }
}
