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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.StateChangingTargetStateSelectedCallback
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice

open class WebCmdActionRow(
        private val stateUiService: StateUiService,
        context: Context,
        layout: Int,
        description: String = context.getString(R.string.webcmd)
) : HolderActionRow<String>(description, layout) {
    override fun getItems(device: FhemDevice): List<String> = device.webCmd

    override fun viewFor(command: String, device: FhemDevice, inflater: LayoutInflater,
                         context: Context, viewGroup: ViewGroup, connectionId: String): View {

        val container = inflater.inflate(R.layout.webcmd_row_element, viewGroup, false)
        val button = container.findViewById<ToggleButton>(R.id.toggleButton)!!

        button.text = device.getEventMapStateFor(command)
        button.textOn = device.getEventMapStateFor(command)
        button.textOff = device.getEventMapStateFor(command)

        button.setOnClickListener {
            val setList = device.setList
            val callback = StateChangingTargetStateSelectedCallback(context, stateUiService, connectionId)
            val result = AvailableTargetStatesDialogUtil.handleSelectedOption(
                    context, device, setList[command, true], callback
            )
            if (!result) {
                stateUiService.setState(device, command, context, connectionId)
            }
        }

        return container
    }
}
