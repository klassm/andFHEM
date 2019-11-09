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
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.domain.heating.schedule.configuration.FHTConfiguration
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval
import li.klass.fhem.fragments.weekprofile.HeatingConfigurationProvider
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FragmentUiService @Inject constructor() {
    fun showIntervalWeekProfileFor(device: XmlListDevice, connectionId: String?, context: Context,
                                   heatingConfiguration: HeatingConfigurationProvider<*>) =
            showWeekProfileFor(context, connectionId, device, heatingConfiguration, FragmentType.INTERVAL_WEEK_PROFILE)

    fun showFromToWeekProfileFor(device: XmlListDevice, connectionId: String?, context: Context) {
        val provider = object : HeatingConfigurationProvider<FromToHeatingInterval> {
            override fun get(): HeatingConfiguration<FromToHeatingInterval, *> = FHTConfiguration()
        }
        showWeekProfileFor(context, connectionId, device, provider,
                           FragmentType.FROM_TO_WEEK_PROFILE)
    }

    private fun showWeekProfileFor(context: Context, connectionId: String?, device: XmlListDevice,
                                   heatingConfigurationProvider: HeatingConfigurationProvider<*>,
                                   fragmentType: FragmentType) {
        context.sendBroadcast(Intent(Actions.SHOW_FRAGMENT)
                .putExtra(FRAGMENT, fragmentType)
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DEVICE_NAME, device.name)
                .putExtra(DEVICE_DISPLAY_NAME, device.getAttribute("alias") ?: device.name).putExtra(
                        HEATING_CONFIGURATION, heatingConfigurationProvider))
    }
}
