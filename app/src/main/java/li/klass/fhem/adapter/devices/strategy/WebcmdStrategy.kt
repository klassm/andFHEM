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

package li.klass.fhem.adapter.devices.strategy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TableLayout
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.adapter.devices.genericui.HolderActionRow
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow
import li.klass.fhem.adapter.devices.hook.ButtonHook.WEBCMD_DEVICE
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebcmdStrategy @Inject
constructor(val hookProvider: DeviceHookProvider, val stateUiService: StateUiService) : ViewStrategy() {

    override suspend fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?, rawDevice: FhemDevice, deviceItems: List<XmlDeviceViewItem>, connectionId: String?): View {
        var myView = convertView
        if (myView == null || myView.tag == null) {
            myView = layoutInflater.inflate(R.layout.device_overview_generic, null)
            val holder = GenericDeviceOverviewViewHolder(myView)
            myView!!.tag = holder
        }
        val holder = myView.tag as GenericDeviceOverviewViewHolder
        holder.resetHolder()
        holder.deviceName.visibility = View.GONE
        addOverviewSwitchActionRow(holder, rawDevice, connectionId)
        return myView
    }

    override fun supports(fhemDevice: FhemDevice): Boolean =
            hookProvider.buttonHookFor(fhemDevice) == WEBCMD_DEVICE

    private suspend fun addOverviewSwitchActionRow(holder: GenericDeviceOverviewViewHolder, device: FhemDevice, connectionId: String?) {
        val layout = holder.tableLayout
        addWebCmdOverviewActionRow(layout.context, device, layout, connectionId)
    }

    private suspend fun addWebCmdOverviewActionRow(context: Context, device: FhemDevice,
                                                   tableLayout: TableLayout, connectionId: String?) {
        tableLayout.addView(WebCmdActionRow(stateUiService, context, HolderActionRow.LAYOUT_OVERVIEW, device.aliasOrName)
                .createRow(context, tableLayout, device, connectionId))
    }
}
