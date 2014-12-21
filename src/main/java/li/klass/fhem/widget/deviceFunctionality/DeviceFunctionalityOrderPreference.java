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

package li.klass.fhem.widget.deviceFunctionality;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.ericharlow.DragNDrop.DragNDropListView;

import org.apache.pig.impl.util.ObjectSerializer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.ArrayListUtil;
import li.klass.fhem.util.Filter;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.widget.deviceFunctionality.DeviceFunctionalityOrderAdapter.OrderAction;

public class DeviceFunctionalityOrderPreference extends DialogPreference {

    @Inject
    ApplicationProperties applicationProperties;
    private ArrayList<DeviceFunctionalityPreferenceWrapper> wrappedDevices = newArrayList();

    @SuppressWarnings("unused")
    public DeviceFunctionalityOrderPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inject(context);
    }

    @SuppressWarnings("unused")
    public DeviceFunctionalityOrderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        inject(context);
    }

    private void inject(Context context) {
        ((AndFHEMApplication) ((Activity) context).getApplication()).inject(this);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.device_type_order_layout, null);

        assert view != null;

        DragNDropListView deviceTypeListView = (DragNDropListView) view.findViewById(android.R.id.list);

        // dirty hack ... this should be called by Android automatically ...
        onSetInitialValue(true, "");

        final DeviceFunctionalityOrderAdapter adapter =
                new DeviceFunctionalityOrderAdapter(getContext(), R.layout.device_type_list_item,
                        wrappedDevices);

        adapter.setListener(new DeviceFunctionalityOrderAdapter.OrderActionListener() {
            @Override
            public void deviceTypeReordered(DeviceFunctionalityPreferenceWrapper wrapper, OrderAction action) {
                int currentPosition = wrappedDevices.indexOf(wrapper);

                switch (action) {
                    case VISIBILITY_CHANGE:
                        wrappedDevices.get(currentPosition).invertVisibility();
                        break;
                }
                callChangeListener(wrappedDevices);
            }
        });
        deviceTypeListView.setAdapter(adapter);

        return view;
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);

        DeviceGroupHolder deviceTypeHolder = new DeviceGroupHolder(applicationProperties);
        List<DeviceFunctionality> visible = deviceTypeHolder.getVisible();
        List<DeviceFunctionality> invisible = deviceTypeHolder.getInvisible();

        this.wrappedDevices = wrapDevices(visible, invisible);
    }

    private ArrayList<DeviceFunctionalityPreferenceWrapper> wrapDevices(
            List<DeviceFunctionality> visible, List<DeviceFunctionality> invisible) {

        ArrayList<DeviceFunctionalityPreferenceWrapper> returnList = newArrayList();

        returnList.addAll(wrapList(visible, true));
        returnList.addAll(wrapList(invisible, false));

        return returnList;
    }

    private List<DeviceFunctionalityPreferenceWrapper> wrapList(
            List<DeviceFunctionality> toWrap, boolean isVisible) {

        List<DeviceFunctionalityPreferenceWrapper> result = newArrayList();
        for (DeviceFunctionality deviceType : toWrap) {
            result.add(new DeviceFunctionalityPreferenceWrapper(deviceType, isVisible));
        }
        return result;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) return;
        save();
    }

    private void save() {
        saveVisibleDevices();
        saveInvisibleDevices();
    }

    private void saveVisibleDevices() {
        ArrayList<DeviceFunctionalityPreferenceWrapper> visibleDevices =
                ArrayListUtil.filter(wrappedDevices, new Filter<DeviceFunctionalityPreferenceWrapper>() {
                    @Override
                    public boolean doFilter(DeviceFunctionalityPreferenceWrapper object) {
                        return object.isVisible();
                    }
                });
        DeviceFunctionality[] toPersist = unwrapDeviceTypes(visibleDevices);
        if (shouldPersist()) persistString(ObjectSerializer.serialize(toPersist));

    }

    private void saveInvisibleDevices() {
        ArrayList<DeviceFunctionalityPreferenceWrapper> invisibleDevices =
                ArrayListUtil.filter(wrappedDevices, new Filter<DeviceFunctionalityPreferenceWrapper>() {
                    @Override
                    public boolean doFilter(DeviceFunctionalityPreferenceWrapper object) {
                        return !object.isVisible();
                    }
                });
        DeviceFunctionality[] toPersist = unwrapDeviceTypes(invisibleDevices);
        if (shouldPersist()) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            assert sharedPreferences != null;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PreferenceKeys.DEVICE_TYPE_FUNCTIONALITY_ORDER_INVISIBLE,
                    ObjectSerializer.serialize(toPersist)).apply();
        }
    }

    private DeviceFunctionality[] unwrapDeviceTypes(ArrayList<DeviceFunctionalityPreferenceWrapper> toUnwrap) {
        ArrayList<DeviceFunctionality> finalList = newArrayList();
        for (DeviceFunctionalityPreferenceWrapper wrapper : toUnwrap) {
            finalList.add(wrapper.getDeviceFunctionality());
        }
        return finalList.toArray(new DeviceFunctionality[finalList.size()]);
    }

    @Override
    protected boolean shouldPersist() {
        return true;
    }

}
