/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.widget.deviceType;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.ericharlow.DragNDrop.DragNDropListView;
import li.klass.fhem.R;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.util.ArrayListUtil;
import li.klass.fhem.util.Filter;
import org.apache.pig.impl.util.ObjectSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class DeviceTypeOrderPreference extends DialogPreference {

    private ArrayList<DeviceTypePreferenceWrapper> wrappedDevices = new ArrayList<DeviceTypePreferenceWrapper>();

    public DeviceTypeOrderPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DeviceTypeOrderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.device_type_order_layout, null);
        DragNDropListView deviceTypeListView = (DragNDropListView) view.findViewById(android.R.id.list);

        // dirty hack ... this should be called by Android automatically ...
        onSetInitialValue(true, "");

        final DeviceTypeOrderAdapter adapter = new DeviceTypeOrderAdapter(getContext(), R.layout.device_type_list_item, wrappedDevices);
        adapter.setListener(new DeviceTypeOrderAdapter.DeviceTypeOrderActionListener() {
            @Override
            public void deviceTypeReordered(DeviceTypePreferenceWrapper wrapper, DeviceTypeOrderAdapter.DeviceTypeOrderAction action) {
                int currentPosition = wrappedDevices.indexOf(wrapper);

                switch (action) {
                    case UP:
                        ArrayListUtil.moveUp(wrappedDevices, currentPosition);
                        break;
                    case DOWN:
                        ArrayListUtil.moveDown(wrappedDevices, currentPosition);
                        break;
                    case VISIBILITY_CHANGE:
                        wrappedDevices.get(currentPosition).invertVisibility();
                        break;
                }
                callChangeListener(wrappedDevices);
                adapter.updateData(wrappedDevices);
            }
        });
        deviceTypeListView.setAdapter(adapter);

        return view;
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);

        DeviceTypeHolder deviceTypeHolder = new DeviceTypeHolder();
        List<DeviceType> visibleDeviceTypes = deviceTypeHolder.getVisibleDeviceTypes();
        List<DeviceType> invisibleDeviceTypes = deviceTypeHolder.getInvisibleDeviceTypes();

        this.wrappedDevices = wrapDevices(visibleDeviceTypes, invisibleDeviceTypes);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (! positiveResult) return;
        save();
    }

    private List<DeviceType> parsePersistedValue(String persistedValue, DeviceType[] defaultValue) {
        if (shouldPersist() && persistedValue != null && ! "".equals(persistedValue)) {
            return Arrays.asList((DeviceType[]) ObjectSerializer.deserialize(persistedValue));
        }
        return Arrays.asList(defaultValue);
    }

    private ArrayList<DeviceTypePreferenceWrapper> wrapDevices(List<DeviceType> visibleDeviceTypes, List<DeviceType> invisibleDeviceTypes) {
        ArrayList<DeviceTypePreferenceWrapper> returnList = new ArrayList<DeviceTypePreferenceWrapper>();

        returnList.addAll(wrapList(visibleDeviceTypes, true));
        returnList.addAll(wrapList(invisibleDeviceTypes, false));

        return returnList;
    }

    private List<DeviceTypePreferenceWrapper> wrapList(List<DeviceType> toWrap, boolean isVisible) {
        List<DeviceTypePreferenceWrapper> result = new ArrayList<DeviceTypePreferenceWrapper>();
        for (DeviceType deviceType : toWrap) {
            result.add(new DeviceTypePreferenceWrapper(deviceType, isVisible));
        }
        return result;
    }

    private void save() {
        saveVisibleDevices();
        saveInvisibleDevices();
    }

    private void saveVisibleDevices() {
        ArrayList<DeviceTypePreferenceWrapper> visibleDevices = ArrayListUtil.filter(wrappedDevices, new Filter<DeviceTypePreferenceWrapper>() {
            @Override
            public boolean doFilter(DeviceTypePreferenceWrapper object) {
                return object.isVisible();
            }
        });
        DeviceType[] toPersist = unwrapDeviceTypes(visibleDevices);
        if (shouldPersist()) persistString(ObjectSerializer.serialize(toPersist));

    }

    private void saveInvisibleDevices() {
        ArrayList<DeviceTypePreferenceWrapper> invisibleDevices = ArrayListUtil.filter(wrappedDevices, new Filter<DeviceTypePreferenceWrapper>() {
            @Override
            public boolean doFilter(DeviceTypePreferenceWrapper object) {
                return ! object.isVisible();
            }
        });
        DeviceType[] toPersist = unwrapDeviceTypes(invisibleDevices);
        if (shouldPersist()) {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(PreferenceKeys.DEVICE_TYPE_ORDER_INVISIBLE, ObjectSerializer.serialize(toPersist)).commit();
        }
    }

    private DeviceType[] unwrapDeviceTypes(ArrayList<DeviceTypePreferenceWrapper> toUnwrap) {
        ArrayList<DeviceType> finalList = new ArrayList<DeviceType>();
        for (DeviceTypePreferenceWrapper wrapper : toUnwrap) {
            finalList.add(wrapper.getDeviceType());
        }
        return finalList.toArray(new DeviceType[finalList.size()]);
    }

    @Override
    protected boolean shouldPersist() {
        return true;
    }
}
