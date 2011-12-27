package li.klass.fhem.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.DeviceAdapter;
import li.klass.fhem.adapter.devices.DeviceAdapterProvider;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.widget.NestedListViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static li.klass.fhem.domain.DeviceType.*;

public class RoomDetailAdapter extends NestedListViewAdapter<DeviceType, Device> {
    private RoomDeviceList roomDeviceList;
    private List<DeviceType> deviceTypeOrderList;

    public RoomDetailAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context);
        this.deviceTypeOrderList = Arrays.asList(KS300, FHT, HMS, OWTEMP, CUL_WS, FS20, SIS_PMS, CUL_FHTTK);

        if (roomDeviceList != null) {
            updateData(roomDeviceList);
        }
    }

    @Override
    protected Device getChildForParentAndChildPosition(DeviceType parent, int childPosition) {
        if (childPosition == -1) return null;

        return getChildrenForDeviceType(parent).get(childPosition);
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
    protected View getChildView(final Device child, View view, ViewGroup viewGroup) {
        final DeviceAdapter<? extends Device<?>> deviceAdapter = DeviceAdapterProvider.INSTANCE.getAdapterFor(child);
        if (deviceAdapter == null) {
            Log.e(RoomDetailAdapter.class.getName(), "unsupported device type " + child);
            throw new IllegalArgumentException("unsupported device type " + child);
        }

        if (! deviceAdapter.supports(child.getClass())) {
            Log.e(RoomDetailAdapter.class.getName(), "adapter was found for device type, but it will not support the device: " + child);
            throw new IllegalArgumentException("adapter was found for device type, but it will not support the device: " + child);
        }

        view = deviceAdapter.getView(layoutInflater, view, child);
        view.setTag(child);

        return view;
    }

    @Override
    protected List<DeviceType> getParents() {
        List<DeviceType> parents = new ArrayList<DeviceType>();
        for (DeviceType deviceType : deviceTypeOrderList) {
            if (getChildrenCountForParent(deviceType) > 0) {
                parents.add(deviceType);
            }
        }
        return parents;
    }

    public void updateData(RoomDeviceList roomDeviceList) {
        this.roomDeviceList = roomDeviceList;
        super.updateData();
    }

    private static class ParentViewHolder {
        TextView deviceName;
    }
}
