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

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.adapter.devices.genericui.OnOffActionRowForToggleables;
import li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow;
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.TOGGLEABLE_AND_NOT_DIMMABLE;
import static li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow.HOLDER_KEY;
import static li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow.LAYOUT_DETAIL;
import static li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow.LAYOUT_OVERVIEW;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.TOGGLE_DEVICE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.WEBCMD_DEVICE;

public abstract class ToggleableAdapter<D extends ToggleableDevice<D>> extends GenericDeviceAdapter<D> {
    public ToggleableAdapter(Class<D> deviceClass) {
        super(deviceClass);
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate) {
        ToggleableDevice device = (ToggleableDevice) rawDevice;
        if (!device.supportsToggle()) {
            return super.createOverviewView(layoutInflater, convertView, rawDevice, lastUpdate);
        }
        if (convertView == null || convertView.getTag() == null) {
            convertView = layoutInflater.inflate(R.layout.device_overview_generic, null);
            GenericDeviceOverviewViewHolder holder = new GenericDeviceOverviewViewHolder(convertView);
            convertView.setTag(holder);
        }
        GenericDeviceOverviewViewHolder holder = (GenericDeviceOverviewViewHolder) convertView.getTag();
        holder.resetHolder();
        holder.getDeviceName().setVisibility(View.GONE);
        addOverviewSwitchActionRow(holder, device);
        return convertView;
    }

    protected <T extends ToggleableDevice<T>> void addOverviewSwitchActionRow(GenericDeviceOverviewViewHolder holder, T device) {
        TableLayout layout = holder.getTableLayout();
        ToggleableDevice.ButtonHookType buttonHookType = device.getButtonHookType();
        if (device.isSpecialButtonDevice() && buttonHookType != TOGGLE_DEVICE) {
            if (buttonHookType == WEBCMD_DEVICE) {
                addWebCmdOverviewActionRow(layout.getContext(), device, layout);
            } else {
                addOnOffActionRow(holder, device, OnOffActionRowForToggleables.LAYOUT_OVERVIEW);
            }
        } else {
            addToggleDeviceActionRow(holder, device, LAYOUT_OVERVIEW);
        }
    }

    private <T extends ToggleableDevice<T>> void addWebCmdOverviewActionRow(Context context, T device,
                                                                            TableLayout tableLayout) {
        tableLayout.addView(new WebCmdActionRow<T>(device.getAliasOrName(), HolderActionRow.LAYOUT_OVERVIEW)
                .createRow(context, getInflater(), tableLayout, device));
    }

    private <T extends ToggleableDevice<T>> void addToggleDeviceActionRow(GenericDeviceOverviewViewHolder holder, T device, int layoutId) {
        ToggleDeviceActionRow<T> actionRow = holder.getAdditionalHolderFor(HOLDER_KEY);
        if (actionRow == null) {
            actionRow = new ToggleDeviceActionRow<>(getInflater(), layoutId);
            holder.putAdditionalHolder(HOLDER_KEY, actionRow);
        }
        actionRow.fillWith(getContext(), device, device.getAliasOrName());
        holder.getTableLayout().addView(actionRow.getView());
    }

    @SuppressWarnings("unchecked")
    private <T extends ToggleableDevice<T>> void addToggleDeviceActionRow(Context context, T device,
                                                                          TableLayout tableLayout, int layoutId) {
        tableLayout.addView(new ToggleDeviceActionRow<T>(getInflater(), layoutId)
                .createRow(context, device, device.getAliasOrName()));
    }

    private <T extends ToggleableDevice<T>> void addOnOffActionRow(Context context, T device, TableLayout tableLayout, int layoutId) {
        tableLayout.addView(new OnOffActionRowForToggleables<T>(layoutId)
                .createRow(getInflater(), device, context));
    }

    private <T extends ToggleableDevice<T>> void addOnOffActionRow(GenericDeviceOverviewViewHolder holder, T device, int layoutId) {
        OnOffActionRowForToggleables<T> onOffActionRow = holder.getAdditionalHolderFor(OnOffActionRowForToggleables.HOLDER_KEY);
        if (onOffActionRow == null) {
            onOffActionRow = new OnOffActionRowForToggleables<>(layoutId);
            holder.putAdditionalHolder(OnOffActionRowForToggleables.HOLDER_KEY, onOffActionRow);
        }
        holder.getTableLayout().addView(onOffActionRow
                .createRow(getInflater(), device, holder.getTableLayout().getContext()));
    }

    @Override
    protected void afterPropertiesSet() {
        registerFieldListener("state", new FieldNameAddedToDetailListener<D>(TOGGLEABLE_AND_NOT_DIMMABLE) {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, D device, android.widget.TableRow fieldTableRow) {
                if (!device.supportsToggle()) {
                    return;
                }
                addDetailSwitchActionRow(context, device, tableLayout);
            }
        });
    }

    protected <T extends ToggleableDevice<T>> void addDetailSwitchActionRow(Context context, T device, TableLayout layout) {
        ToggleableDevice.ButtonHookType buttonHookType = device.getButtonHookType();
        if (device.isSpecialButtonDevice() && buttonHookType != TOGGLE_DEVICE) {
            addOnOffActionRow(context, device, layout, OnOffActionRowForToggleables.LAYOUT_DETAIL);
        } else {
            addToggleDeviceActionRow(context, device, layout, LAYOUT_DETAIL);
        }
    }

    @Override
    public Class getOverviewViewHolderClass() {
        return GenericDeviceOverviewViewHolder.class;
    }
}
