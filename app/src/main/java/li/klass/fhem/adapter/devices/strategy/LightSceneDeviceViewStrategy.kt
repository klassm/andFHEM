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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.adapter.devices.genericui.HolderActionRow
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LightSceneDeviceViewStrategy @Inject constructor(val stateUiService: StateUiService) :
        ViewStrategy() {

    override fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?,
                                    rawDevice: FhemDevice, deviceItems: List<XmlDeviceViewItem>,
                                    connectionId: String?): View {
        val layout = layoutInflater.inflate(R.layout.device_overview_generic, null) as TableLayout
        layout.removeAllViews()

        layout.addView(object : HolderActionRow<String>(rawDevice.aliasOrName, LAYOUT_OVERVIEW) {

            override fun getItems(device: FhemDevice): List<String> {
                return when (val sceneEntry = device.setList["scene"]) {
                    is GroupSetListEntry -> sceneEntry.groupStates
                    else                 -> emptyList()
                }
            }

            override fun viewFor(item: String, device: FhemDevice, inflater: LayoutInflater,
                                 context: Context, viewGroup: ViewGroup,
                                 connectionId: String?): View {
                val button = inflater.inflate(R.layout.lightscene_button, viewGroup, false) as Button
                if (item == device.state) {
                    button.background =
                        context.resources.getDrawable(R.drawable.theme_toggle_on_normal)
                }
                setSceneButtonProperties(device, item, button, context)
                return button
            }
        }.createRow(layout.context, layout, rawDevice, connectionId))
        return layout
    }

    override fun supports(
            fhemDevice: FhemDevice): Boolean = fhemDevice.xmlListDevice.type == "LightScene"

    private fun setSceneButtonProperties(device: FhemDevice, scene: String, button: Button,
                                         context: Context) {
        button.text = scene
        button.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                launch {
                    activateScene(device, scene, context)
                }
            }
        }
    }

    private suspend fun activateScene(device: FhemDevice, scene: String, context: Context) {
        stateUiService.setSubState(device.xmlListDevice, "scene", scene, null, context)
    }
}
