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
import android.widget.TableLayout;

import javax.inject.Inject;

import li.klass.fhem.adapter.devices.genericui.OnOffActionRowForToggleables;
import li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow;
import li.klass.fhem.adapter.devices.overview.strategy.OverviewStrategy;
import li.klass.fhem.adapter.devices.overview.strategy.ToggleableOverviewStrategy;
import li.klass.fhem.domain.core.ToggleableDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.TOGGLEABLE_AND_NOT_DIMMABLE;
import static li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow.LAYOUT_DETAIL;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.TOGGLE_DEVICE;

public class ToggleableAdapter<D extends ToggleableDevice<D>> extends ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow<D> {
    @Inject
    ToggleableOverviewStrategy toggleableOverviewStrategy;

    public ToggleableAdapter(Class<D> deviceClass) {
        super(deviceClass);
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

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();
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

    @Override
    public OverviewStrategy getOverviewStrategy() {
        return toggleableOverviewStrategy;
    }
}
