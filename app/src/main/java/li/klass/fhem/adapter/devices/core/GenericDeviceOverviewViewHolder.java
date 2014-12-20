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
 *  Free Software Foundation, Inc.
 *  51 Franklin Street, Fifth Floor
 *  Boston, MA  02110-1301  USA
 */

package li.klass.fhem.adapter.devices.core;

import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import li.klass.fhem.R;

public class GenericDeviceOverviewViewHolder {


    private TableLayout tableLayout;
    private TableRow deviceNameRow;
    private TextView deviceName;
    private List<GenericDeviceTableRowHolder> tableRows = Lists.newArrayList();
    private Map<String, Object> additionalHolders = Maps.newHashMap();

    public GenericDeviceOverviewViewHolder(View convertView) {
        tableLayout = (TableLayout) convertView.findViewById(R.id.device_overview_generic);
        deviceName = (TextView) convertView.findViewById(R.id.deviceName);
        deviceNameRow = (TableRow) convertView.findViewById(R.id.overviewRow);
    }

    public static class GenericDeviceTableRowHolder {
        TableRow row;
        TextView description;
        TextView value;

    }

    public void resetHolder() {
        deviceName.setVisibility(View.VISIBLE);
        tableLayout.removeAllViews();
        tableLayout.addView(deviceNameRow);
    }

    public TableLayout getTableLayout() {
        return tableLayout;
    }

    public TextView getDeviceName() {
        return deviceName;
    }

    public void addTableRow(GenericDeviceTableRowHolder row) {
        tableRows.add(row);
    }

    public GenericDeviceTableRowHolder getTableRowAt(int index) {
        return tableRows.get(index);
    }

    public int getTableRowCount() {
        return tableRows.size();
    }

    @SuppressWarnings("unchecked")
    public <T> T getAdditionalHolderFor(String key) {
        return (T) additionalHolders.get(key);
    }

    public void putAdditionalHolder(String key, Object value) {
        additionalHolders.put(key, value);
    }
}
