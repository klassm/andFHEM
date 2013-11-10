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
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.widget.GridViewWithSectionsAdapter;
import li.klass.fhem.widget.deviceFunctionality.DeviceFunctionalityHolder;

import static li.klass.fhem.constants.PreferenceKeys.DEVICE_COLUMN_WIDTH;
import static li.klass.fhem.constants.PreferenceKeys.SHOW_HIDDEN_DEVICES;

public class DeviceGridAdapter extends GridViewWithSectionsAdapter<DeviceFunctionality, Device<?>> {
    public static final String TAG = DeviceGridAdapter.class.getName();
    protected RoomDeviceList roomDeviceList;
    private static final int DEFAULT_COLUMN_WIDTH = 355;
    private int lastParentHeight;
    private List<DeviceFunctionality> parents;

    public DeviceGridAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context);
        restoreParents();
        if (roomDeviceList != null) {
            updateData(roomDeviceList);
        }
    }

    private void restoreParents() {
        DeviceFunctionalityHolder holder = new DeviceFunctionalityHolder();
        parents = holder.getVisible();
        Log.d(TAG, "set visible parents: " + parents);
    }

    @Override
    protected Device<?> getChildForParentAndChildPosition(DeviceFunctionality parent, int childPosition) {
        if (childPosition < 0) return null;

        List<Device<?>> childrenForDeviceType = getChildrenForDeviceFunctionality(parent);
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

    private List<Device<?>> getChildrenForDeviceFunctionality(DeviceFunctionality deviceFunctionality) {
        if (roomDeviceList == null) return new ArrayList<Device<?>>();

        return roomDeviceList.getDevicesOfFunctionality(deviceFunctionality);
    }

    @Override
    protected View getParentView(DeviceFunctionality parent, int parentOffset, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.room_detail_parent, null);
        assert view != null;

        TextView textView = (TextView) view.findViewById(R.id.deviceType);
        if (parentOffset != 0) {
            textView.setText("");
        } else {
            textView.setText(parent.name());
        }

        int widthMeasureSpec = View.MeasureSpec.UNSPECIFIED;
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = view.getMeasuredHeight();
        if (lastParentHeight < measuredHeight) {
            lastParentHeight = measuredHeight;
        }
        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, lastParentHeight));

        return view;
    }

    @Override
    protected View getChildView(final DeviceFunctionality parent, int parentPosition,
                                Device<?> child, View view, ViewGroup viewGroup) {

        final DeviceAdapter<? extends Device<?>> deviceAdapter = DeviceType.getAdapterFor(child);
        if (deviceAdapter == null) {
            Log.d(DeviceGridAdapter.class.getName(), "unsupported device type " + child);
            View ret = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
            assert ret != null;

            ret.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            return ret;
        }

        if (!deviceAdapter.supports(child.getClass())) {
            String text = "adapter was found for device type, but it will not support the device: "
                    + child;
            Log.e(TAG, text);
            throw new IllegalArgumentException(text);
        }

        view = deviceAdapter.createOverviewView(layoutInflater, child);
        view.setTag(child);
        view.setLayoutParams(new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );

        return view;
    }

    @Override
    protected List<DeviceFunctionality> getParents() {
        List<DeviceFunctionality> viewableParents = new ArrayList<DeviceFunctionality>();
        for (DeviceFunctionality deviceType : parents) {
            if (getChildrenCountForParent(deviceType) <= 0) {
                Log.d(TAG, "deviceType " + deviceType + " has no children, filtered!");
            } else {
                viewableParents.add(deviceType);
            }
        }
        return viewableParents;
    }

    @Override
    protected int getRequiredColumnWidth() {
        ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
        int width = applicationProperties.getIntegerSharedPreference(DEVICE_COLUMN_WIDTH, DEFAULT_COLUMN_WIDTH);
        Log.d(TAG, "column width: " + width);
        return width;
    }

    public void updateData(RoomDeviceList roomDeviceList) {
        if (roomDeviceList == null) return;

        ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
        if (!applicationProperties.getBooleanSharedPreference(SHOW_HIDDEN_DEVICES, false)) {
            Set<Device> allDevices = roomDeviceList.getAllDevices();
            for (Device device : allDevices) {
                if (device.isInRoom("hidden")) {
                    roomDeviceList.removeDevice(device);
                }
            }
        }

        this.roomDeviceList = roomDeviceList;
        super.updateData();
    }
}
