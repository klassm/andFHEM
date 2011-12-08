package li.klass.fhem.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.KS300Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.widget.NestedListViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomDetailAdapter extends NestedListViewAdapter<String, Device> {
    private RoomDeviceList roomDeviceList;
    private List<String> deviceTypes;

    public RoomDetailAdapter(Context context, RoomDeviceList roomDeviceList) {
        super(context);
        this.deviceTypes = Arrays.asList("KS300", "FS20");

        if (roomDeviceList != null) {
            updateData(roomDeviceList);
        }
    }

    @Override
    protected Device getChildForParentAndChildPosition(String parent, int childPosition) {
        return getChildrenForDeviceType(parent).get(childPosition);
    }

    @Override
    protected int getChildrenCountForParent(String parent) {
        return getChildrenForDeviceType(parent).size();
    }

    private List<Device> getChildrenForDeviceType(String deviceType) {
        if (roomDeviceList == null) return new ArrayList<Device>();
        
        List<Device> devices = roomDeviceList.getDevicesForType(deviceType);
        if (devices == null) return new ArrayList<Device>();

        return devices;
    }

    @Override
    protected View getParentView(String parent, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.room_detail_parent, null);

        ParentViewHolder viewHolder = new ParentViewHolder();
        viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceType);
        view.setTag(viewHolder);

        viewHolder.deviceName.setText(parent);

        return view;
    }

    @Override
    protected View getChildView(Device child, View view, ViewGroup viewGroup) {
        if (child instanceof FS20Device) {
            return getFS20View((FS20Device) child, view);
        } else if (child instanceof KS300Device) {
            return getKS300View((KS300Device) child, view);
        } else {
            throw new IllegalArgumentException("unsupported device type");
        }
    }

    protected View getFS20View(FS20Device child, View view) {
        FS20Holder deviceHolder = new FS20Holder();
        view = layoutInflater.inflate(R.layout.room_detail_fs20, null);

        deviceHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceHolder.switchButton = (ToggleButton) view.findViewById(R.id.switchButton);

        deviceHolder.deviceName.setText(child.getName());
        deviceHolder.switchButton.setChecked(child.isOn());

        deviceHolder.switchButton.setTag(child);

        view.setTag(child);

        return view;
    }

    protected View getKS300View(KS300Device child, View view) {
        KS300Holder deviceHolder = new KS300Holder();
        view = layoutInflater.inflate(R.layout.room_detail_ks300, null);

        deviceHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceHolder.wind = (TextView) view.findViewById(R.id.wind);
        deviceHolder.humidity = (TextView) view.findViewById(R.id.humidity);
        deviceHolder.rain = (TextView) view.findViewById(R.id.rain);
        deviceHolder.temperature = (TextView) view.findViewById(R.id.temperature);

        deviceHolder.deviceName.setText(child.getName());
        deviceHolder.wind.setText(child.getWind());
        deviceHolder.temperature.setText(child.getTemperature());
        deviceHolder.humidity.setText(child.getHumidity());
        deviceHolder.rain.setText(child.getRain());

        view.setTag(child);

        return view;
    }

    @Override
    protected List<String> getParents() {
        List<String> parents = new ArrayList<String>();
        for (String deviceType : deviceTypes) {
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

    static class FS20Holder {
        TextView deviceName;
        ToggleButton switchButton;
    }

    static class KS300Holder {
        TextView deviceName;
        TextView wind;
        TextView temperature;
        TextView humidity;
        TextView rain;
    }
}
