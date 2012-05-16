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

package li.klass.fhem.adapter.devices.core;

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
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.genericview.DeviceChart;
import li.klass.fhem.domain.genericview.ShowInDetail;
import li.klass.fhem.domain.genericview.ShowInOverview;
import li.klass.fhem.domain.genericview.ViewSettings;

import java.lang.reflect.Field;

public class GenericDeviceAdapter<T extends Device<T>> extends DeviceDetailAvailableAdapter<T> {
    private static final String TAG = GenericDeviceAdapter.class.getName();

    private Class<T> deviceClass;

    public GenericDeviceAdapter(Class<T> deviceClass) {
        this.deviceClass = deviceClass;
    }

    @Override
    protected int getOverviewLayout(T device) {
        return R.layout.device_overview_generic;
    }

    @Override
    protected void fillDeviceOverviewView(View view, T device) {
        Context context = AndFHEMApplication.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        setTextView(view, R.id.deviceName, device.getAliasOrName());

        try {
            if (device.getClass().isAnnotationPresent(ViewSettings.class)) {
                if (device.getClass().getAnnotation(ViewSettings.class).showState()) {
                    createTableRow(inflater, layout, R.string.state, device.getState(), R.layout.device_overview_generic_table_row);
                }
            }
            Field[] declaredFields = device.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(ShowInOverview.class)) {
                    addOverviewField(device, inflater, layout, declaredField);
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
                    createTableRow(inflater, layout, R.string.state, device.getState(), R.layout.device_detail_generic_table_row);
                }
            }
            Field[] declaredFields = device.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(ShowInDetail.class)) {
                    addDetailField(device, inflater, layout, declaredField);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception occurred while setting device overview values", e);
        }

        LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphButtons);
        if (device.getDeviceCharts().size() == 0 || device.getFileLog() == null) {
            graphLayout.setVisibility(View.GONE);
        }
        for (DeviceChart deviceChart : device.getDeviceCharts()) {
            addGraphButton(context, graphLayout, inflater, device, deviceChart);
        }
    }

    private void addOverviewField(T device, LayoutInflater inflater, TableLayout layout, Field declaredField) throws IllegalAccessException {
        int description = declaredField.getAnnotation(ShowInOverview.class).description();
        Object value = declaredField.get(device);

        createTableRow(inflater, layout, description, value, R.layout.device_overview_generic_table_row);
    }

    private void addDetailField(T device, LayoutInflater inflater, TableLayout layout, Field declaredField) throws IllegalAccessException {
        int description = declaredField.getAnnotation(ShowInDetail.class).description();
        Object value = declaredField.get(device);

        createTableRow(inflater, layout, description, value, R.layout.device_detail_generic_table_row);
    }

    private void createTableRow(LayoutInflater inflater, TableLayout layout, int description, Object value, int resource) {
        TableRow tableRow = (TableRow) inflater.inflate(resource, null);
        fillTableRow(description, value, tableRow);
        layout.addView(tableRow);
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
        Button button = (Button) inflater.inflate(R.layout.device_detail_generic_plotbutton, null);
        fillGraphButton(context, device, chart, button);
        graphLayout.addView(button);
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return deviceClass;
    }
}
