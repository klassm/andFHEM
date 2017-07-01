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

package li.klass.fhem.adapter.devices.genericui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.TextView
import android.widget.ToggleButton
import com.google.common.base.Optional
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.EventMap
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.intent.DeviceIntentService
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory

class ToggleDeviceActionRow(context: Context, private val onOffBehavior: OnOffBehavior) {
    val view: TableRow
    private val descriptionView: TextView
    private val toggleButton: ToggleButton

    init {
        val stopWatch = StopWatch()
        stopWatch.start()

        this.view = LayoutInflater.from(context).inflate(LAYOUT_OVERVIEW, null) as TableRow
        LOGGER.debug("inflation complete, time=" + stopWatch.time)

        this.descriptionView = this.view.findViewById(R.id.description) as TextView
        this.toggleButton = this.view.findViewById(R.id.toggleButton) as ToggleButton
        LOGGER.debug("finished, time=" + stopWatch.time)
    }

    private fun isOn(device: FhemDevice): Boolean {
        return onOffBehavior.isOn(device)
    }

    protected fun onButtonClick(context: Context, device: FhemDevice) {
        context.startService(Intent(Actions.DEVICE_TOGGLE_STATE)
                .setClass(context, DeviceIntentService::class.java)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, UpdatingResultReceiver(context)))
    }

    fun createRow(context: Context, device: FhemDevice, description: String): TableRow {
        fillWith(context, device, description)
        return view
    }

    fun fillWith(context: Context, device: FhemDevice, description: String) {
        descriptionView.text = description
        toggleButton.setOnClickListener { onButtonClick(context, device) }
        setToggleButtonText(device, toggleButton, context)
        toggleButton.isChecked = isOn(device)
    }

    private fun setToggleButtonText(device: FhemDevice, toggleButton: ToggleButton, context: Context) {
        val eventMap = device.eventMap

        val onStateText = getOnStateText(eventMap)
        if (onStateText.isPresent) {
            toggleButton.textOn = onStateText.get()
        } else {
            toggleButton.textOn = context.getString(R.string.on)
        }

        val offStateText = getOffStateText(eventMap)
        if (offStateText.isPresent) {
            toggleButton.textOff = offStateText.get()
        } else {
            toggleButton.textOff = context.getString(R.string.off)
        }
    }

    private fun getOnStateText(eventMap: EventMap): Optional<String> {
        return Optional.fromNullable(eventMap.getValueFor("on"))
    }

    private fun getOffStateText(eventMap: EventMap): Optional<String> {
        return Optional.fromNullable(eventMap.getValueFor("off"))
    }

    companion object {
        val HOLDER_KEY = ToggleDeviceActionRow::class.java.name!!
        val LAYOUT_OVERVIEW = R.layout.device_overview_togglebuttonrow
        private val LOGGER = LoggerFactory.getLogger(ToggleDeviceActionRow::class.java)
    }
}
