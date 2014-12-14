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
import android.os.Parcelable;

import li.klass.fhem.fragments.RoomListFragment;

import static li.klass.fhem.constants.Actions.SHOW_FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;
import static li.klass.fhem.fragments.FragmentType.DEVICE_SELECTION;

public class DeviceNameSelectionNavigationFragment extends RoomListFragment {

    private Parcelable resultReceiver;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        resultReceiver = args.getParcelable(RESULT_RECEIVER);
    }

    @Override
    public void onClick(String roomName) {
        Intent intent = new Intent(SHOW_FRAGMENT);
        intent.putExtra(FRAGMENT, DEVICE_SELECTION);
        intent.putExtra(ROOM_NAME, roomName);
        intent.putExtra(RESULT_RECEIVER, resultReceiver);

        getActivity().sendBroadcast(intent);
    }
}
