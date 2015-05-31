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
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.base.Optional;

import java.util.List;
import java.util.Set;

import li.klass.fhem.R;
import li.klass.fhem.activities.graph.ChartingActivity;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider;
import li.klass.fhem.adapter.devices.genericui.OnOffActionRow;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetListGroupValue;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

public class GenericOverviewDetailDeviceAdapter extends OverviewDeviceAdapter<GenericDevice> {
    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return GenericDevice.class;
    }

    @Override
    public boolean supportsDetailView(FhemDevice device) {
        return true;
    }

    @Override
    protected View getDeviceDetailView(Context context, final GenericDevice device, long lastUpdate) {
        LinearLayout linearLayout = (LinearLayout) getInflater().inflate(getDetailViewLayout(), null);

        fillStatesCard(device, linearLayout);
        fillAttributesCard(device, linearLayout);
        fillInternalsCard(device, linearLayout);
        fillPlotsCard(device, linearLayout);

        return linearLayout;
    }

    private void fillPlotsCard(final GenericDevice device, LinearLayout linearLayout) {
        CardView plotsCard = (CardView) linearLayout.findViewById(R.id.plotsCard);
        if (device.getSvgGraphDefinitions().isEmpty()) {
            plotsCard.setVisibility(View.GONE);
            return;
        }

        LinearLayout graphLayout = (LinearLayout) plotsCard.findViewById(R.id.plotsList);
        for (final SvgGraphDefinition svgGraphDefinition : device.getSvgGraphDefinitions()) {
            Button button = (Button) getInflater().inflate(R.layout.device_detail_card_plots_button, null);
            button.setText(svgGraphDefinition.getName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChartingActivity.showChart(getContext(), device, svgGraphDefinition);
                }
            });
            graphLayout.addView(button);
        }
    }

    private void fillStatesCard(final GenericDevice device, LinearLayout linearLayout) {
        fillCard(device, linearLayout, R.id.statesCard, R.string.detailStatesSection, new StateItemProvider());
    }

    private void fillAttributesCard(final GenericDevice device, LinearLayout linearLayout) {
        fillCard(device, linearLayout, R.id.attributesCard, R.string.detailAttributesSection, new AttributeItemProvider());
    }

    private void fillInternalsCard(final GenericDevice device, LinearLayout linearLayout) {
        fillCard(device, linearLayout, R.id.internalsCard, R.string.detailInternalsSection, new InternalsItemProvider());
    }

    private void fillCard(final GenericDevice device, LinearLayout linearLayout, int cardId, int caption, final ItemProvider itemProvider) {
        CardView statesCard = (CardView) linearLayout.findViewById(cardId);

        boolean hasConfiguration = device.getDeviceConfiguration().isPresent();

        TextView statesCaption = (TextView) statesCard.findViewById(R.id.cardCaption);
        statesCaption.setText(caption);

        final TableLayout table = (TableLayout) statesCard.findViewById(R.id.table);
        fillTable(device, table, itemProvider, !hasConfiguration);

        final Button button = (Button) statesCard.findViewById(R.id.expandButton);
        button.setVisibility(hasConfiguration ? View.VISIBLE : View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTable(device, table, itemProvider, true);
                button.setVisibility(View.GONE);
            }
        });
    }

    private void fillTable(GenericDevice device, TableLayout table, ItemProvider itemProvider, boolean showUnknown) {
        List<DeviceViewItem> items = getSortedClassItems(device, itemProvider, showUnknown);
        table.removeAllViews();

        for (DeviceViewItem item : items) {
            GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder holder = createTableRow(getInflater(), R.layout.device_detail_generic_table_row);
            fillTableRow(holder, item, device);
            table.addView(holder.row);
            addActionIfRequired(device, table, item, holder.row);
        }

        table.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void addActionIfRequired(GenericDevice device, TableLayout table, DeviceViewItem item, TableRow row) {
        if (item.getSortKey().equalsIgnoreCase("state") && device.getSetList().contains("on", "off")) {
            table.addView(new OnOffActionRow<GenericDevice>(OnOffActionRow.LAYOUT_DETAIL, Optional.<Integer>absent())
                    .createRow(getInflater(), device, getContext()));
            return;
        }

        SetListValue setListValue = device.getSetList().get(item.getSortKey());
        if (setListValue == null) {
            return;
        }

        if (setListValue instanceof SetListSliderValue) {
            SetListSliderValue sliderValue = (SetListSliderValue) setListValue;
            TableRow sliderRow = new StateChangingSeekBarFullWidth<GenericDevice>(
                    getContext(), extractLeadingInt(item.getValueFor(device)),
                    sliderValue, item.getSortKey(), row, applicationProperties)
                    .createRow(getInflater(), device);
            table.addView(sliderRow);
        } else if (setListValue instanceof SetListGroupValue) {
            SetListGroupValue groupValue = (SetListGroupValue) setListValue;
            List<String> groupStates = groupValue.getGroupStates();
            if (groupStates.contains("on") && groupStates.contains("off")) {
                table.addView(new OnOffActionRow<GenericDevice>(OnOffActionRow.LAYOUT_DETAIL, Optional.<Integer>absent())
                        .createRow(getInflater(), device, getContext()));
            }
        }
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, FhemDevice device, Intent intent) {
        return intent;
    }

    private int getDetailViewLayout() {
        return R.layout.device_detail_generic;
    }


    private List<DeviceViewItem> getSortedClassItems(FhemDevice device, ItemProvider itemProvider, boolean showUnknown) {
        Set<DeviceViewItem> xmlViewItems = itemProvider.itemsFor(xmlDeviceItemProvider, device, showUnknown);
        return deviceViewItemSorter.sortedViewItemsFrom(xmlViewItems);
    }

    private interface ItemProvider {
        Set<DeviceViewItem> itemsFor(XmlDeviceItemProvider provider, FhemDevice device, boolean showUnknown);
    }

    private static class StateItemProvider implements ItemProvider {
        public Set<DeviceViewItem> itemsFor(XmlDeviceItemProvider provider, FhemDevice device, boolean showUnknown) {
            return provider.getStatesFor(device, showUnknown);
        }
    }

    private static class AttributeItemProvider implements ItemProvider {
        public Set<DeviceViewItem> itemsFor(XmlDeviceItemProvider provider, FhemDevice device, boolean showUnknown) {
            return provider.getAttributesFor(device, showUnknown);
        }
    }

    private static class InternalsItemProvider implements ItemProvider {
        public Set<DeviceViewItem> itemsFor(XmlDeviceItemProvider provider, FhemDevice device, boolean showUnknown) {
            return provider.getInternalsFor(device, showUnknown);
        }
    }
}
