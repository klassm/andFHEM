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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;

import java.lang.reflect.Field;
import java.util.*;

public class GenericDeviceAdapter<D extends Device<D>> extends DeviceAdapter<D> {
    private static final String TAG = GenericDeviceAdapter.class.getName();

    private Class<D> deviceClass;
    protected List<DeviceDetailViewAction<D>> detailActions = new ArrayList<DeviceDetailViewAction<D>>();
    private Map<String, Set<FieldNameAddedToDetailListener<D>>> fieldNameAddedListeners = new HashMap<String, Set<FieldNameAddedToDetailListener<D>>>();
    protected final LayoutInflater inflater;

    public GenericDeviceAdapter(Class<D> deviceClass) {
        this.deviceClass = deviceClass;
        inflater = (LayoutInflater) AndFHEMApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        afterPropertiesSet();
    }

    protected void afterPropertiesSet() {
    }

    @Override
    protected int getOverviewLayout(D device) {
        return R.layout.device_overview_generic;
    }

    @Override
    protected void fillDeviceOverviewView(View view, D device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        setTextView(view, R.id.deviceName, device.getAliasOrName());

        try {
            if (device.getClass().isAnnotationPresent(DetailOverviewViewSettings.class)) {
                DetailOverviewViewSettings annotation = device.getClass().getAnnotation(DetailOverviewViewSettings.class);
                if (annotation.showState()) {
                    createTableRow(inflater, layout, R.layout.device_overview_generic_table_row, device.getState(), annotation.stateStringId().getId());
                }
                if (annotation.showMeasured()) {
                    createTableRow(inflater, layout, R.layout.device_overview_generic_table_row, device.getMeasured(), annotation.measuredStringId().getId());
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
    public boolean supportsDetailView(Device device) {
        return true;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_generic;
    }

    @Override
    protected final View getDeviceDetailView(Context context, D device) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(getDetailViewLayout(), null);
        fillDeviceDetailView(context, view, device);

        setTextViewOrHideTableRow(view, R.id.tableRowDeviceName, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowDef, R.id.def, device.getDefinition());
        setTextViewOrHideTableRow(view, R.id.tableRowRoom, R.id.rooms, device.getRoomConcatenated());
        setTextViewOrHideTableRow(view, R.id.tableRowMeasured, R.id.measured, device.getMeasured());

        return view;
    }

    protected void fillDeviceDetailView(Context context, View view, D device) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TableLayout layout = (TableLayout) view.findViewById(R.id.generic);
        setTextView(view, R.id.deviceName, device.getAliasOrName());

        try {
            if (device.getClass().isAnnotationPresent(DetailOverviewViewSettings.class)) {
                if (device.getClass().getAnnotation(DetailOverviewViewSettings.class).showState()) {
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
        addDetailActionButtons(context, view, device, inflater);
        fillOtherStuffLayout(context, (LinearLayout) view.findViewById(R.id.otherStuff), device, inflater);
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        return intent;
    }

    @Override
    public boolean supportsFloorplan(D device) {
        return deviceClass.isAnnotationPresent(FloorplanViewSettings.class);
    }

    @Override
    public View getFloorplanView(Context context, D device) {
        if (!supportsFloorplan(device)) return null;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView deviceName = new TextView(context);
        deviceName.setText(device.getAliasOrName() + "  ");
        deviceName.setMaxLines(1);
        deviceName.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(deviceName);

        fillFloorplanView(context, device, layout, deviceClass.getAnnotation(FloorplanViewSettings.class));

        return layout;
    }

    protected void fillFloorplanView(Context context, D device, LinearLayout layout, FloorplanViewSettings viewSettings) {
        if (viewSettings.showState()) {
            layout.addView(createFloorplanTextView(context, device.getState()));
        }

        Field[] declaredFields = device.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.isAnnotationPresent(ShowField.class) && declaredField.getAnnotation(ShowField.class).showInFloorplan()) {
                try {
                    layout.addView(createFloorplanTextView(context, declaredField.get(device).toString()));
                } catch (IllegalAccessException e) {
                    Log.e(GenericDeviceAdapter.class.getName(), "exception while reading floorplan value", e);
                }
            }
        }
    }

    private TextView createFloorplanTextView(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text + "   ");
        textView.setTextSize(10);
        textView.setSingleLine(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        textView.setLayoutParams(params);

        return textView;
    }

    protected void fillOtherStuffLayout(Context context, LinearLayout layout, D device, LayoutInflater inflater) {
    }

    protected void registerFieldListener(String fieldName, FieldNameAddedToDetailListener<D> listener) {
        if (!fieldNameAddedListeners.containsKey(fieldName)) {
            fieldNameAddedListeners.put(fieldName, new HashSet<FieldNameAddedToDetailListener<D>>());
        }

        fieldNameAddedListeners.get(fieldName).add(listener);
    }

    private void notifyFieldListeners(Context context, D device, TableLayout layout, String fieldName, TableRow fieldTableRow) {
        if (!fieldNameAddedListeners.containsKey(fieldName)) {
            return;
        }

        Set<FieldNameAddedToDetailListener<D>> listeners = fieldNameAddedListeners.get(fieldName);
        for (FieldNameAddedToDetailListener<D> listener : listeners) {
            if (listener.supportsDevice(device)) {
                listener.onFieldNameAdded(context, layout, fieldName, device, fieldTableRow);
            }
        }
    }

    private void addDetailGraphButtons(Context context, View view, D device, LayoutInflater inflater) {
        LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphButtons);
        if (device.getDeviceCharts().size() == 0 || device.getFileLog() == null) {
            graphLayout.setVisibility(View.GONE);
            return;
        }
        for (DeviceChart deviceChart : device.getDeviceCharts()) {
            addGraphButton(context, graphLayout, inflater, device, deviceChart);
        }
    }

    private void addDetailActionButtons(Context context, View view, D device, LayoutInflater inflater) {
        LinearLayout actionLayout = (LinearLayout) view.findViewById(R.id.actionButtons);
        if (detailActions.size() == 0) {
            actionLayout.setVisibility(View.GONE);
        }
        for (DeviceDetailViewAction<D> action : detailActions) {
            Button button = action.createButton(context, inflater, device, actionLayout);
            actionLayout.addView(button);
        }
    }

    private TableRow createTableRow(D device, LayoutInflater inflater, TableLayout layout, Field declaredField, int resource) throws IllegalAccessException {
        Object value = declaredField.get(device);
        int description = declaredField.getAnnotation(ShowField.class).description().getId();

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

    private void addGraphButton(final Context context, LinearLayout graphLayout, LayoutInflater inflater, final D device,
                                final DeviceChart chart) {
        Button button = (Button) inflater.inflate(R.layout.button_device_detail, graphLayout, false);
        fillGraphButton(context, device, chart, button);
        graphLayout.addView(button);
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return deviceClass;
    }
}
