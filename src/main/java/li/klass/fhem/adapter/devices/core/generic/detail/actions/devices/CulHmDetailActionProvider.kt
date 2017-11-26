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
import com.google.common.collect.ImmutableList
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.HeatingModeDetailAction
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.StateAttributeAction
import li.klass.fhem.adapter.devices.genericui.CustomViewTableRow
import li.klass.fhem.adapter.uiservice.FragmentUiService
import li.klass.fhem.domain.CulHmHeatingMode
import li.klass.fhem.domain.CulHmHeatingMode.heatingModeFor
import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.domain.heating.schedule.configuration.CULHMConfiguration
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble
import li.klass.fhem.util.ValueExtractUtil.extractLeadingInt
import li.klass.fhem.widget.LitreContentView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CulHmDetailActionProvider @Inject
constructor(private val fragmentUiService: FragmentUiService) : DeviceDetailActionProvider() {

    init {
        addStateAttributeAction(MODE_STATE_NAME, CulHmHeatingModeDetailAction())
        addStateAttributeAction("content", KFM100ContentView())
    }

    override fun getDeviceType(): String = "CUL_HM"

    override fun actionsFor(context: Context): List<ActionCardAction> {
        return ImmutableList.of<ActionCardAction>(
                object : ActionCardButton(R.string.timetable, context) {
                    override fun onClick(device: XmlListDevice, connectionId: String, context: Context) {
                        fragmentUiService.showIntervalWeekProfileFor(device, connectionId, context, CULHMConfiguration())
                    }

                    override fun supports(genericDevice: GenericDevice): Boolean =
                            supportsHeating(genericDevice.xmlListDevice)
                }
        )
    }

    private class CulHmHeatingModeDetailAction : HeatingModeDetailAction<CulHmHeatingMode>() {

        override val availableModes: Array<CulHmHeatingMode>
            get() = CulHmHeatingMode.values()

        override fun getCurrentModeFor(device: XmlListDevice): CulHmHeatingMode =
                heatingModeFor(device.getState(MODE_STATE_NAME, false).get()).get()

        override fun supports(xmlListDevice: XmlListDevice): Boolean =
                CulHmDetailActionProvider.supportsHeating(xmlListDevice)
    }

    private class KFM100ContentView : StateAttributeAction {

        override fun createRow(device: XmlListDevice, connectionId: String?, key: String, stateValue: String, context: Context, parent: ViewGroup): TableRow {
            val model = device.getAttribute("model").get()
            val fillContentPercentage = determineContentPercentage(device, model)


            return object : CustomViewTableRow() {
                override fun getContentView(): View =
                        LitreContentView(context, fillContentPercentage)
            }.createRow(LayoutInflater.from(context), parent)
        }

        private fun determineContentPercentage(device: XmlListDevice, model: String): Double {
            val fillContentPercentage: Double
            if ("HM-Sen-Wa-Od" == model) {
                fillContentPercentage = extractLeadingDouble(device.getState("level", false).get()) / 100.0
            } else {
                val rawToReadable = device.getAttribute("rawToReadable").get()
                val parts = parseRawToReadable(rawToReadable)
                val maximum = if (parts.size == 2) {
                    extractLeadingInt(parts[1]).toDouble()
                } else 0.0

                val contentValue = extractLeadingDouble(device.getState("content", false).get())
                val content = if (contentValue > maximum) maximum else contentValue
                fillContentPercentage = content / maximum
            }
            return fillContentPercentage
        }

        private fun parseRawToReadable(value: String): Array<String> {
            val lastSpace = value.lastIndexOf(" ")
            val lastDefinition = if (lastSpace == -1) value else value.substring(lastSpace + 1)
            return lastDefinition.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        override fun supports(xmlListDevice: XmlListDevice): Boolean {
            val modelOpt = xmlListDevice.getAttribute("model")
            if (!modelOpt.isPresent) {
                return false
            }

            val model = modelOpt.get()
            return ("HM-Sen-Wa-Od".equals(model, ignoreCase = true) && xmlListDevice.containsState("level"))
                    || (xmlListDevice.containsAttribute("rawToReadable") && xmlListDevice.containsState("content"))

        }
    }

    companion object {
        internal val MODE_STATE_NAME = "controlMode"

        private fun supportsHeating(xmlListDevice: XmlListDevice): Boolean {
            val controlMode = xmlListDevice.getState(MODE_STATE_NAME, false)
            return controlMode.isPresent && heatingModeFor(controlMode.get()).isPresent
        }
    }
}
