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
import android.widget.TableRow;

import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class DimmableDeviceDimActionRowFullWidth<D extends DimmableDevice<D>> extends DeviceDimActionRowFullWidth<D> {

    public DimmableDeviceDimActionRowFullWidth(D device, int layoutId, TableRow updateRow) {
        super(device.getDimPosition(), device.getDimLowerBound(), device.getDimStep(),
                device.getDimUpperBound(), updateRow, layoutId);
    }

    public void onStopTrackingTouch(final Context context, D device, int progress) {
        int dimProgress = dimProgressToDimState(progress, device.getDimLowerBound(), device.getDimStep());

        Intent intent = new Intent(Actions.DEVICE_DIM);
        intent.setClass(context, DeviceIntentService.class);
        intent.putExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, dimProgress);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

        context.startService(intent);
    }

    @Override
    public void onStopDim(Context context, D device, int progress) {
        Intent intent = new Intent(Actions.DEVICE_DIM);
        intent.putExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, progress);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

        context.startService(intent);
    }

    @Override
    public String toDimUpdateText(D device, int progress) {
        return device.getDimStateForPosition(progress);
    }
}
