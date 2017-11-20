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

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.DevStateIconAdder;
import li.klass.fhem.adapter.devices.core.deviceItems.AnnotatedMethodsAndFieldsProvider;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItemSorter;
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.adapter.devices.strategy.DefaultViewStrategy;
import li.klass.fhem.adapter.devices.strategy.LightSceneDeviceViewStrategy;
import li.klass.fhem.adapter.devices.strategy.ViewStrategy;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.connection.backend.DataConnectionSwitch;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;
import li.klass.fhem.update.backend.device.configuration.DeviceDescMapping;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public abstract class OverviewDeviceAdapter extends DeviceAdapter {

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

    @Inject
    DefaultViewStrategy defaultOverviewStrategy;

    @Inject
    LightSceneDeviceViewStrategy lightSceneDeviceViewStrategy;

    @Inject
    DevStateIconAdder devStateIconAdder;

    private List<ViewStrategy> overviewStrategies;

    /**
     * Field to cache our sorted and annotated class members. This is especially useful as
     * recreating this list on each view creation is really really expensive (involves reflection,
     * list sorting, object creation, ...)
     */
    private transient Set<DeviceViewItem> annotatedClassItems;

    Map<String, List<FieldNameAddedToDetailListener>> fieldNameAddedListeners = newHashMap();

    @Override
    protected void onAfterInject() {
        super.onAfterInject();
        overviewStrategies = newArrayList();
        fillOverviewStrategies(overviewStrategies);
        Collections.reverse(overviewStrategies);
    }

    protected void fillOverviewStrategies(List<ViewStrategy> overviewStrategies) {
        overviewStrategies.add(defaultOverviewStrategy);
        overviewStrategies.add(lightSceneDeviceViewStrategy);
    }

    private ViewStrategy getMostSpecificOverviewStrategy(FhemDevice device) {
        for (ViewStrategy viewStrategy : overviewStrategies) {
            if (viewStrategy.supports(device)) {
                return viewStrategy;
            }
        }
        throw new IllegalStateException("no overview strategy found, default should always be present");
    }

    public final View createOverviewView(View convertView, FhemDevice rawDevice, Context context) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ViewStrategy viewStrategy = getMostSpecificOverviewStrategy(rawDevice);
        if (viewStrategy == null) {
            throw new NullPointerException("was null for device " + rawDevice.toString() + " and adapter " + getClass().getSimpleName());
        }
        LOGGER.debug("createOverviewView - viewStrategy=" + viewStrategy.getClass().getSimpleName() + ",time=" + stopWatch.getTime());
        View view = viewStrategy.createOverviewView(LayoutInflater.from(context), convertView, rawDevice, getSortedAnnotatedClassItems(rawDevice, context), null);
        LOGGER.debug("createOverviewView - finished, time=" + stopWatch.getTime());
        return view;
    }

    GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder createTableRow(LayoutInflater inflater, int resource) {
        GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder holder = new GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder();
        TableRow tableRow = (TableRow) inflater.inflate(resource, null);
        assert tableRow != null;
        holder.row = tableRow;
        holder.description = tableRow.findViewById(R.id.description);
        holder.value = tableRow.findViewById(R.id.value);
        holder.devStateIcon = tableRow.findViewById(R.id.devStateIcon);
        return holder;
    }

    void fillTableRow(GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder holder, DeviceViewItem item, FhemDevice device, Context context) {
        String value = item.getValueFor(device);
        String description = item.getName(deviceDescMapping, context);
        setTextView(holder.description, description);
        setTextView(holder.value, String.valueOf(value));
        if (value == null || value.equals("")) {
            holder.row.setVisibility(View.GONE);
        } else {
            holder.row.setVisibility(View.VISIBLE);
        }
        devStateIconAdder.addDevStateIconIfRequired(value, device, holder.devStateIcon);
    }

    List<DeviceViewItem> getSortedAnnotatedClassItems(FhemDevice device, Context context) {
        if (annotatedClassItems == null) {
            annotatedClassItems = annotatedMethodsAndFieldsProvider.generateAnnotatedClassItemsList(device.getClass());
        }
        Set<DeviceViewItem> xmlViewItems = xmlDeviceItemProvider.getDeviceClassItems(device, context);
        registerListenersFor(device, xmlViewItems);

        return deviceViewItemSorter.sortedViewItemsFrom(mergeSets(annotatedClassItems, xmlViewItems));
    }

    private Iterable<DeviceViewItem> mergeSets(Set<DeviceViewItem> annotatedClassItems, Set<DeviceViewItem> xmlViewItems) {
        Set<DeviceViewItem> result = newHashSet();
        result.addAll(annotatedClassItems);
        result.addAll(xmlViewItems);
        return result;
    }

    private void registerListenersFor(FhemDevice device, Set<DeviceViewItem> xmlViewItems) {
        for (DeviceViewItem xmlViewItem : xmlViewItems) {
            registerListenerFor(device, xmlViewItem);
        }
    }

    private void registerListenerFor(FhemDevice device, final DeviceViewItem xmlViewItem) {
        final String key = xmlViewItem.getSortKey();
        if (device.getXmlListDevice().getSetList().contains(key)) {
            registerFieldListener(key, new FieldNameAddedToDetailListener() {
                @Override
                protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, String connectionId, TableRow fieldTableRow) {
                    SetListEntry setListEntry = device.getXmlListDevice().getSetList().get(key, true);
                    if (setListEntry instanceof SliderSetListEntry) {
                        tableLayout.addView(
                                new StateChangingSeekBarFullWidth(context, stateUiService, applicationProperties, DimmableBehavior.continuousBehaviorFor(device, key, connectionId).get(), fieldTableRow)
                                        .createRow(LayoutInflater.from(context), device));
                    } else if (setListEntry instanceof GroupSetListEntry) {
                        GroupSetListEntry groupValue = (GroupSetListEntry) setListEntry;
                        tableLayout.addView(new StateChangingSpinnerActionRow(context, key, key, groupValue.getGroupStates(), xmlViewItem.getValueFor(device), key)
                                .createRow(device.getXmlListDevice(), connectionId, tableLayout));
                    }
                }
            });
        }
    }

    protected void registerFieldListener(String fieldName, FieldNameAddedToDetailListener listener) {
        fieldName = fieldName.toLowerCase(Locale.getDefault());
        if (!fieldNameAddedListeners.containsKey(fieldName)) {
            fieldNameAddedListeners.put(fieldName, new ArrayList<FieldNameAddedToDetailListener>());
        }

        fieldNameAddedListeners.get(fieldName).add(listener);
    }

    @Override
    public Class getOverviewViewHolderClass() {
        return GenericDeviceOverviewViewHolder.class;
    }
}
