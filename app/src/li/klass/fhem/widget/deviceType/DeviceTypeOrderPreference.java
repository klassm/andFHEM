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
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.util.ArrayListUtil;
import org.apache.pig.impl.util.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class DeviceTypeOrderPreference extends DialogPreference {

    private ArrayList<DeviceTypePreferenceWrapper> deviceTypes;

    public DeviceTypeOrderPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DeviceTypeOrderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        final ListView deviceTypeListView = new ListView(getContext());

        // dirty hack ... this should be called by Android automatically ...
        onSetInitialValue(true, "");

        final DeviceTypeOrderAdapter adapter = new DeviceTypeOrderAdapter(getContext(), R.layout.device_type_list_item, deviceTypes);
        adapter.setListener(new DeviceTypeOrderAdapter.DeviceTypeOrderActionListener() {
            @Override
            public void deviceTypeReordered(DeviceTypePreferenceWrapper wrapper, DeviceTypeOrderAdapter.DeviceTypeOrderAction action) {
                int currentPosition = deviceTypes.indexOf(wrapper);

                switch (action) {
                    case UP:
                        ArrayListUtil.moveUp(deviceTypes, currentPosition);
                        break;
                    case DOWN:
                        ArrayListUtil.moveDown(deviceTypes, currentPosition);
                        break;
                    case VISIBILITY_CHANGE:
                        deviceTypes.get(currentPosition).invertVisibility();
                        break;
                }
                callChangeListener(deviceTypes);
                adapter.updateData(deviceTypes);
            }
        });
        deviceTypeListView.setAdapter(adapter);
        return deviceTypeListView;
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);

        DeviceType[] elements;
        String persistedValue = getPersistedString(null);
        if (shouldPersist() && persistedValue != null && ! "".equals(persistedValue)) {
            elements = (DeviceType[]) ObjectSerializer.deserialize(persistedValue);
        } else {
            elements = DeviceType.values();
        }
        deviceTypes = wrapDeviceTypes(elements);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (! positiveResult) return;

        DeviceType[] toPersist = unwrapDeviceTypes(deviceTypes);
        if (shouldPersist()) persistString(ObjectSerializer.serialize(toPersist));
    }

    private DeviceType[] unwrapDeviceTypes(ArrayList<DeviceTypePreferenceWrapper> toUnwrap) {
        ArrayList<DeviceType> finalList = new ArrayList<DeviceType>();
        for (DeviceTypePreferenceWrapper wrapper : toUnwrap) {
            if (wrapper.isVisible()) {
                finalList.add(wrapper.getDeviceType());
            }
        }
        return finalList.toArray(new DeviceType[finalList.size()]);
    }

    private ArrayList<DeviceTypePreferenceWrapper> wrapDeviceTypes(DeviceType[] toWrap) {
        List<DeviceType> allDeviceTypes = new ArrayList<DeviceType>(Arrays.asList(DeviceType.values()));
        ArrayList<DeviceTypePreferenceWrapper> returnList = new ArrayList<DeviceTypePreferenceWrapper>();

        for (DeviceType deviceType : toWrap) {

            allDeviceTypes.remove(deviceType);
            returnList.add(new DeviceTypePreferenceWrapper(deviceType, true));
        }

        for (DeviceType deviceType : allDeviceTypes) {
            returnList.add(new DeviceTypePreferenceWrapper(deviceType, false));
        }

        return returnList;
    }

    @Override
    protected boolean shouldPersist() {
        return true;
    }
}
