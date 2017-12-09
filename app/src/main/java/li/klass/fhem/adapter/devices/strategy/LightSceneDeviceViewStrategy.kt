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
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem
import li.klass.fhem.adapter.devices.genericui.HolderActionRow
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LightSceneDeviceViewStrategy @Inject constructor(
        val stateUiService: StateUiService
) : ViewStrategy() {

    override fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?, device: FhemDevice, deviceItems: List<DeviceViewItem>, connectionId: String?): View {
        val layout = layoutInflater.inflate(R.layout.device_overview_generic, null) as TableLayout
        layout.removeAllViews()

        layout.addView(object : HolderActionRow<String>(device.aliasOrName,
                HolderActionRow.LAYOUT_OVERVIEW) {

            override fun getItems(device: FhemDevice): List<String> {
                val sceneEntry = device.xmlListDevice.setList.get("scene")
                return when (sceneEntry) {
                    is GroupSetListEntry -> sceneEntry.groupStates
                    else -> emptyList()
                }
            }

            override fun viewFor(scene: String, device: FhemDevice, inflater: LayoutInflater, context: Context, viewGroup: ViewGroup, connectionId: String?): View {
                val button = inflater.inflate(R.layout.lightscene_button, viewGroup, false) as Button
                setSceneButtonProperties(device, scene, button, context)
                return button
            }
        }.createRow(layout.context, layout, device, connectionId))
        return layout
    }

    override fun supports(fhemDevice: FhemDevice): Boolean =
            fhemDevice.xmlListDevice.type == "LightScene"

    private fun setSceneButtonProperties(device: FhemDevice, scene: String, button: Button, context: Context) {
        button.text = scene
        button.setOnClickListener { activateScene(device, scene, context) }
    }

    private fun activateScene(device: FhemDevice, scene: String, context: Context) {
        stateUiService.setSubState(device, null, "scene", scene, context)
    }
}
