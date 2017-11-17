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

package li.klass.fhem.service.intent

import android.content.Intent
import android.os.ResultReceiver
import com.google.common.base.Optional
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.update.backend.DeviceListUpdateService.UpdateResult
import org.slf4j.LoggerFactory
import javax.inject.Inject

class RoomListUpdateIntentService : ConvenientIntentService(RoomListUpdateIntentService::class.java.name) {

    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService

    @Inject
    lateinit var widgetUpdateService: AppWidgetUpdateService

    override fun handleIntent(intent: Intent, updatePeriod: Long, resultReceiver: ResultReceiver?): ConvenientIntentService.State {
        val action = intent.action

        if (action == Actions.DO_REMOTE_UPDATE) {
            val deviceName = Optional.fromNullable(intent.getStringExtra(DEVICE_NAME))
            val roomName = Optional.fromNullable(intent.getStringExtra(ROOM_NAME))
            val connectionId = Optional.fromNullable(intent.getStringExtra(CONNECTION_ID))
            return doRemoteUpdate(deviceName, roomName, connectionId)
        } else {
            return ConvenientIntentService.State.DONE
        }
    }

    private fun doRemoteUpdate(deviceName: Optional<String>, roomName: Optional<String>, connectionId: Optional<String>): ConvenientIntentService.State {
        LOG.info("doRemoteUpdate() - starting remote update")

        val result = when {
            deviceName.isPresent -> deviceListUpdateService.updateSingleDevice(deviceName.get(), connectionId, this)
            roomName.isPresent -> deviceListUpdateService.updateRoom(roomName.get(), connectionId, this)
            else -> deviceListUpdateService.updateAllDevices(connectionId, this)
        }
        handleResult(result)
        return ConvenientIntentService.State.DONE
    }

    private fun handleResult(result: UpdateResult) {
        when (result) {
            is UpdateResult.Success -> {
                LOG.info("doRemoteUpdate() - remote device list update finished")
                sendBroadcast(Intent(DO_REFRESH))
                widgetUpdateService.updateAllWidgets()
            }
            is UpdateResult.Error -> LOG.error("handleResult - update failed")
        }
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RoomListUpdateIntentService::class.java)
    }
}
