package li.klass.fhem.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.*;
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
    private List<DeviceType> deviceTypes;
    private final List<DeviceAdapter<? extends Device<?>>> deviceAdapters;

    public RoomDetailAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context);
        this.deviceTypes = Arrays.asList(KS300, FHT, HMS, OWTEMP, CUL_WS, FS20, SIS_PMS);

        if (roomDeviceList != null) {
            updateData(roomDeviceList);
        }

        deviceAdapters = new ArrayList<DeviceAdapter<? extends Device<?>>>();
        deviceAdapters.add(new FS20Adapter());
        deviceAdapters.add(new CULWSAdapter());
        deviceAdapters.add(new HMSAdapter());
        deviceAdapters.add(new OwtempAdapter());
        deviceAdapters.add(new KS300Adapter());
        deviceAdapters.add(new FHTAdapter());
        deviceAdapters.add(new SISPMSAdapter());
    }

    @Override
    protected Device getChildForParentAndChildPosition(DeviceType parent, int childPosition) {
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
    protected View getChildView(Device child, View view, ViewGroup viewGroup) {
        for (DeviceAdapter<? extends Device<?>> deviceAdapter : deviceAdapters) {
            if (deviceAdapter.supports(child.getClass())) {
                view = deviceAdapter.getView(layoutInflater, child);
                view.setTag(child);
                return view;
            }
        }
        throw new IllegalArgumentException("unsupported device type " + child);
    }

    @Override
    protected List<DeviceType> getParents() {
        List<DeviceType> parents = new ArrayList<DeviceType>();
        for (DeviceType deviceType : deviceTypes) {
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
