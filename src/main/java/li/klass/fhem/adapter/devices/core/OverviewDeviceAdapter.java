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
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.deviceItems.AnnotatedMethodsAndFieldsProvider;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItemSorter;
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.setlist.SetListGroupValue;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.fhem.DummyDataConnection;
import li.klass.fhem.service.deviceConfiguration.DeviceDescMapping;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Maps.newHashMap;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

public abstract class OverviewDeviceAdapter<D extends FhemDevice<D>> extends DeviceAdapter<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OverviewDeviceAdapter.class);

    @Inject
    DataConnectionSwitch dataConnectionSwitch;

    @Inject
    protected StateUiService stateUiService;

    @Inject
    DeviceViewItemSorter deviceViewItemSorter;

    @Inject
    AnnotatedMethodsAndFieldsProvider annotatedMethodsAndFieldsProvider;

    @Inject
    XmlDeviceItemProvider xmlDeviceItemProvider;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    DeviceDescMapping deviceDescMapping;

    /**
     * Field to cache our sorted and annotated class members. This is especially useful as
     * recreating this list on each view creation is really really expensive (involves reflection,
     * list sorting, object creation, ...)
     */
    protected transient Set<DeviceViewItem> annotatedClassItems;

    private Map<String, List<FieldNameAddedToDetailListener<D>>> fieldNameAddedListeners = newHashMap();

    @SuppressWarnings("unchecked")
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = layoutInflater.inflate(getOverviewLayout(), null);
            GenericDeviceOverviewViewHolder viewHolder = new GenericDeviceOverviewViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            LOGGER.info("Reusing generic device overview view");
        }
        GenericDeviceOverviewViewHolder viewHolder = (GenericDeviceOverviewViewHolder) convertView.getTag();
        fillDeviceOverviewView(convertView, (D) rawDevice, lastUpdate, viewHolder);
        return convertView;
    }


    private int getOverviewLayout() {
        return R.layout.device_overview_generic;
    }

    private void fillDeviceOverviewView(View view, D device, long lastUpdate, GenericDeviceOverviewViewHolder viewHolder) {
        viewHolder.resetHolder();
        setTextView(viewHolder.getDeviceName(), device.getAliasOrName());
        try {
            OverviewViewSettings annotation = device.getOverviewViewSettingsCache();
            List<DeviceViewItem> items = getSortedAnnotatedClassItems(device);
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
                        rowHolder = createTableRow(getInflater(), R.layout.device_overview_generic_table_row);
                        viewHolder.addTableRow(rowHolder);
                    }
                    fillTableRow(rowHolder, item, device);
                    viewHolder.getTableLayout().addView(rowHolder.row);
                }
            }

            if (isOverviewError(device, lastUpdate)) {
                Resources resources = getContext().getResources();
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

    protected boolean isOverviewError(D device, long lastUpdate) {
        // It does not make sense to show measure errors for data stemming out of a prestored
        // XML file.
        boolean sensorDevice = isSensorDevice(device);
        return !(dataConnectionSwitch.getCurrentProvider(getContext()) instanceof DummyDataConnection) &&
                lastUpdate != -1 &&
                sensorDevice &&
                isOutdatedData(device, lastUpdate);

    }

    protected boolean isSensorDevice(D device) {
        return device.isSensorDevice() ||
                (device.getDeviceConfiguration().isPresent() && device.getDeviceConfiguration().get().isSensorDevice());
    }

    protected boolean isOutdatedData(D device, long lastUpdateTime) {
        return device.getLastMeasureTime() != -1
                && lastUpdateTime - device.getLastMeasureTime() > FhemDevice.OUTDATED_DATA_MS_DEFAULT;

    }

    protected void fillTableRow(GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder holder, DeviceViewItem item, D device) {
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

    protected List<DeviceViewItem> getSortedAnnotatedClassItems(D device) {
        if (annotatedClassItems == null) {
            annotatedClassItems = annotatedMethodsAndFieldsProvider.generateAnnotatedClassItemsList(device.getClass());
        }
        Set<DeviceViewItem> xmlViewItems = xmlDeviceItemProvider.getDeviceClassItems(device);
        registerListenersFor(device, xmlViewItems);

        return deviceViewItemSorter.sortedViewItemsFrom(concat(annotatedClassItems, xmlViewItems));
    }


    protected void registerListenersFor(D device, Set<DeviceViewItem> xmlViewItems) {
        for (DeviceViewItem xmlViewItem : xmlViewItems) {
            registerListenerFor(device, xmlViewItem);
        }
    }

    private void registerListenerFor(D device, final DeviceViewItem xmlViewItem) {
        final String key = xmlViewItem.getSortKey();
        if (device.getSetList().contains(key)) {
            registerFieldListener(key, new FieldNameAddedToDetailListener<D>() {
                @Override
                protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, D device, TableRow fieldTableRow) {
                    SetListValue setListValue = device.getSetList().get(key);
                    int state = extractLeadingInt(xmlViewItem.getValueFor(device));
                    if (setListValue instanceof SetListSliderValue) {
                        SetListSliderValue sliderValue = (SetListSliderValue) setListValue;
                        tableLayout.addView(
                                new StateChangingSeekBarFullWidth<D>(getContext(), state, sliderValue, key, fieldTableRow, applicationProperties)
                                        .createRow(getInflater(), device));
                    } else if (setListValue instanceof SetListGroupValue) {
                        SetListGroupValue groupValue = (SetListGroupValue) setListValue;
                        tableLayout.addView(new StateChangingSpinnerActionRow<D>(getContext(), key, key, groupValue.getGroupStates(), xmlViewItem.getValueFor(device), key)
                                .createRow(device, tableLayout));
                    }
                }
            });
        }
    }

    protected void registerFieldListener(String fieldName, FieldNameAddedToDetailListener<D> listener) {
        if (!fieldNameAddedListeners.containsKey(fieldName)) {
            fieldNameAddedListeners.put(fieldName, new ArrayList<FieldNameAddedToDetailListener<D>>());
        }

        fieldNameAddedListeners.get(fieldName).add(listener);
    }

    @Override
    public Class getOverviewViewHolderClass() {
        return GenericDeviceOverviewViewHolder.class;
    }
}
