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

package li.klass.fhem.adapter.devices

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewButtonAction
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.GCMSendDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fcm.GCMSendDeviceService
import org.jetbrains.anko.coroutines.experimental.bg
import javax.inject.Inject

class GCMSendDeviceAdapter : ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow() {
    @Inject
    lateinit var gcmSendDeviceService: GCMSendDeviceService

    override fun getSupportedDeviceClass(): Class<out FhemDevice> = GCMSendDevice::class.java

    override fun inject(daggerComponent: ApplicationComponent) {
        daggerComponent.inject(this)
    }

    override fun provideDetailActions(): MutableList<DeviceDetailViewAction> {
        val detailActions = super.provideDetailActions()

        detailActions.add(object : DeviceDetailViewButtonAction(R.string.gcmRegisterThis) {
            override fun onButtonClick(context: Context, device: FhemDevice, connectionId: String?) {
                async(UI) {
                    val result = bg {
                        gcmSendDeviceService.addSelf(device as GCMSendDevice, context)
                    }.await()

                    showToast(result.resultText, context)
                    context.sendBroadcast(Intent(Actions.DO_UPDATE)
                            .putExtra(BundleExtraKeys.DO_REFRESH, true))
                }
            }

            override fun isVisible(device: FhemDevice, context: Context): Boolean {
                val registered = gcmSendDeviceService.isDeviceRegistered(device as GCMSendDevice)
                return !registered
            }
        })

        return detailActions
    }


    internal fun showToast(stringId: Int, context: Context) {
        context.sendBroadcast(Intent(Actions.SHOW_TOAST)
                .putExtra(BundleExtraKeys.STRING_ID, stringId))
    }

    override fun getGeneralDetailsNotificationText(context: Context, device: FhemDevice): String? {
        val registered = gcmSendDeviceService.isDeviceRegistered(device as GCMSendDevice)
        return if (registered) {
            context.getString(R.string.gcmAlreadyRegistered)
        } else null
    }
}
