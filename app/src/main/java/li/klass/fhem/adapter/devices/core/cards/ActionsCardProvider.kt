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
import android.support.v7.widget.CardView
import kotlinx.android.synthetic.main.device_detail_card_actions.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProviders
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchAction
import li.klass.fhem.domain.GenericDevice
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class ActionsCardProvider @Inject constructor(
        private val detailActionProviders: GenericDetailActionProviders
) : GenericDetailCardProvider {
    override fun ordering(): Int = 30

    override fun provideCard(fhemDevice: GenericDevice, context: Context, connectionId: String?): CardView? {
        if (fhemDevice.xmlListDevice.setList.isEmpty()) {
            return null
        }
        val layoutInflater = context.layoutInflater
        val card = layoutInflater.inflate(R.layout.device_detail_card_actions, null) as CardView

        val actionsList = card.actionsList
        actionsList.addView(AvailableTargetStatesSwitchAction().createView(context, layoutInflater, fhemDevice, actionsList, connectionId))

        async(UI) {
            bg {
                detailActionProviders.providers
                        .filter { it.supports(fhemDevice.xmlListDevice) }
                        .flatMap { it.actionsFor(context) }
                        .filter { it.supports(fhemDevice) }
                        .map { it.createView(fhemDevice.xmlListDevice, connectionId, context, layoutInflater, actionsList) }
            }.await().forEach { actionsList.addView(it) }
        }
        return card
    }
}