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
import com.google.common.base.Optional
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem
import li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow
import li.klass.fhem.adapter.devices.genericui.onoff.AbstractOnOffActionRow
import li.klass.fhem.adapter.devices.genericui.onoff.OnOffActionRowForToggleables
import li.klass.fhem.adapter.devices.hook.ButtonHook.*
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior
import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.ToggleableDevice
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleableStrategy
@Inject constructor(val hookProvider: DeviceHookProvider,
                    val onOffBehavior: OnOffBehavior) : ViewStrategy() {

    override fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?, rawDevice: FhemDevice, deviceItems: List<DeviceViewItem>, connectionId: String?): View {
        var myView = convertView
        val device = rawDevice as ToggleableDevice
        val stopWatch = StopWatch()
        stopWatch.start()
        if (myView == null || myView.tag == null) {
            myView = layoutInflater.inflate(R.layout.device_overview_generic, null)
            val holder = GenericDeviceOverviewViewHolder(myView)
            myView!!.tag = holder
            LOGGER.debug("createOverviewView - inflating layout, device=" + rawDevice.getName() + ", time=" + stopWatch.time)
        } else {
            LOGGER.debug("createOverviewView - reusing generic device overview view for device=" + rawDevice.getName())
        }
        val holder = myView.tag as GenericDeviceOverviewViewHolder
        holder.resetHolder()
        holder.deviceName.visibility = View.GONE
        addOverviewSwitchActionRow(holder, device, layoutInflater, null)
        LOGGER.debug("createOverviewView - finished, device=" + rawDevice.getName() + ", time=" + stopWatch.time)
        return myView
    }

    override fun supports(fhemDevice: FhemDevice): Boolean =
            hookProvider.buttonHookFor(fhemDevice) != WEBCMD_DEVICE && OnOffBehavior.supports(fhemDevice)
                    && !fhemDevice.devStateIcons.anyNoFhemwebLinkOf(onOffBehavior.getOnOffStateNames(fhemDevice))

    protected fun addOverviewSwitchActionRow(holder: GenericDeviceOverviewViewHolder, device: ToggleableDevice, layoutInflater: LayoutInflater, connectionId: String?) {
        val stopWatch = StopWatch()
        stopWatch.start()
        val hook = hookProvider.buttonHookFor(device)
        if (hook != NORMAL && hook != TOGGLE_DEVICE) {
            addOnOffActionRow(holder, device, OnOffActionRowForToggleables.LAYOUT_OVERVIEW, Optional.absent<Int>(), connectionId)
        } else {
            addToggleDeviceActionRow(holder, device, layoutInflater.context)
        }
        LOGGER.debug("addOverviewSwitchActionRow - finished, time=" + stopWatch.time)
    }

    private fun addToggleDeviceActionRow(holder: GenericDeviceOverviewViewHolder, device: ToggleableDevice, context: Context) {
        val stopWatch = StopWatch()
        stopWatch.start()

        var actionRow: ToggleDeviceActionRow? = holder.getAdditionalHolderFor<ToggleDeviceActionRow>(ToggleDeviceActionRow.HOLDER_KEY)
        if (actionRow == null) {
            actionRow = ToggleDeviceActionRow(context, onOffBehavior)
            holder.putAdditionalHolder(ToggleDeviceActionRow.HOLDER_KEY, actionRow)
            LOGGER.info("addToggleDeviceActionRow - creating row, time=" + stopWatch.time)
        }
        actionRow.fillWith(context, device, device.aliasOrName)
        holder.tableLayout.addView(actionRow.view)

        LOGGER.debug("addToggleDeviceActionRow - finished, time=" + stopWatch.time)
    }

    private fun addOnOffActionRow(holder: GenericDeviceOverviewViewHolder, device: ToggleableDevice, layoutId: Int, text: Optional<Int>, connectionId: String?) {
        var onOffActionRow: OnOffActionRowForToggleables? = holder.getAdditionalHolderFor<OnOffActionRowForToggleables>(OnOffActionRowForToggleables.HOLDER_KEY)
        if (onOffActionRow == null) {
            onOffActionRow = OnOffActionRowForToggleables(layoutId, hookProvider, onOffBehavior, text, connectionId)
            holder.putAdditionalHolder(OnOffActionRowForToggleables.HOLDER_KEY, onOffActionRow)
        }
        holder.tableLayout.addView(onOffActionRow
                .createRow(device, holder.tableLayout.context))
    }

    fun createDetailView(device: GenericDevice, context: Context, connectionId: String?): TableRow {
        return OnOffActionRowForToggleables(AbstractOnOffActionRow.LAYOUT_DETAIL, hookProvider, onOffBehavior, Optional.of(R.string.blank), connectionId)
                .createRow(device, context)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ToggleableStrategy::class.java)
    }
}
