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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import androidx.navigation.NavController
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.HeatingModeDetailAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.StateAttributeAction
import li.klass.fhem.adapter.devices.genericui.CustomViewTableRow
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.devices.detail.ui.DeviceDetailFragmentDirections
import li.klass.fhem.domain.CulHmHeatingMode
import li.klass.fhem.domain.CulHmHeatingMode.Companion.heatingModeFor
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.heating.schedule.configuration.CULHMConfiguration
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval
import li.klass.fhem.fragments.weekprofile.HeatingConfigurationProvider
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble
import li.klass.fhem.util.ValueExtractUtil.extractLeadingInt
import li.klass.fhem.widget.LitreContentView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CulHmDetailActionProvider @Inject constructor(
        stateUiService: StateUiService) :
        DeviceDetailActionProvider() {

    init {
        addStateAttributeAction(MODE_STATE_NAME, CulHmHeatingModeDetailAction(stateUiService))
        addStateAttributeAction("state", KFM100ContentView())
    }

    override fun getDeviceType() = "CUL_HM"

    override fun actionsFor(context: Context): List<ActionCardAction> {
        return listOf(object : ActionCardButton(R.string.timetable,
                context) {
            override fun onClick(device: XmlListDevice, connectionId: String?, context: Context, navController: NavController) {
                val provider = object : HeatingConfigurationProvider<FilledTemperatureInterval> {
                    override fun get(): HeatingConfiguration<FilledTemperatureInterval, *> = CULHMConfiguration()
                }

                navController.navigate(DeviceDetailFragmentDirections.actionDeviceDetailFragmentToIntervalWeekProfileFragment(
                    device.displayName(), device.name, provider, connectionId
                ))
            }

            override suspend fun supports(device: FhemDevice): Boolean = supportsHeating(
                    device.xmlListDevice)
        })
    }

    private class CulHmHeatingModeDetailAction(stateUiService: StateUiService) :
            HeatingModeDetailAction<CulHmHeatingMode>(stateUiService) {

        override val availableModes: Array<CulHmHeatingMode>
            get() = CulHmHeatingMode.values()

        override fun getCurrentModeFor(device: XmlListDevice): CulHmHeatingMode = heatingModeFor(
                device.getState(MODE_STATE_NAME, false))!!

        override fun supports(xmlListDevice: XmlListDevice): Boolean = supportsHeating(
                xmlListDevice)
    }

    private class KFM100ContentView : StateAttributeAction {

        override fun createRow(device: XmlListDevice, connectionId: String?, key: String,
                               stateValue: String, context: Context, parent: ViewGroup): TableRow {
            val model = device.getAttribute("model")!!
            val fillContentPercentage = determineContentPercentage(device, model)


            return object : CustomViewTableRow() {
                override fun getContentView(): View = LitreContentView(context,
                                                                       fillContentPercentage)
            }.createRow(LayoutInflater.from(context), parent)
        }

        private fun determineContentPercentage(device: XmlListDevice,
                                               model: String) = if ("HM-Sen-Wa-Od".equals(model,
                                                                                          ignoreCase = true)) {
            extractLeadingDouble(device.getState("level", false)) / 100.0
        } else {
            val rawToReadable = device.getAttribute("rawToReadable")!!
            val parts = parseRawToReadable(rawToReadable)
            val maximum = if (parts.size == 2) {
                extractLeadingInt(parts[1]).toDouble()
            } else 0.0

            val contentValue = extractLeadingDouble(device.getState("content", false))
            val content = if (contentValue > maximum) maximum else contentValue
            content / maximum
        }

        private fun parseRawToReadable(value: String): Array<String> {
            val lastSpace = value.lastIndexOf(" ")
            val lastDefinition = if (lastSpace == -1) value else value.substring(lastSpace + 1)
            return lastDefinition.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        override fun supports(xmlListDevice: XmlListDevice): Boolean {
            val model = xmlListDevice.getAttribute("model") ?: return false

            return ("HM-Sen-Wa-Od".equals(model, ignoreCase = true) && xmlListDevice.containsState(
                    "level")) || (xmlListDevice.containsAttribute(
                    "rawToReadable") && xmlListDevice.containsState("content"))
        }
    }

    companion object {
        internal const val MODE_STATE_NAME = "controlMode"

        private fun supportsHeating(xmlListDevice: XmlListDevice): Boolean {
            val controlMode = xmlListDevice.getState(MODE_STATE_NAME, false)
            return controlMode != null && heatingModeFor(controlMode) != null
        }
    }
}
