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

package li.klass.fhem.fragments.device

import android.content.Intent
import android.os.Bundle
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.ui.FragmentType

/**
 * Show all devices for a specific room and switch to the device detail when the name is clicked.
 */
class DeviceNameListNavigationFragment : DeviceNameListFragment() {

    private var roomName: String? = null

    override fun setArguments(args: Bundle) {
        super.setArguments(args)
        roomName = args.getString(ROOM_NAME)
    }

    override fun onDeviceNameClick(child: FhemDevice) {
        activity.sendBroadcast(Intent(Actions.SHOW_FRAGMENT)
                .putExtra(FRAGMENT, FragmentType.DEVICE_DETAIL)
                .putExtra(DEVICE_NAME, child.name)
                .putExtra(CALLING_FRAGMENT, arguments.getSerializable(CALLING_FRAGMENT))
                .putExtra(ROOM_NAME, roomName))
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
