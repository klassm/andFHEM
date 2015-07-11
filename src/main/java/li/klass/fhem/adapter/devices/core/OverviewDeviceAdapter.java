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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
import li.klass.fhem.adapter.devices.overview.strategy.DefaultOverviewStrategy;
import li.klass.fhem.adapter.devices.overview.strategy.OverviewStrategy;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
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

    @Inject
    DefaultOverviewStrategy defaultOverviewStrategy;

    /**
     * Field to cache our sorted and annotated class members. This is especially useful as
     * recreating this list on each view creation is really really expensive (involves reflection,
     * list sorting, object creation, ...)
     */
    protected transient Set<DeviceViewItem> annotatedClassItems;

    protected Map<String, List<FieldNameAddedToDetailListener<D>>> fieldNameAddedListeners = newHashMap();

    public OverviewStrategy getOverviewStrategy() {
        return defaultOverviewStrategy;
    }

    public final View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate) {
        return getOverviewStrategy().createOverviewView(layoutInflater, convertView, rawDevice, lastUpdate, getSortedAnnotatedClassItems(rawDevice));
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

    protected List<DeviceViewItem> getSortedAnnotatedClassItems(FhemDevice device) {
        if (annotatedClassItems == null) {
            annotatedClassItems = annotatedMethodsAndFieldsProvider.generateAnnotatedClassItemsList(device.getClass());
        }
        Set<DeviceViewItem> xmlViewItems = xmlDeviceItemProvider.getDeviceClassItems(device);
        registerListenersFor(device, xmlViewItems);

        return deviceViewItemSorter.sortedViewItemsFrom(concat(annotatedClassItems, xmlViewItems));
    }


    protected void registerListenersFor(FhemDevice device, Set<DeviceViewItem> xmlViewItems) {
        for (DeviceViewItem xmlViewItem : xmlViewItems) {
            registerListenerFor(device, xmlViewItem);
        }
    }

    private void registerListenerFor(FhemDevice device, final DeviceViewItem xmlViewItem) {
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
