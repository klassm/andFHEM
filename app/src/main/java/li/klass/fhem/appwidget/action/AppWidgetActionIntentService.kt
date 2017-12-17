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

package li.klass.fhem.appwidget.action

import android.content.Intent
import android.os.ResultReceiver
import com.google.common.base.Optional
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.intent.ConvenientIntentService
import li.klass.fhem.update.backend.DeviceListService
import javax.inject.Inject

class AppWidgetActionIntentService : ConvenientIntentService(AppWidgetActionIntentService::class.java.name) {
    @Inject lateinit var genericDeviceService: GenericDeviceService
    @Inject lateinit var deviceListService: DeviceListService

    override fun handleIntent(intent: Intent?, updatePeriod: Long, resultReceiver: ResultReceiver?): State {
        intent ?: return State.ERROR

        val connectionId = intent.getStringExtra(CONNECTION_ID)
        val device: FhemDevice? = deviceListService.getDeviceForName(intent.getStringExtra(DEVICE_NAME), connectionId)
        device ?: return State.ERROR

        when (intent.action) {
            Actions.DEVICE_SET_STATE -> {
                val targetState = intent.getStringExtra(DEVICE_TARGET_STATE)
                genericDeviceService.setState(device, targetState, Optional.fromNullable(connectionId), this, true)
            }
        }

        return State.SUCCESS
    }

    override fun inject(applicationComponent: ApplicationComponent?) {
        applicationComponent?.inject(this)
    }
}