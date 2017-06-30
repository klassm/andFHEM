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

package li.klass.fhem.adapter.devices.strategy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;

import com.google.common.base.Optional;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow;
import li.klass.fhem.adapter.devices.genericui.onoff.AbstractOnOffActionRow;
import li.klass.fhem.adapter.devices.genericui.onoff.OnOffActionRowForToggleables;
import li.klass.fhem.adapter.devices.hook.ButtonHook;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;

import static li.klass.fhem.adapter.devices.genericui.ToggleActionRow.LAYOUT_OVERVIEW;
import static li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow.HOLDER_KEY;
import static li.klass.fhem.adapter.devices.hook.ButtonHook.NORMAL;
import static li.klass.fhem.adapter.devices.hook.ButtonHook.TOGGLE_DEVICE;
import static li.klass.fhem.adapter.devices.hook.ButtonHook.WEBCMD_DEVICE;

@Singleton
public class ToggleableStrategy extends ViewStrategy {
    @Inject
    DeviceHookProvider hookProvider;

    @Inject
    OnOffBehavior onOffBehavior;

    @Inject
    public ToggleableStrategy() {
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, List<DeviceViewItem> deviceItems, String connectionId) {
        ToggleableDevice device = (ToggleableDevice) rawDevice;

        if (convertView == null || convertView.getTag() == null) {
            convertView = layoutInflater.inflate(R.layout.device_overview_generic, null);
            GenericDeviceOverviewViewHolder holder = new GenericDeviceOverviewViewHolder(convertView);
            convertView.setTag(holder);
        }
        GenericDeviceOverviewViewHolder holder = (GenericDeviceOverviewViewHolder) convertView.getTag();
        holder.resetHolder();
        holder.getDeviceName().setVisibility(View.GONE);
        addOverviewSwitchActionRow(holder, device, layoutInflater, null);
        return convertView;
    }

    @Override
    public boolean supports(FhemDevice fhemDevice) {
        return hookProvider.buttonHookFor(fhemDevice) != WEBCMD_DEVICE && OnOffBehavior.supports(fhemDevice);
    }

    protected <T extends ToggleableDevice<T>> void addOverviewSwitchActionRow(GenericDeviceOverviewViewHolder holder, T device, LayoutInflater layoutInflater, String connectionId) {
        ButtonHook hook = hookProvider.buttonHookFor(device);
        if (hook != NORMAL && hook != TOGGLE_DEVICE) {
            addOnOffActionRow(holder, device, OnOffActionRowForToggleables.LAYOUT_OVERVIEW, layoutInflater, Optional.<Integer>absent(), connectionId);
        } else {
            addToggleDeviceActionRow(holder, device, LAYOUT_OVERVIEW, layoutInflater);
        }
    }

    private <T extends ToggleableDevice<T>> void addToggleDeviceActionRow(GenericDeviceOverviewViewHolder holder, T device, int layoutId, LayoutInflater layoutInflater) {
        Context context = layoutInflater.getContext();

        ToggleDeviceActionRow actionRow = holder.getAdditionalHolderFor(HOLDER_KEY);
        if (actionRow == null) {
            actionRow = new ToggleDeviceActionRow(layoutInflater, layoutId, onOffBehavior);
            holder.putAdditionalHolder(HOLDER_KEY, actionRow);
        }
        actionRow.fillWith(context, device, device.getAliasOrName());
        holder.getTableLayout().addView(actionRow.getView());
    }

    private <T extends ToggleableDevice<T>> void addOnOffActionRow(GenericDeviceOverviewViewHolder holder, T device, int layoutId, LayoutInflater layoutInflater, Optional<Integer> text, String connectionId) {
        OnOffActionRowForToggleables onOffActionRow = holder.getAdditionalHolderFor(OnOffActionRowForToggleables.HOLDER_KEY);
        if (onOffActionRow == null) {
            onOffActionRow = new OnOffActionRowForToggleables(layoutId, hookProvider, onOffBehavior, text, connectionId);
            holder.putAdditionalHolder(OnOffActionRowForToggleables.HOLDER_KEY, onOffActionRow);
        }
        holder.getTableLayout().addView(onOffActionRow
                .createRow(device, holder.getTableLayout().getContext()));
    }

    public TableRow createDetailView(GenericDevice device, Context context, String connectionId) {
        return new OnOffActionRowForToggleables(AbstractOnOffActionRow.LAYOUT_DETAIL, hookProvider, onOffBehavior, Optional.of(R.string.blank), connectionId)
                .createRow(device, context);
    }
}
