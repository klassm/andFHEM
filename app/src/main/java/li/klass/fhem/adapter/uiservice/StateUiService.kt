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

package li.klass.fhem.adapter.uiservice

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.constants.Actions
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateUiService @Inject constructor(
        val genericDeviceService: GenericDeviceService
) {

    suspend fun setSubState(device: XmlListDevice,
                            stateName: String, value: String, connectionId: String?, context: Context) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                genericDeviceService.setSubState(device, stateName, value, connectionId)
            }
            invokeUpdate(context)
        }
    }

    suspend fun setState(device: FhemDevice, value: String, context: Context, connectionId: String?) =
            setState(device.xmlListDevice, value, context, connectionId)

    suspend fun setState(device: XmlListDevice, value: String, context: Context, connectionId: String?) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                genericDeviceService.setState(device, value, connectionId)
            }
            invokeUpdate(context)
        }
    }

    private fun invokeUpdate(context: Context) = context.sendBroadcast(Intent(Actions.DO_UPDATE).apply { setPackage(context.packageName) })
}
