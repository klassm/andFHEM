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

package li.klass.fhem.fragments.device;

import android.content.Intent;
import android.os.Bundle;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;

public class DeviceNameSelectionFragment extends DeviceNameListFragment {
    @Override
    protected void onDeviceNameClick(String parent, Device<?> child) {
        if (child == null) return;

        if (resultReceiver != null) {
            Bundle result = new Bundle();
            result.putSerializable(BundleExtraKeys.CLICKED_DEVICE, child);
            resultReceiver.send(ResultCodes.SUCCESS, result);
        }

        Intent intent = new Intent(Actions.BACK);
        intent.putExtra(BundleExtraKeys.CLICKED_DEVICE, child);
        getActivity().sendBroadcast(intent);
    }
}