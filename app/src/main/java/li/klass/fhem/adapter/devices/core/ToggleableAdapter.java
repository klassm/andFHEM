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
import android.view.View;
import android.widget.TableLayout;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.adapter.devices.genericui.OnOffActionRow;
import li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow;
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow;
import li.klass.fhem.domain.core.ToggleableDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.TOGGLEABLE_AND_NOT_DIMMABLE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.TOGGLE_DEVICE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.WEBCMD_DEVICE;

public abstract class ToggleableAdapter<D extends ToggleableDevice<D>> extends GenericDeviceAdapter<D> {
    public ToggleableAdapter(Class<D> deviceClass) {
        super(deviceClass);
    }

    @Override
    public void fillDeviceOverviewView(View view, final D device, long lastUpdate) {
        if (!device.supportsToggle()) {
            super.fillDeviceOverviewView(view, device, lastUpdate);
            return;
        }

        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);

        addOverviewSwitchActionRow(view.getContext(), device, layout);
    }

    protected <T extends ToggleableDevice<T>> void addOverviewSwitchActionRow(Context context, T device, TableLayout layout) {
        ToggleableDevice.ButtonHookType buttonHookType = device.getButtonHookType();
        if (device.isSpecialButtonDevice() && buttonHookType != TOGGLE_DEVICE) {
            if (buttonHookType == WEBCMD_DEVICE) {
                addWebCmdOverviewActionRow(context, device, layout);
            } else {
                addSwitchActionRow(context, device, layout, OnOffActionRow.LAYOUT_OVERVIEW);
            }
        } else {
            addSwitchActionRow(context, device, layout, ToggleDeviceActionRow.LAYOUT_OVERVIEW);
        }
    }

    private <T extends ToggleableDevice<T>> void addWebCmdOverviewActionRow(Context context, T device,
                                                                            TableLayout tableLayout) {
        new WebCmdActionRow<T>(device.getAliasOrName(), HolderActionRow.LAYOUT_OVERVIEW)
                .createRow(context, getInflater(), tableLayout, device);
    }

    @SuppressWarnings("unchecked")
    private <T extends ToggleableDevice<T>> void addSwitchActionRow(Context context, T device,
                                                                    TableLayout tableLayout, int layoutId) {
        ToggleableDevice.ButtonHookType buttonHookType = device.getButtonHookType();
        if (device.isSpecialButtonDevice() && buttonHookType != TOGGLE_DEVICE) {
            tableLayout.addView(new OnOffActionRow<T>(device.getAliasOrName(), layoutId)
                    .createRow(context, getInflater(), device));
        } else {
            tableLayout.addView(new ToggleDeviceActionRow<T>(device.getAliasOrName(), layoutId)
                    .createRow(context, getInflater(), device));
        }
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
            addSwitchActionRow(context, device, layout, OnOffActionRow.LAYOUT_DETAIL);
        } else {
            addSwitchActionRow(context, device, layout, ToggleDeviceActionRow.LAYOUT_DETAIL);
        }
    }
}
