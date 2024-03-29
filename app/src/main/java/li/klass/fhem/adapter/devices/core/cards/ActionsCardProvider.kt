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
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProviders
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesSwitchAction
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.databinding.DeviceDetailCardActionsBinding
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class ActionsCardProvider @Inject constructor(
        private val detailActionProviders: GenericDetailActionProviders,
        private val stateUiService: StateUiService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 30

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String, navController: NavController, expandHandler: ExpandHandler): CardView? {
        if (device.setList.isEmpty()) {
            return null
        }
        val layoutInflater = LayoutInflater.from(context)
        val binding = DeviceDetailCardActionsBinding.inflate(layoutInflater, null, false)

        val actionsList = binding.actionsList
        actionsList.addView(AvailableTargetStatesSwitchAction(stateUiService).createView(context, layoutInflater, device, actionsList, connectionId))

        coroutineScope {
            withContext(Dispatchers.IO) {
                detailActionProviders.providers
                        .filter { it.supports(device.xmlListDevice) }
                        .flatMap { it.actionsFor(context) }
                        .filter { it.supports(device) }
                        .map { it.createView(device.xmlListDevice, connectionId, context, layoutInflater, actionsList, navController) }
            }.forEach { actionsList.addView(it) }
        }
        return binding.root
    }
}