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
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fcm.receiver.GCMSendDeviceService
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class GcmSendCardProvider @Inject constructor(
        private val gcmSendDeviceService: GCMSendDeviceService
) : GenericDetailCardProvider, DeviceDetailActionProvider() {
    override fun ordering(): Int = 0

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String?): CardView? {
        if (device.xmlListDevice.type != getDeviceType()) {
            return null
        }
        val cardView = context.layoutInflater.inflate(R.layout.device_detail_card_gcmsend, null) as CardView
        cardView.visibility = View.GONE

        loadCardContent(device, cardView)

        return cardView
    }

    private suspend fun loadCardContent(device: FhemDevice, cardView: CardView) {
        coroutineScope {
            val isRegistered = async(Dispatchers.IO) { gcmSendDeviceService.isDeviceRegistered(device.xmlListDevice) }.await()
            if (isRegistered) {
                cardView.visibility = View.VISIBLE
            }
        }
    }

    override fun actionsFor(context: Context): List<ActionCardAction> {
        return listOf(object : ActionCardButton(R.string.gcmRegisterThis, context) {
            override fun onClick(device: XmlListDevice, connectionId: String?, context: Context) {
                GlobalScope.launch(Dispatchers.Main) {
                    val result = async(Dispatchers.IO) { gcmSendDeviceService.addSelf(device) }.await()
                    Toast.makeText(context, result.resultText, Toast.LENGTH_LONG).show()
                }
            }

            override fun supports(device: FhemDevice): Boolean =
                    !gcmSendDeviceService.isDeviceRegistered(device.xmlListDevice)
        })
    }

    override fun getDeviceType(): String = "gcmsend"
}