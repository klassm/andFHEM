/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.adapter.devices.generic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.genericview.DeviceChart;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.genericview.ViewSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericDeviceAdapter<T extends Device<T>> extends DeviceDetailAvailableAdapter<T> {
    private static final String TAG = GenericDeviceAdapter.class.getName();

    private Class<T> deviceClass;
    protected List<DeviceDetailViewAction<T>> detailActions = new ArrayList<DeviceDetailViewAction<T>>();
    protected Map<String, FieldNameAddedToDetailListener<T>> fieldNameAddedListeners = new HashMap<String, FieldNameAddedToDetailListener<T>>();
    protected final LayoutInflater inflater;

    public GenericDeviceAdapter(Class<T> deviceClass) {
        this.deviceClass = deviceClass;
        inflater = (LayoutInflater) AndFHEMApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        afterPropertiesSet();
    }

    protected void afterPropertiesSet() {}

    @Override
    protected int getOverviewLayout(T device) {
        return R.layout.device_overview_generic;
    }

    @Override
    protected void fillDeviceOverviewView(View view, T device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        setTextView(view, R.id.deviceName, device.getAliasOrName());

        try {
            if (device.getClass().isAnnotationPresent(ViewSettings.class)) {
                ViewSettings annotation = device.getClass().getAnnotation(ViewSettings.class);
                if (annotation.showState()) {
                    createTableRow(inflater, layout, R.layout.device_overview_generic_table_row, device.getState(), annotation.stateStringId());
                }
                if (annotation.showMeasured()) {
                    createTableRow(inflater, layout, R.layout.device_overview_generic_table_row, device.getMeasured(), annotation.measuredStringId());
                }
            }
            Field[] declaredFields = device.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(ShowField.class) && declaredField.getAnnotation(ShowField.class).showInOverview()) {
                    createTableRow(device, inflater, layout, declaredField, R.layout.device_overview_generic_table_row);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception occurred while setting device overview values", e);
        }
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_generic;
    }

    @Override
    protected void fillDeviceDetailView(Context context, View view, T device) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TableLayout layout = (TableLayout) view.findViewById(R.id.generic);
        setTextView(view, R.id.deviceName, device.getAliasOrName());

        try {
            if (device.getClass().isAnnotationPresent(ViewSettings.class)) {
                if (device.getClass().getAnnotation(ViewSettings.class).showState()) {
                    TableRow row = createTableRow(inflater, layout, R.layout.device_detail_generic_table_row, device.getState(), R.string.state);
                    notifyFieldListeners(context, device, layout, "state", row);
                }
            }
            Field[] declaredFields = device.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(ShowField.class) && declaredField.getAnnotation(ShowField.class).showInDetail()) {
                    TableRow row = createTableRow(device, inflater, layout, declaredField, R.layout.device_detail_generic_table_row);
                    notifyFieldListeners(context, device, layout, declaredField.getName(), row);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception occurred while setting device overview values", e);
        }

        addDetailGraphButtons(context, view, device, inflater);
        addDetailActionhButtons(context, view, device, inflater);
        fillOtherStuffLayout(context, (LinearLayout) view.findViewById(R.id.otherStuff), device, inflater);
    }

    protected void fillOtherStuffLayout(Context context, LinearLayout layout, T device, LayoutInflater inflater) {}

    private void notifyFieldListeners(Context context, T device, TableLayout layout, String fieldName, TableRow fieldTableRow) {
        if (fieldNameAddedListeners.containsKey(fieldName)) {
            fieldNameAddedListeners.get(fieldName).onFieldNameAdded(context, layout, fieldName, device, fieldTableRow);
        }
    }

    private void addDetailGraphButtons(Context context, View view, T device, LayoutInflater inflater) {
        LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphButtons);
        if (device.getDeviceCharts().size() == 0 || device.getFileLog() == null) {
            graphLayout.setVisibility(View.GONE);
        }
        for (DeviceChart deviceChart : device.getDeviceCharts()) {
            addGraphButton(context, graphLayout, inflater, device, deviceChart);
        }
    }

    private void addDetailActionhButtons(Context context, View view, T device, LayoutInflater inflater) {
        LinearLayout actionLyout = (LinearLayout) view.findViewById(R.id.actionButtons);
        if (detailActions.size() == 0) {
            actionLyout.setVisibility(View.GONE);
        }
        for (DeviceDetailViewAction<T> action : detailActions) {
            actionLyout.addView(action.createButton(context, inflater, device));
        }
    }

    private TableRow createTableRow(T device, LayoutInflater inflater, TableLayout layout, Field declaredField, int resource) throws IllegalAccessException {
        Object value = declaredField.get(device);
        int description = declaredField.getAnnotation(ShowField.class).description();

        return createTableRow(inflater, layout, resource, value, description);
    }

    private TableRow createTableRow(LayoutInflater inflater, TableLayout layout, int resource, Object value, int description) {
        TableRow tableRow = (TableRow) inflater.inflate(resource, null);
        fillTableRow(description, value, tableRow);
        layout.addView(tableRow);
        return tableRow;
    }

    private void fillTableRow(int description, Object value, TableRow tableRow) {
        setTextView(tableRow, R.id.description, description);
        setTextView(tableRow, R.id.value, String.valueOf(value));

        if (value == null || value.equals("")) {
            tableRow.setVisibility(View.GONE);
        }
    }

    private void addGraphButton(final Context context, LinearLayout graphLayout, LayoutInflater inflater, final T device,
                                final DeviceChart chart) {
        Button button = (Button) inflater.inflate(R.layout.button, null);
        fillGraphButton(context, device, chart, button);
        graphLayout.addView(button);
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return deviceClass;
    }
}
