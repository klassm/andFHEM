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

package li.klass.fhem.adapter.devices;

import android.content.Context;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewButtonAction;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.UniRollDevice;
import li.klass.fhem.domain.core.FhemDevice;

public class UniRollAdapter extends ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow {

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return UniRollDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected List<DeviceDetailViewAction> provideDetailActions() {
        List<DeviceDetailViewAction> detailActions = super.provideDetailActions();

        detailActions.add(new DeviceDetailViewButtonAction(R.string.up) {
            @Override
            public void onButtonClick(Context context, FhemDevice device) {
                stateUiService.setState(device, "up", context);
            }
        });

        detailActions.add(new DeviceDetailViewButtonAction(R.string.stop) {
            @Override
            public void onButtonClick(Context context, FhemDevice device) {
                stateUiService.setState(device, "stop", context);
            }
        });

        detailActions.add(new DeviceDetailViewButtonAction(R.string.down) {
            @Override
            public void onButtonClick(Context context, FhemDevice device) {
                stateUiService.setState(device, "down", context);
            }
        });

        return detailActions;
    }
}
