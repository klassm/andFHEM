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
import android.widget.TableLayout;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;

import static li.klass.fhem.adapter.devices.hook.ButtonHook.WEBCMD_DEVICE;

@Singleton
public class WebcmdStrategy extends ViewStrategy {
    @Inject
    DeviceHookProvider hookProvider;

    @Inject
    public WebcmdStrategy() {
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
        addOverviewSwitchActionRow(holder, device, connectionId);
        return convertView;
    }

    @Override
    public boolean supports(FhemDevice fhemDevice) {
        return hookProvider.buttonHookFor(fhemDevice) == WEBCMD_DEVICE;
    }

    private <T extends ToggleableDevice> void addOverviewSwitchActionRow(GenericDeviceOverviewViewHolder holder, T device, String connectionId) {
        TableLayout layout = holder.getTableLayout();
        addWebCmdOverviewActionRow(layout.getContext(), device, layout, connectionId);
    }

    private <T extends ToggleableDevice> void addWebCmdOverviewActionRow(Context context, T device,
                                                                         TableLayout tableLayout, String connectionId) {
        tableLayout.addView(new WebCmdActionRow(device.getAliasOrName(), HolderActionRow.LAYOUT_OVERVIEW)
                .createRow(context, tableLayout, device, connectionId));
    }
}
