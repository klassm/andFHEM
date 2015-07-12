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

package li.klass.fhem.adapter.devices.overview.strategy;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.service.deviceConfiguration.DeviceDescMapping;

@Singleton
public class DefaultOverviewStrategy extends OverviewStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOverviewStrategy.class);

    @Inject
    DataConnectionSwitch dataConnectionSwitch;

    @Inject
    DeviceDescMapping deviceDescMapping;

    @Inject
    public DefaultOverviewStrategy() {
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate, List<DeviceViewItem> deviceItems) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = layoutInflater.inflate(getOverviewLayout(), null);
            GenericDeviceOverviewViewHolder viewHolder = new GenericDeviceOverviewViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            LOGGER.info("Reusing generic device overview view");
        }
        GenericDeviceOverviewViewHolder viewHolder = (GenericDeviceOverviewViewHolder) convertView.getTag();
        fillDeviceOverviewView(convertView, rawDevice, lastUpdate, viewHolder, deviceItems, layoutInflater);
        return convertView;
    }

    private int getOverviewLayout() {
        return R.layout.device_overview_generic;
    }

    private void fillDeviceOverviewView(View view, FhemDevice device, long lastUpdate, GenericDeviceOverviewViewHolder viewHolder, List<DeviceViewItem> items, LayoutInflater layoutInflater) {
        Context context = layoutInflater.getContext();

        viewHolder.resetHolder();
        setTextView(viewHolder.getDeviceName(), device.getAliasOrName());

        try {
            OverviewViewSettings annotation = device.getOverviewViewSettingsCache();
            int currentGenericRow = 0;
            for (DeviceViewItem item : items) {
                String name = item.getSortKey();
                boolean alwaysShow = false;
                if (annotation != null) {
                    if (name.equalsIgnoreCase("state")) {
                        if (!annotation.showState()) continue;
                        alwaysShow = true;
                    }

                    if (name.equalsIgnoreCase("measured")) {
                        if (!annotation.showMeasured()) continue;
                        alwaysShow = true;
                    }
                }
                if (alwaysShow || item.isShowInOverview()) {
                    currentGenericRow++;
                    GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder rowHolder;
                    if (currentGenericRow <= viewHolder.getTableRowCount()) {
                        rowHolder = viewHolder.getTableRowAt(currentGenericRow - 1);
                    } else {
                        rowHolder = createTableRow(layoutInflater, R.layout.device_overview_generic_table_row);
                        viewHolder.addTableRow(rowHolder);
                    }
                    fillTableRow(rowHolder, item, device);
                    viewHolder.getTableLayout().addView(rowHolder.row);
                }
            }


            if (isOverviewError(device, lastUpdate, context)) {
                Resources resources = context.getResources();
                int color = resources.getColor(R.color.errorBackground);
                view.setBackgroundColor(color);
            }

        } catch (Exception e) {
            LOGGER.error("exception occurred while setting device overview values", e);
        }
    }

    protected GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder createTableRow(LayoutInflater inflater, int resource) {
        GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder holder = new GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder();
        TableRow tableRow = (TableRow) inflater.inflate(resource, null);
        assert tableRow != null;
        holder.row = tableRow;
        holder.description = (TextView) tableRow.findViewById(R.id.description);
        holder.value = (TextView) tableRow.findViewById(R.id.value);
        return holder;
    }

    protected boolean isOverviewError(FhemDevice device, long lastUpdate, Context context) {
        // It does not make sense to show measure errors for data stemming out of a prestored
        // XML file.
        boolean sensorDevice = isSensorDevice(device);
        return !(dataConnectionSwitch.isDummyDataActive(context)) &&
                lastUpdate != -1 &&
                sensorDevice &&
                isOutdatedData(device, lastUpdate);

    }


    protected boolean isSensorDevice(FhemDevice device) {
        return device.isSensorDevice() ||
                (device.getDeviceConfiguration().isPresent() && device.getDeviceConfiguration().get().isSensorDevice());
    }

    protected boolean isOutdatedData(FhemDevice device, long lastUpdateTime) {
        return device.getLastMeasureTime() != -1
                && lastUpdateTime - device.getLastMeasureTime() > FhemDevice.OUTDATED_DATA_MS_DEFAULT;

    }


    protected void fillTableRow(GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder holder, DeviceViewItem item, FhemDevice device) {
        String value = item.getValueFor(device);
        String description = item.getName(deviceDescMapping);
        setTextView(holder.description, description);
        setTextView(holder.value, String.valueOf(value));
        if (value == null || value.equals("")) {
            holder.row.setVisibility(View.GONE);
        } else {
            holder.row.setVisibility(View.VISIBLE);
        }
    }
}
