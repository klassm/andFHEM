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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.OnOffActionRow;
import li.klass.fhem.adapter.devices.genericui.ToggleActionRow;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.util.device.FloorplanUtil;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.TOGGLEABLE_AND_NOT_DIMMABLE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.TOGGLE_DEVICE;

public class ToggleableAdapter<D extends ToggleableDevice<D>> extends GenericDeviceAdapter<D> {
    public ToggleableAdapter(Class<D> deviceClass) {
        super(deviceClass);
    }

    @Override
    public void fillDeviceOverviewView(View view, final D device) {
        if (!device.supportsToggle()) {
            super.fillDeviceOverviewView(view, device);
            return;
        }

        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        layout.findViewById(R.id.deviceName).setVisibility(View.GONE);

        addOverviewSwitchActionRow(view.getContext(), device, layout);
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

    protected <T extends ToggleableDevice<D>> void addOverviewSwitchActionRow(Context context, T device, TableLayout layout) {
        if (device.isSpecialButtonDevice() && device.getButtonHookType() != TOGGLE_DEVICE) {
            addSwitchActionRow(context, device, layout, OnOffActionRow.LAYOUT_OVERVIEW);
        } else {
            addSwitchActionRow(context, device, layout, ToggleActionRow.LAYOUT_OVERVIEW);
        }
    }

    protected <T extends ToggleableDevice<D>> void addDetailSwitchActionRow(Context context, T device, TableLayout layout) {
        if (device.isSpecialButtonDevice() && device.getButtonHookType() != TOGGLE_DEVICE) {
            addSwitchActionRow(context, device, layout, OnOffActionRow.LAYOUT_DETAIL);
        } else {
            addSwitchActionRow(context, device, layout, ToggleActionRow.LAYOUT_DETAIL);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ToggleableDevice<D>> void addSwitchActionRow(Context context, T device, TableLayout layout, int rowId) {
        if (device.isSpecialButtonDevice() && device.getButtonHookType() != TOGGLE_DEVICE) {
            layout.addView(new OnOffActionRow<T>(device.getAliasOrName(), rowId)
                    .createRow(context, inflater, device));
        } else {
            layout.addView(new ToggleActionRow<T>(device.getAliasOrName(), rowId)
                    .createRow(context, inflater, device));
        }
    }

    @Override
    protected void fillFloorplanView(final Context context, final D device, LinearLayout layout, FloorplanViewSettings viewSettings) {
        ImageView buttonView = FloorplanUtil.createSwitchStateBasedImageView(context, device);
        layout.addView(buttonView);
    }
}
