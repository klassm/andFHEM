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

import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.FloorplanDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.FragmentType;

public class FloorplanAdapter extends GenericDeviceAdapter<FloorplanDevice> {
    public FloorplanAdapter() {
        super(FloorplanDevice.class);
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.FLOORPLAN);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        return intent;
    }
}
