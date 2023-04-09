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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices

import android.content.Context
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.FHTModeStateOverwrite
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.devices.detail.ui.DeviceDetailFragmentDirections
import li.klass.fhem.domain.heating.schedule.configuration.FHTConfiguration
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval
import li.klass.fhem.fragments.weekprofile.HeatingConfigurationProvider
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FHTDetailActionProvider @Inject constructor(
        fhtModeStateOverwrite: FHTModeStateOverwrite,
        val genericDeviceService: GenericDeviceService
) : DeviceDetailActionProvider() {

    init {
        addStateAttributeAction("mode", fhtModeStateOverwrite)
    }

    override fun actionsFor(context: Context): List<ActionCardAction> {
        return listOf(
                object : ActionCardButton(R.string.timetable, context) {
                    override fun onClick(device: XmlListDevice, connectionId: String?, context: Context, navController: NavController) {
                        val provider = object : HeatingConfigurationProvider<FromToHeatingInterval> {
                            override fun get(): HeatingConfiguration<FromToHeatingInterval, *> = FHTConfiguration()
                        }
                        navController.navigate(DeviceDetailFragmentDirections.actionDeviceDetailFragmentToFromToWeekProfileFragment(
                            device.name, device.displayName(), provider, connectionId
                        ))
                    }
                },
                object : ActionCardButton(R.string.requestRefresh, context) {
                    override fun onClick(device: XmlListDevice, connectionId: String?, context: Context, navController: NavController) {
                        GlobalScope.launch(Dispatchers.IO) {
                            genericDeviceService.setState(device, "refreshvalues", connectionId)
                        }
                    }
                }
        )
    }

    override fun getDeviceType(): String = "FHT"

    companion object {
        const val MAXIMUM_TEMPERATURE = 30.5
        const val MINIMUM_TEMPERATURE = 5.5
    }
}
