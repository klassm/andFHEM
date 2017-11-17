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

package li.klass.fhem.room.detail.ui

import android.content.Context
import android.os.Bundle
import com.google.common.base.Optional
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.settings.SettingsKeys.UPDATE_ON_ROOM_OPEN
import li.klass.fhem.update.backend.DeviceListService
import javax.inject.Inject

class RoomDetailFragment : DeviceListFragment() {
    @Inject
    lateinit var deviceListService: DeviceListService

    private var roomName: String? = null

    override fun onResume() {
        super.onResume()
        val updateOnRoomOpen = applicationProperties.getBooleanSharedPreference(UPDATE_ON_ROOM_OPEN, false)
        if (updateOnRoomOpen) {
            update(true)
        }
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        roomName = args?.getString(BundleExtraKeys.ROOM_NAME)
    }

    override fun getTitle(context: Context): CharSequence =
            arguments?.getString(BundleExtraKeys.ROOM_NAME) ?: "unknown"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BundleExtraKeys.ROOM_NAME, roomName)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            roomName = savedInstanceState.getString(BundleExtraKeys.ROOM_NAME)
        }
    }

    override fun getRoomDeviceListForUpdate(context: Context) = deviceListService.getDeviceListForRoom(roomName!!, Optional.absent(), context)

    override fun executeRemoteUpdate(context: Context) {
        if (roomName != null) {
            deviceListUpdateService.updateRoom(roomName!!, Optional.absent(), context)
        }
    }
}
