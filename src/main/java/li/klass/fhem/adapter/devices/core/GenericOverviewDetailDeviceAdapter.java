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
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.activities.graph.ChartingActivity;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProvider;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.StateAttributeAction;
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchAction;
import li.klass.fhem.adapter.devices.genericui.OnOffActionRow;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.adapter.devices.strategy.DimmableStrategy;
import li.klass.fhem.adapter.devices.strategy.ToggleableStrategy;
import li.klass.fhem.adapter.devices.strategy.ViewStrategy;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetListGroupValue;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.util.Optionals;

import static com.google.common.collect.FluentIterable.from;

public class GenericOverviewDetailDeviceAdapter extends OverviewDeviceAdapter {

    private Animation animation;

    @Inject
    ToggleableStrategy toggleableStrategy;

    @Inject
    DimmableStrategy dimmableStrategy;

    @Inject
    OnOffBehavior onOffBehavior;

    @Inject
    DeviceHookProvider deviceHookProvider;

    @Inject
    Set<GenericDetailActionProvider> detailActionProviders;

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return GenericDevice.class;
    }

    @Override
    public boolean supportsDetailView(FhemDevice device) {
        return true;
    }

    @Override
    protected View getDeviceDetailView(Context context, final FhemDevice device, long lastUpdate) {
        GenericDevice genericDevice = (GenericDevice) device;

        List<GenericDetailActionProvider> providers = from(detailActionProviders)
                .filter(unsupportedDetailActions(genericDevice))
                .toList();

        LinearLayout linearLayout = (LinearLayout) getInflater().inflate(getDetailViewLayout(), null);
        fillStatesCard(genericDevice, linearLayout, providers);
        fillAttributesCard(genericDevice, linearLayout);
        fillInternalsCard(genericDevice, linearLayout);
        fillPlotsCard(genericDevice, linearLayout);
        fillActionsCard(genericDevice, linearLayout, providers);

        return linearLayout;
    }

    private void fillActionsCard(final GenericDevice genericDevice, final LinearLayout linearLayout, List<GenericDetailActionProvider> detailActionProviders) {
        CardView actionsCard = (CardView) linearLayout.findViewById(R.id.actionsCard);
        if (genericDevice.getSetList().size() == 0) {
            actionsCard.setVisibility(View.GONE);
            return;
        }

        LinearLayout layout = (LinearLayout) actionsCard.findViewById(R.id.actionsList);
        layout.addView(new AvailableTargetStatesSwitchAction().createView(getContext(), getInflater(), genericDevice, linearLayout));

        ImmutableList<View> views = from(detailActionProviders)
                .transformAndConcat(actionProviderToActions())
                .filter(supportsAction(genericDevice))
                .transform(toDetailActionView(genericDevice, linearLayout))
                .toList();

        for (View view : views) {
            layout.addView(view);
        }
    }

    private Predicate<? super ActionCardAction> supportsAction(final GenericDevice genericDevice) {
        return new Predicate<ActionCardAction>() {
            @Override
            public boolean apply(ActionCardAction input) {
                return input.supports(genericDevice);
            }
        };
    }

    @NonNull
    private Function<ActionCardAction, View> toDetailActionView(final GenericDevice genericDevice, final LinearLayout linearLayout) {
        return new Function<ActionCardAction, View>() {
            @Override
            public View apply(ActionCardAction input) {
                return input.createView(genericDevice.getXmlListDevice(), getContext(), getInflater(), linearLayout);
            }
        };
    }

    @NonNull
    private Function<GenericDetailActionProvider, Iterable<ActionCardAction>> actionProviderToActions() {
        return new Function<GenericDetailActionProvider, Iterable<ActionCardAction>>() {
            @Override
            public Iterable<ActionCardAction> apply(GenericDetailActionProvider input) {
                return input.actionsFor(getContext());
            }
        };
    }

    @NonNull
    private Predicate<GenericDetailActionProvider> unsupportedDetailActions(final GenericDevice genericDevice) {
        return new Predicate<GenericDetailActionProvider>() {
            @Override
            public boolean apply(GenericDetailActionProvider input) {
                return input.supports(genericDevice.getXmlListDevice());
            }
        };
    }

    private void fillPlotsCard(final GenericDevice device, LinearLayout linearLayout) {
        CardView plotsCard = (CardView) linearLayout.findViewById(R.id.plotsCard);
        if (device.getSvgGraphDefinitions().isEmpty()) {
            plotsCard.setVisibility(View.GONE);
            return;
        }

        LinearLayout graphLayout = (LinearLayout) plotsCard.findViewById(R.id.plotsList);
        for (final SvgGraphDefinition svgGraphDefinition : device.getSvgGraphDefinitions()) {
            Button button = (Button) getInflater().inflate(R.layout.device_detail_card_plots_button, graphLayout, false);
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

    private void fillStatesCard(final GenericDevice device, LinearLayout linearLayout, List<GenericDetailActionProvider> providers) {
        fillCard(device, linearLayout, R.id.statesCard, R.string.detailStatesSection, new StateItemProvider(), providers);
    }

    private void fillAttributesCard(final GenericDevice device, LinearLayout linearLayout) {
        fillCard(device, linearLayout, R.id.attributesCard, R.string.detailAttributesSection, new AttributeItemProvider(), Collections.<GenericDetailActionProvider>emptyList());
    }

    private void fillInternalsCard(final GenericDevice device, LinearLayout linearLayout) {
        fillCard(device, linearLayout, R.id.internalsCard, R.string.detailInternalsSection, new InternalsItemProvider(), Collections.<GenericDetailActionProvider>emptyList());
    }

    private void fillCard(final GenericDevice device, LinearLayout linearLayout, int cardId, int caption, final ItemProvider itemProvider, final List<GenericDetailActionProvider> providers) {
        CardView card = (CardView) linearLayout.findViewById(cardId);

        boolean hasConfiguration = device.getDeviceConfiguration().isPresent();

        TextView captionTextView = (TextView) card.findViewById(R.id.cardCaption);
        captionTextView.setText(caption);

        final TableLayout table = (TableLayout) card.findViewById(R.id.table);

        boolean showExpandButton = hasConfiguration;
        List<DeviceViewItem> itemsToShow = getSortedClassItems(device, itemProvider, false);
        List<DeviceViewItem> allItems = getSortedClassItems(device, itemProvider, true);
        if (itemsToShow.isEmpty() || itemsToShow.size() == allItems.size()) {
            itemsToShow = allItems;
            showExpandButton = false;
        }
        fillTable(device, table, itemsToShow, providers);

        final Button button = (Button) card.findViewById(R.id.expandButton);
        button.setVisibility(showExpandButton ? View.VISIBLE : View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTable(device, table, getSortedClassItems(device, itemProvider, true), providers);
                button.setVisibility(View.GONE);
            }
        });

        if (itemsToShow.isEmpty()) {
            card.setVisibility(View.GONE);
        }
    }

    private void fillTable(GenericDevice device, TableLayout table, List<DeviceViewItem> items, List<GenericDetailActionProvider> providers) {
        table.removeAllViews();

        for (DeviceViewItem item : items) {
            GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder holder = createTableRow(getInflater(), R.layout.device_detail_generic_table_row);
            fillTableRow(holder, item, device);
            addRow(table, holder.row);
            addActionIfRequired(device, table, item, holder.row, providers);
        }

        table.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void addRow(TableLayout table, TableRow row) {
        row.setAnimation(animation);
        table.addView(row);
    }

    private void addActionIfRequired(GenericDevice device, TableLayout table, final DeviceViewItem item, TableRow row, List<GenericDetailActionProvider> providers) {
        List<StateAttributeAction> attributeActions = from(providers)
                .transform(new Function<GenericDetailActionProvider, Optional<StateAttributeAction>>() {
                    @Override
                    public Optional<StateAttributeAction> apply(GenericDetailActionProvider input) {
                        return input.stateAttributeActionFor(item);
                    }
                })
                .filter(Optionals.PRESENT)
                .transform(Optionals.<StateAttributeAction>get()).toList();

        if (!attributeActions.isEmpty()) {
            for (StateAttributeAction action : attributeActions) {
                if (action.supports(device.getXmlListDevice())) {
                    addRow(table, action.createRow(device.getXmlListDevice(), item.getKey(), item.getValueFor(device), getContext(), getInflater(), table));
                    return;
                }
            }
        }

        if (item.getSortKey().equalsIgnoreCase("state")) {
            if (dimmableStrategy.supports(device)) {
                addRow(table, dimmableStrategy.createDetailView(device, row, getInflater(), getContext()));
            } else if (toggleableStrategy.supports(device)) {
                addRow(table, toggleableStrategy.createDetailView(device, row, getInflater(), getContext()));
            }
            if (!device.getWebCmd().isEmpty()) {
                addRow(table, new WebCmdActionRow(WebCmdActionRow.LAYOUT_DETAIL, getContext()).createRow(getContext(), getInflater(), table, device));
            }
            return;
        }

        SetListValue setListValue = device.getSetList().get(item.getKey());
        if (setListValue == null) {
            return;
        }

        if (setListValue instanceof SetListSliderValue) {
            TableRow sliderRow = new StateChangingSeekBarFullWidth(
                    getContext(), stateUiService, applicationProperties, DimmableBehavior.continuousBehaviorFor(device, item.getKey()).get(), row)
                    .createRow(getInflater(), device);
            addRow(table, sliderRow);
        } else if (setListValue instanceof SetListGroupValue) {
            SetListGroupValue groupValue = (SetListGroupValue) setListValue;
            List<String> groupStates = groupValue.getGroupStates();
            if (groupStates.size() <= 1) return;

            if (groupStates.contains("on") && groupStates.contains("off") && groupStates.size() < 5) {
                addRow(table, new OnOffActionRow(OnOffActionRow.LAYOUT_DETAIL, Optional.<Integer>absent())
                        .createRow(getInflater(), device, getContext()));
            } else {
                addRow(table, new StateChangingSpinnerActionRow(getContext(), null, item.getName(deviceDescMapping), groupStates, item.getValueFor(device), item.getKey())
                        .createRow(device.getXmlListDevice(), table));
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

    @Override
    protected void fillOverviewStrategies(List<ViewStrategy> overviewStrategies) {
        super.fillOverviewStrategies(overviewStrategies);
        overviewStrategies.add(toggleableStrategy);
        overviewStrategies.add(dimmableStrategy);
    }
}
