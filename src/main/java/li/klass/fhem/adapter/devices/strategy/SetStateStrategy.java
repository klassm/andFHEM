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

import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.adapter.devices.StateChangeButtonActionRow;
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.genericui.ButtonActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ApplicationProperties;

@Singleton
public class SetStateStrategy extends DefaultViewStrategy {
    @Inject
    StateUiService stateUiService;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    DimmableStrategy dimmableStrategy;

    @Inject
    ToggleableStrategy toggleableStrategy;

    @Inject
    public SetStateStrategy() {
    }

    @Override
    protected void fillDeviceOverviewView(View view, FhemDevice device, GenericDeviceOverviewViewHolder viewHolder, List<DeviceViewItem> items, LayoutInflater layoutInflater) {
        super.fillDeviceOverviewView(view, device, viewHolder, items, layoutInflater);

        StateChangeButtonActionRow row = new StateChangeButtonActionRow(layoutInflater.getContext(), device, ButtonActionRow.LAYOUT_OVERVIEW, null);
        viewHolder.getTableLayout().addView(row.createRow(layoutInflater));
    }

    @Override
    public boolean supports(FhemDevice fhemDevice) {
        return !toggleableStrategy.supports(fhemDevice) && !dimmableStrategy.supports(fhemDevice)
                && fhemDevice.getSetList().contains("state");
    }

}
