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
import android.widget.TableRow
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.adapter.devices.genericui.DimActionRow
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth
import li.klass.fhem.adapter.devices.hook.ButtonHook
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.ApplicationProperties
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DimmableStrategy @Inject
constructor(
        val deviceHookProvider: DeviceHookProvider,
        val stateUiService: StateUiService,
        val applicationProperties: ApplicationProperties
) : ViewStrategy() {

    override suspend fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?, rawDevice: FhemDevice, deviceItems: List<XmlDeviceViewItem>, connectionId: String?): View {
        val myView = when {
            convertView == null || convertView.tag == null -> {
                val v = layoutInflater.inflate(R.layout.device_overview_generic, null)
                val holder = GenericDeviceOverviewViewHolder(v)
                v!!.tag = holder
                v
            }
            else -> convertView
        }
        val holder = myView.tag as GenericDeviceOverviewViewHolder
        holder.resetHolder()
        holder.deviceName.visibility = View.GONE
        val row = holder.getAdditionalHolderFor(DimActionRow.HOLDER_KEY) ?: {
            val newRow = DimActionRow(layoutInflater, stateUiService, layoutInflater.context)
            holder.putAdditionalHolder(DimActionRow.HOLDER_KEY, newRow)
            newRow
        }()
        row.fillWith(rawDevice, null, null)
        holder.tableLayout.addView(row.view)
        return myView
    }

    override fun supports(fhemDevice: FhemDevice): Boolean {
        val hook = deviceHookProvider.buttonHookFor(fhemDevice)
        return hook == ButtonHook.NORMAL && DimmableBehavior.behaviorFor(fhemDevice, null) != null
    }

    fun createDetailView(device: FhemDevice, row: TableRow, context: Context, connectionId: String?): TableRow {
        val dimmableBehavior = DimmableBehavior.behaviorFor(device, connectionId)!!
        return StateChangingSeekBarFullWidth(context, stateUiService, applicationProperties, dimmableBehavior, row)
                .createRow(LayoutInflater.from(context), device)
    }
}
