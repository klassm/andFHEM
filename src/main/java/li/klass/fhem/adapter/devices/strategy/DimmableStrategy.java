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
import li.klass.fhem.adapter.devices.genericui.DimActionRow;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.hook.ButtonHook;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.behavior.dim.DimmableBehavior.isDimDisabled;

@Singleton
public class DimmableStrategy extends ViewStrategy {
    @Inject
    DeviceHookProvider deviceHookProvider;

    @Inject
    StateUiService stateUiService;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    public DimmableStrategy() {
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate, List<DeviceViewItem> deviceItems, String connectionId) {
        DimmableDevice<?> device = (DimmableDevice<?>) rawDevice;

        if (convertView == null || convertView.getTag() == null) {
            convertView = layoutInflater.inflate(R.layout.device_overview_generic, null);
            GenericDeviceOverviewViewHolder holder = new GenericDeviceOverviewViewHolder(convertView);
            convertView.setTag(holder);
        }

        GenericDeviceOverviewViewHolder holder = (GenericDeviceOverviewViewHolder) convertView.getTag();
        holder.resetHolder();
        holder.getDeviceName().setVisibility(View.GONE);
        DimActionRow row = holder.getAdditionalHolderFor(DimActionRow.HOLDER_KEY);
        if (row == null) {
            row = new DimActionRow(layoutInflater, stateUiService, layoutInflater.getContext());
            holder.putAdditionalHolder(DimActionRow.HOLDER_KEY, row);
        }
        row.fillWith(device, null, null);
        holder.getTableLayout().addView(row.getView());
        return convertView;
    }

    @Override
    public boolean supports(FhemDevice fhemDevice) {
        if (isDimDisabled(fhemDevice)) {
            return false;
        }
        ButtonHook hook = deviceHookProvider.buttonHookFor(fhemDevice);
        return hook == ButtonHook.NORMAL
                && DimmableBehavior.behaviorFor(fhemDevice, null).isPresent();
    }

    public TableRow createDetailView(GenericDevice device, TableRow row, LayoutInflater inflater, Context context, String connectionId) {
        Optional<DimmableBehavior> dimmableBehaviorOpt = DimmableBehavior.behaviorFor(device, connectionId);
        DimmableBehavior behavior = dimmableBehaviorOpt.get();
        return new StateChangingSeekBarFullWidth(context, stateUiService, applicationProperties, behavior, row)
                .createRow(inflater, device);
    }
}
