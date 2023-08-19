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

package li.klass.fhem.adapter.devices.core.cards

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fcm.receiver.FcmSendDeviceService
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.slf4j.LoggerFactory
import javax.inject.Inject

class GcmSendCardProvider @Inject constructor(
        private val fcmSendDeviceService: FcmSendDeviceService
) : GenericDetailCardProvider, DeviceDetailActionProvider() {
    override fun ordering(): Int = 0

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String, navController: NavController, expandHandler: ExpandHandler): CardView? {
        if (device.xmlListDevice.type != getDeviceType()) {
            return null
        }
        val cardView = LayoutInflater.from(context)
            .inflate(R.layout.device_detail_card_gcmsend, null) as CardView
        cardView.visibility = View.GONE

        loadCardContent(device, cardView)

        return cardView
    }

    private suspend fun loadCardContent(device: FhemDevice, cardView: CardView) {
        coroutineScope {
            val isRegistered = withContext(Dispatchers.IO) { fcmSendDeviceService.isDeviceRegistered(device.xmlListDevice) }
            LoggerFactory.getLogger(GcmSendCardProvider::class.java).info("isRegistered = $isRegistered")
            if (isRegistered) {
                cardView.visibility = View.VISIBLE
            }
        }
    }

    override fun actionsFor(context: Context): List<ActionCardAction> {
        return listOf(object : ActionCardButton(R.string.gcmRegisterThis, context) {
            override fun onClick(device: XmlListDevice, connectionId: String?, context: Context, navController: NavController) {
                GlobalScope.launch(Dispatchers.Main) {
                    val result = withContext(Dispatchers.IO) { fcmSendDeviceService.addSelf(device) }
                    Toast.makeText(context, result.resultText, Toast.LENGTH_LONG).show()
                }
            }

            override suspend fun supports(device: FhemDevice): Boolean =
                    !fcmSendDeviceService.isDeviceRegistered(device.xmlListDevice)
        })
    }

    override fun getDeviceType(): String = "gcmsend"
}