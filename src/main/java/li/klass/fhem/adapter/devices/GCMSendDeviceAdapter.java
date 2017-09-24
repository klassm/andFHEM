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
import android.content.Intent;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewButtonAction;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.GCMSendDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.device.GCMSendDeviceService;
import li.klass.fhem.service.intent.DeviceIntentService;

public class GCMSendDeviceAdapter extends ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow {
    @Inject
    GCMSendDeviceService gcmSendDeviceService;

    public GCMSendDeviceAdapter() {
        super();
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return GCMSendDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected List<DeviceDetailViewAction> provideDetailActions() {
        List<DeviceDetailViewAction> detailActions = super.provideDetailActions();

        detailActions.add(new DeviceDetailViewButtonAction(R.string.gcmRegisterThis) {
            @Override
            public void onButtonClick(Context context, FhemDevice device, String connectionId) {
                context.startService(new Intent(Actions.GCM_ADD_SELF)
                        .setClass(context, DeviceIntentService.class)
                        .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName()));
            }

            @Override
            public boolean isVisible(FhemDevice device, Context context) {
                return !gcmSendDeviceService.isDeviceRegistered((GCMSendDevice) device);
            }
        });

        return detailActions;
    }

    @Override
    protected String getGeneralDetailsNotificationText(Context context, FhemDevice device) {
        if (gcmSendDeviceService.isDeviceRegistered((GCMSendDevice) device)) {
            return context.getString(R.string.gcmAlreadyRegistered);
        }
        return null;
    }
}
