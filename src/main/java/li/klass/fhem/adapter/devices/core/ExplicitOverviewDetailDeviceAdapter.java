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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.genericview.DetailViewSettings;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;

import static com.google.common.collect.Maps.newHashMap;
import static li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder;

public class ExplicitOverviewDetailDeviceAdapter<D extends FhemDevice<D>> extends OverviewDeviceAdapter<D> {
    private static final String TAG = ExplicitOverviewDetailDeviceAdapter.class.getName();

    @Inject
    protected StateUiService stateUiService;

    private Class<D> deviceClass;

    private Map<String, List<FieldNameAddedToDetailListener<D>>> fieldNameAddedListeners = newHashMap();


    public ExplicitOverviewDetailDeviceAdapter(Class<D> deviceClass) {
        super();

        this.deviceClass = deviceClass;
        afterPropertiesSet();
        registerFieldListener("state", new FieldNameAddedToDetailListener<D>() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field,
                                            D device, TableRow fieldTableRow) {
                createWebCmdTableRowIfRequired(getInflater(), tableLayout, device);
            }
        });
    }

    protected void afterPropertiesSet() {
    }

    private void createWebCmdTableRowIfRequired(LayoutInflater inflater, TableLayout layout,
                                                final D device) {
        if (device.getWebCmd().isEmpty()) return;
        final Context context = inflater.getContext();

        layout.addView(new WebCmdActionRow<D>(HolderActionRow.LAYOUT_DETAIL, context)
                .createRow(context, inflater, layout, device));
    }

    @Override
    public boolean supportsDetailView(FhemDevice device) {
        return true;
    }

    @Override
    protected final View getDeviceDetailView(Context context, D device, long lastUpdate) {
        View view = getInflater().inflate(getDetailViewLayout(), null);
        fillDeviceDetailView(context, view, device);

        if (isSensorDevice(device) && isOutdatedData(device, lastUpdate)) {
            View measureErrorView = view.findViewById(R.id.measure_error_notification);
            if (measureErrorView != null) {
                measureErrorView.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    private int getDetailViewLayout() {
        return R.layout.device_detail_explicit;
    }

    protected void fillDeviceDetailView(Context context, View view, D device) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TableLayout layout = (TableLayout) view.findViewById(R.id.generic);

        try {
            DetailViewSettings annotation = device.getClass().getAnnotation(DetailViewSettings.class);
            List<DeviceViewItem> items = getSortedAnnotatedClassItems(device);

            for (DeviceViewItem item : items) {
                String name = item.getSortKey();

                if (annotation != null) {
                    if (name.equalsIgnoreCase("state") && !annotation.showState()) {
                        continue;
                    }

                    if (name.equalsIgnoreCase("measured") && !annotation.showMeasured()) {
                        continue;
                    }
                }

                if (item.isShowInDetail()) {
                    GenericDeviceTableRowHolder holder = createTableRow(inflater, R.layout.device_detail_generic_table_row);
                    fillTableRow(holder, item, device);
                    layout.addView(holder.row);
                    notifyFieldListeners(context, device, layout, name, holder.row);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception occurred while setting device overview values", e);
        }

        addDetailGraphButtons(context, view, device, inflater);

        addDetailActionButtons(context, view, device, inflater);

        LinearLayout otherStuffView = (LinearLayout) view.findViewById(R.id.otherStuff);
        fillOtherStuffDetailLayout(context, otherStuffView, device, inflater);

        updateGeneralDetailsNotificationText(context, view, device);
    }

    private void notifyFieldListeners(Context context, D device, TableLayout layout, String fieldName, TableRow fieldTableRow) {
        if (!fieldNameAddedListeners.containsKey(fieldName)) {
            return;
        }

        List<FieldNameAddedToDetailListener<D>> listeners = fieldNameAddedListeners.get(fieldName);
        for (FieldNameAddedToDetailListener<D> listener : listeners) {
            if (listener.supportsDevice(device)) {
                listener.onFieldNameAdded(context, layout, fieldName, device, fieldTableRow);
            }
        }
    }

    private void addDetailGraphButtons(Context context, View view, D device, LayoutInflater inflater) {
        LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.graphButtons);
        if (device.getSvgGraphDefinitions().isEmpty()) {
            graphLayout.setVisibility(View.GONE);
            return;
        }
        for (SvgGraphDefinition svgGraphDefinition : device.getSvgGraphDefinitions()) {
            addGraphButton(context, graphLayout, inflater, device, svgGraphDefinition);
        }
    }

    private void addDetailActionButtons(Context context, View view, D device, LayoutInflater inflater) {
        LinearLayout actionLayout = (LinearLayout) view.findViewById(R.id.actionButtons);
        List<DeviceDetailViewAction<D>> actions = provideDetailActions();
        if (!hasVisibleDetailActions(actions, device)) {
            actionLayout.setVisibility(View.GONE);
        }
        for (DeviceDetailViewAction<D> action : actions) {
            if (!action.isVisible(device)) continue;

            View actionView = action.createView(context, inflater, device, actionLayout);
            actionLayout.addView(actionView);
        }
    }

    protected void fillOtherStuffDetailLayout(Context context, LinearLayout layout, D device, LayoutInflater inflater) {
    }

    private void updateGeneralDetailsNotificationText(Context context, View view, D device) {
        String text = getGeneralDetailsNotificationText(context, device);
        TextView notificationView = (TextView) view.findViewById(R.id.general_details_notification);

        if (Strings.isNullOrEmpty(text)) {
            notificationView.setVisibility(View.GONE);
            return;
        }

        notificationView.setText(text);
        notificationView.setVisibility(View.VISIBLE);
    }

    private void addGraphButton(final Context context, LinearLayout graphLayout,
                                LayoutInflater inflater, final D device, final SvgGraphDefinition svgGraphDefinition) {
        Button button = (Button) inflater.inflate(R.layout.button_device_detail, graphLayout, false);
        assert button != null;

        fillGraphButton(context, device, svgGraphDefinition, button);
        graphLayout.addView(button);
    }

    private boolean hasVisibleDetailActions(List<DeviceDetailViewAction<D>> actions, D device) {
        if (actions.isEmpty()) return false;

        for (DeviceDetailViewAction<D> detailAction : actions) {
            if (detailAction.isVisible(device)) {
                return true;
            }
        }
        return false;
    }

    protected String getGeneralDetailsNotificationText(Context context, D device) {
        return null;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, FhemDevice device, Intent intent) {
        return intent;
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return deviceClass;
    }

    protected List<DeviceDetailViewAction<D>> provideDetailActions() {
        return Lists.newArrayList();
    }
}
