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
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.device_detail_card_actions.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProviders
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchAction
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class ActionsCardProvider @Inject constructor(
        private val detailActionProviders: GenericDetailActionProviders,
        private val stateUiService: StateUiService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 30

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String?): CardView? {
        if (device.setList.isEmpty()) {
            return null
        }
        val layoutInflater = context.layoutInflater
        val card = layoutInflater.inflate(R.layout.device_detail_card_actions, null) as CardView

        val actionsList = card.actionsList
        actionsList.addView(AvailableTargetStatesSwitchAction(stateUiService).createView(context, layoutInflater, device, actionsList, connectionId))

        coroutineScope {
            async(Dispatchers.IO) {
                detailActionProviders.providers
                        .filter { it.supports(device.xmlListDevice) }
                        .flatMap { it.actionsFor(context) }
                        .filter { it.supports(device) }
                        .map { it.createView(device.xmlListDevice, connectionId, context, layoutInflater, actionsList) }
            }.await().forEach { actionsList.addView(it) }
        }
        return card
    }
}