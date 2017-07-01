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

package li.klass.fhem.fragments

import android.content.Context
import android.os.Bundle
import com.google.common.base.Optional
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.PreferenceKeys.UPDATE_ON_ROOM_OPEN
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.DeviceListFragment
import li.klass.fhem.service.room.RoomListService
import javax.inject.Inject

class RoomDetailFragment : DeviceListFragment() {
    @Inject
    lateinit var roomListService: RoomListService

    private var roomName: String? = null

    override fun onResume() {
        super.onResume()
        val updateOnRoomOpen = applicationProperties.getBooleanSharedPreference(UPDATE_ON_ROOM_OPEN, false, activity)
        if (updateOnRoomOpen) {
            update(true)
        }
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun setArguments(args: Bundle) {
        super.setArguments(args)
        roomName = args.getString(BundleExtraKeys.ROOM_NAME)
    }

    override fun getTitle(context: Context): CharSequence {
        return arguments.getString(BundleExtraKeys.ROOM_NAME)
    }

    override fun getRoomDeviceListForUpdate() = roomListService.getDeviceListForRoom(roomName, Optional.absent(), activity)!!

    override fun executeRemoteUpdate() {
        if (roomName != null) {
            roomListUpdateService.updateRoom(roomName!!, Optional.absent(), context)
        }
    }
}
