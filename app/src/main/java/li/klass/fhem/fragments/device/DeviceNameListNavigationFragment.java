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

package li.klass.fhem.fragments.device;

import android.content.Intent;
import android.os.Bundle;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.FragmentType;

import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;

/**
 * Show all devices for a specific room and switch to the device detail when the name is clicked.
 */
public class DeviceNameListNavigationFragment extends DeviceNameListFragment {

    private String roomName;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        roomName = args.getString(ROOM_NAME);
    }

    @Override
    protected void onDeviceNameClick(String parent, Device<?> child) {
        if (child == null) return;

        Intent intent = new Intent(Actions.SHOW_FRAGMENT);
        intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_DETAIL);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, child.getName());
        intent.putExtra(ROOM_NAME, roomName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, resultReceiver);

        getActivity().sendBroadcast(intent);
    }
}
