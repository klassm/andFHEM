/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;

import static li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil.STATE_SENDING_CALLBACK;

public class AvailableTargetStatesSwitchActionRow<D extends Device<D>> extends DeviceDetailViewButtonAction<D> {
    public AvailableTargetStatesSwitchActionRow() {
        super(R.string.switchSetOptions);
    }

    @Override
    public void onButtonClick(Context context, D device) {
        showSwitchOptionsMenu(context, device);
    }

    private void showSwitchOptionsMenu(final Context context, final D device) {
        AvailableTargetStatesDialogUtil.showSwitchOptionsMenu(context, device, STATE_SENDING_CALLBACK);
    }

    @Override
    public boolean isVisible(D device) {
        return device.getSetList().size() > 0;
    }
}
