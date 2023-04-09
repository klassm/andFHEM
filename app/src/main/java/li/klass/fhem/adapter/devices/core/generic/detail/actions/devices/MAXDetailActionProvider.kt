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
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton
import li.klass.fhem.devices.detail.ui.DeviceDetailFragmentDirections
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.configuration.MAXConfiguration
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval
import li.klass.fhem.fragments.weekprofile.HeatingConfigurationProvider
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MAXDetailActionProvider @Inject constructor() : DeviceDetailActionProvider() {

    override fun actionsFor(context: Context): List<ActionCardAction> {
        return listOf<ActionCardAction>(object : ActionCardButton(R.string.timetable, context) {
            override fun onClick(device: XmlListDevice, connectionId: String?, context: Context, navController: NavController) {
                val provider = object : HeatingConfigurationProvider<FilledTemperatureInterval> {
                    override fun get(): HeatingConfiguration<FilledTemperatureInterval, *> = MAXConfiguration()
                }
                navController.navigate(DeviceDetailFragmentDirections.actionDeviceDetailFragmentToIntervalWeekProfileFragment(
                    device.displayName(), device.name, provider, connectionId
                ))
            }
        })
    }

    override fun getDeviceType(): String = "MAX"
}
