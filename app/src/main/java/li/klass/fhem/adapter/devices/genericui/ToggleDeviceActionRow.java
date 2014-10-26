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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.content.Intent;

import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class ToggleDeviceActionRow<T extends ToggleableDevice> extends ToggleActionRow<T> {
    public ToggleDeviceActionRow(Context context, int description, int layout) {
        super(context, description, layout);
    }

    public ToggleDeviceActionRow(String description, int layout) {
        super(description, layout);
    }

    @Override
    protected boolean isOn(T device) {
        return device.isOnRespectingInvertHook();
    }

    @Override
    protected void onButtonClick(final Context context, T device, boolean isChecked) {
        Intent intent = new Intent(Actions.DEVICE_TOGGLE_STATE);
        intent.setClass(context, DeviceIntentService.class);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

        context.startService(intent);
    }
}
