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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.widget.GridViewWithSectionsAdapter;
import li.klass.fhem.widget.deviceFunctionality.DeviceGroupHolder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_COLUMN_WIDTH;
import static li.klass.fhem.constants.PreferenceKeys.SHOW_HIDDEN_DEVICES;

public class DeviceGridAdapter<T extends FhemDevice<T>> extends GridViewWithSectionsAdapter<String, T> {
    public static final int DEFAULT_COLUMN_WIDTH = 355;

    private final ApplicationProperties applicationProperties;
    protected RoomDeviceList roomDeviceList;
    private int lastParentHeight;
    private List<String> deviceGroupParents = newArrayList();
    private List<String> parents = newArrayList();
    private Set<String> hiddenParents = newHashSet();
    private Map<String, List<T>> parentChildMap = newHashMap();
    private Map<Class, Integer> viewTypeMap = newHashMap();
    private long lastUpdate;

    private static final Logger LOG = LoggerFactory.getLogger(DeviceGridAdapter.class);
    private GroupComparator groupComparator;

    public DeviceGridAdapter(Context context, RoomDeviceList roomDeviceList, ApplicationProperties applicationProperties) {
        super(context);
        int currentViewType = 1;
        for (DeviceType type : DeviceType.values()) {
            if (type.getAdapter() != null && type.getAdapter().getOverviewViewHolderClass() != null) {
                Class viewTypeHolderClass = type.getAdapter().getOverviewViewHolderClass();
                if (!viewTypeMap.containsKey(viewTypeHolderClass)) {
                    viewTypeMap.put(viewTypeHolderClass, currentViewType++);
                }
            }
        }
        this.applicationProperties = applicationProperties;
        restoreParents();
        if (roomDeviceList != null) {
            updateData(roomDeviceList, -1);
        }
    }

    /**
     * Load deviceGroupParents from the serialized state. This includes all visible devices from
     * the {@link li.klass.fhem.constants.PreferenceKeys#DEVICE_FUNCTIONALITY_ORDER_VISIBLE}
     * property.
     */
    public void restoreParents() {
        deviceGroupParents.clear();
        DeviceGroupHolder holder = new DeviceGroupHolder(applicationProperties);

        for (DeviceFunctionality visible : holder.getVisible(context)) {
            deviceGroupParents.add(visible.getCaptionText(context));
        }
        for (DeviceFunctionality invisible : holder.getInvisible(context)) {
            hiddenParents.add(invisible.getCaptionText(context));
        }
        groupComparator = new GroupComparator(DeviceFunctionality.UNKNOWN.getCaptionText(context), deviceGroupParents);

        LOG.trace("restoreParents - set visible deviceGroupParents: {}" + deviceGroupParents);
    }

    @SuppressWarnings("unchecked")
    public void updateData(RoomDeviceList roomDeviceList, long lastUpdate) {
        if (roomDeviceList == null) return;

        LOG.info(TAG, "updateData(lastUpdate={})", lastUpdate);
        this.lastUpdate = lastUpdate;
        parents.clear();
        parents.addAll(deviceGroupParents);

        Set<String> customParents = newHashSet();

        boolean showHiddenDevices = applicationProperties.getBooleanSharedPreference(SHOW_HIDDEN_DEVICES, false, context);

        Set<FhemDevice> allDevices = roomDeviceList.getAllDevices();
        for (FhemDevice device : allDevices) {
            LOG.trace("updateData - contained device {}", device.getName());
            if (device.isInRoom("hidden") && !showHiddenDevices ||
                    device.isInAnyRoomOf(roomDeviceList.getHiddenRooms())) {
                roomDeviceList.removeDevice(device, context);
            } else {
                customParents.addAll(device.getInternalDeviceGroupOrGroupAttributes(context));
            }
        }

        customParents.removeAll(parents);
        Collections.sort(newArrayList(customParents));

        parents.addAll(customParents);
        parents.removeAll(roomDeviceList.getHiddenGroups());
        parents.removeAll(hiddenParents);
        Collections.sort(parents, groupComparator);

        parentChildMap = newHashMap();

        this.roomDeviceList = roomDeviceList;
        super.updateData();
    }

    @Override
    protected T getChildForParentAndChildPosition(String parent, int childPosition) {
        if (childPosition < 0) return null;

        List<T> childrenForDeviceType = getChildrenForDeviceGroup(parent);
        if (childPosition >= childrenForDeviceType.size()) {
            return null;
        } else {
            return childrenForDeviceType.get(childPosition);
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> getChildrenForDeviceGroup(String deviceGroup) {
        if (roomDeviceList == null) {
            return newArrayList();
        } else if (!parentChildMap.containsKey(deviceGroup)) {
            parentChildMap.put(deviceGroup, (List<T>) roomDeviceList.getDevicesOfFunctionality(deviceGroup));
        }

        return parentChildMap.get(deviceGroup);
    }

    @Override
    protected View getParentView(String parent, int parentOffset, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.room_detail_parent, viewGroup, false);

            assert view != null;

            ParentViewHolder viewHolder = new ParentViewHolder();
            viewHolder.setDeviceType((TextView) view.findViewById(R.id.deviceType));
            view.setTag(viewHolder);
        }
        ParentViewHolder holder = (ParentViewHolder) view.getTag();
        TextView textView = holder.getDeviceType();
        if (parentOffset != 0) {
            textView.setText("");
        } else {
            textView.setText(parent);
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
    public int getItemViewType(int position) {
        int parentBasePosition = getParentBasePosition(position);
        if (parentBasePosition != -1) {
            return 0;
        } else {
            FhemDevice item = (FhemDevice) getItem(position);
            if (item != null) {
                DeviceAdapter adapter = DeviceType.getAdapterFor(item);
                if (adapter != null && adapter.getOverviewViewHolderClass() != null) {
                    return viewTypeMap.get(adapter.getOverviewViewHolderClass());
                }
            }
        }
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return viewTypeMap.size() + 1;
    }

    @Override
    protected View getChildView(final String parent, int parentPosition,
                                T child, View view, ViewGroup viewGroup) {

        final DeviceAdapter<? extends FhemDevice<?>> deviceAdapter = DeviceType.getAdapterFor(child);

        if (deviceAdapter == null) {
            LOG.debug("getChildView - unsupported device type {}", child);
            View ret = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
            assert ret != null;

            ret.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            return ret;
        }

        if (!deviceAdapter.supports(child.getClass())) {
            String text = "getChildView - adapter was found for device type, but it will not support the device: "
                    + child;
            LOG.error(text);
            throw new IllegalArgumentException(text);
        }

        deviceAdapter.attach(context);

        view = deviceAdapter.createOverviewView(layoutInflater, view, child, lastUpdate);
        view.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );

        return view;
    }

    @Override
    protected List<String> getDeviceGroupParents() {
        List<String> viewableParents = newArrayList();
        for (String group : parents) {
            if (getChildrenCountForParent(group) <= 0) {
                LOG.trace("getDeviceGroupParents - group {} has no children, filtered!", group);
            } else {
                viewableParents.add(group);
            }
        }
        return viewableParents;
    }

    @Override
    protected int getChildrenCountForParent(String parent) {
        return getChildrenForDeviceGroup(parent).size();
    }

    @Override
    protected int getRequiredColumnWidth() {
        int width = applicationProperties.getIntegerSharedPreference(DEVICE_COLUMN_WIDTH, DEFAULT_COLUMN_WIDTH, context);
        LOG.debug("getRequiredColumnWidth - column width: {}", width);
        return width;
    }
}
