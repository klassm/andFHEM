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

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import li.klass.fhem.R
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.devices.backend.ToggleableService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class AppWidgetActionHandler @Inject constructor(
        private val deviceListService: DeviceListService,
        private val genericDeviceService: GenericDeviceService,
        private val toggleableService: ToggleableService,
        private val appWidgetUpdateService: AppWidgetUpdateService,
        deviceListUpdateService: DeviceListUpdateService
) {
    private val handlers: Map<String, ActionHandler> = mapOf(
            Actions.DEVICE_WIDGET_TOGGLE to object : ActionHandler {
                override fun handle(device: FhemDevice?, connectionId: String?, bundle: Bundle, context: Context) {
                    device ?: return
                    toggleableService.toggleState(device, connectionId)
                }
            },
            Actions.DEVICE_WIDGET_TARGET_STATE to object : ActionHandler {
                override fun handle(device: FhemDevice?, connectionId: String?, bundle: Bundle, context: Context) {
                    device ?: return
                    val targetState = bundle.getString(DEVICE_TARGET_STATE) ?: return
                    genericDeviceService.setState(device.xmlListDevice, targetState, connectionId, true)
                }
            },
            Actions.WIDGET_REQUEST_UPDATE to object : ActionHandler {
                override fun handle(device: FhemDevice?, connectionId: String?, bundle: Bundle, context: Context) {
                    Handler(context.mainLooper).post { Toast.makeText(context, R.string.widget_remote_update_started, Toast.LENGTH_LONG).show() }
                    deviceListUpdateService.updateAllDevices(connectionId)
                }
            }
    )

    fun handle(context: Context, bundle: Bundle, action: String) {
        val handler = handlers[action] ?: return
        val deviceName = bundle.getString(DEVICE_NAME)
        val connectionId = bundle.getString(CONNECTION_ID)

        doAsync {
            val device = deviceName?.let { deviceListService.getDeviceForName(it, connectionId) }
            handler.handle(device, connectionId, bundle, context)
            appWidgetUpdateService.updateAllWidgets()
        }
    }

    internal interface ActionHandler {
        fun handle(device: FhemDevice?, connectionId: String?, bundle: Bundle, context: Context)
    }
}