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

package li.klass.fhem.adapter.devices.core

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.DevStateIconAdder
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItemSorter
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow
import li.klass.fhem.adapter.devices.strategy.DefaultViewStrategy
import li.klass.fhem.adapter.devices.strategy.LightSceneDeviceViewStrategy
import li.klass.fhem.adapter.devices.strategy.ViewStrategy
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.update.backend.device.configuration.DeviceDescMapping
import li.klass.fhem.util.ApplicationProperties
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

abstract class OverviewDeviceAdapter : DeviceAdapter() {
    @Inject
    lateinit var dataConnectionSwitch: DataConnectionSwitch

    @Inject
    lateinit var stateUiService: StateUiService

    @Inject
    lateinit var deviceViewItemSorter: DeviceViewItemSorter

    @Inject
    lateinit var xmlDeviceItemProvider: XmlDeviceItemProvider

    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var deviceDescMapping: DeviceDescMapping

    @Inject
    lateinit var defaultOverviewStrategy: DefaultViewStrategy

    @Inject
    lateinit var lightSceneDeviceViewStrategy: LightSceneDeviceViewStrategy

    @Inject
    lateinit var devStateIconAdder: DevStateIconAdder

    private val overviewStrategies: List<ViewStrategy> by lazy {
        val strategies = mutableListOf<ViewStrategy>()
        fillOverviewStrategies(strategies)
        Collections.reverse(strategies)
        strategies.toList()
    }

    private val fieldNameAddedListeners: MutableMap<String, MutableList<FieldNameAddedToDetailListener>> = mutableMapOf()

    protected open fun fillOverviewStrategies(overviewStrategies: MutableList<ViewStrategy>) {
        overviewStrategies.add(defaultOverviewStrategy)
        overviewStrategies.add(lightSceneDeviceViewStrategy)
    }

    private fun getMostSpecificOverviewStrategy(device: FhemDevice): ViewStrategy {
        for (viewStrategy in overviewStrategies) {
            if (viewStrategy.supports(device)) {
                return viewStrategy
            }
        }
        throw IllegalStateException("no overview strategy found, default should always be present")
    }

    fun createOverviewView(convertView: View?, rawDevice: FhemDevice, context: Context): View {
        val stopWatch = StopWatch()
        stopWatch.start()
        val viewStrategy = getMostSpecificOverviewStrategy(rawDevice)
        LOGGER.debug("createOverviewView - viewStrategy=" + viewStrategy.javaClass.simpleName + ",time=" + stopWatch.time)
        val view = viewStrategy.createOverviewView(LayoutInflater.from(context), convertView, rawDevice, getSortedAnnotatedClassItems(rawDevice, context), null)
        LOGGER.debug("createOverviewView - finished, time=" + stopWatch.time)
        return view
    }

    internal fun createTableRow(inflater: LayoutInflater, resource: Int): GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder {
        val tableRow = inflater.inflate(resource, null) as TableRow
        return GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder(
                tableRow,
                tableRow.findViewById<View>(R.id.description) as TextView,
                tableRow.findViewById<View>(R.id.value) as TextView,
                tableRow.findViewById<View>(R.id.devStateIcon) as ImageView
        )
    }

    internal fun fillTableRow(holder: GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder, item: DeviceViewItem, device: FhemDevice, context: Context) {
        val value = item.getValueFor(device)
        val description = item.getName(deviceDescMapping, context)

        setTextView(holder.description, description)
        setTextView(holder.value, value.toString())
        if (value == null || value == "") {
            holder.row.visibility = View.GONE
        } else {
            holder.row.visibility = View.VISIBLE
        }
        devStateIconAdder.addDevStateIconIfRequired(value, device, holder.devStateIcon)
    }

    private fun getSortedAnnotatedClassItems(device: FhemDevice, context: Context): List<DeviceViewItem> {

        val xmlViewItems = xmlDeviceItemProvider.getDeviceClassItems(device, context)
        registerListenersFor(device, xmlViewItems)

        return deviceViewItemSorter.sortedViewItemsFrom(xmlViewItems)
    }

    private fun registerListenersFor(device: FhemDevice, xmlViewItems: Set<DeviceViewItem>) {
        for (xmlViewItem in xmlViewItems) {
            registerListenerFor(device, xmlViewItem)
        }
    }

    private fun registerListenerFor(device: FhemDevice, xmlViewItem: DeviceViewItem) {
        val key = xmlViewItem.sortKey
        if (device.xmlListDevice.setList.contains(key)) {
            registerFieldListener(key, object : FieldNameAddedToDetailListener() {
                override fun onFieldNameAdded(context: Context, tableLayout: TableLayout, field: String, device: FhemDevice, connectionId: String, fieldTableRow: TableRow) {
                    val setListEntry = device.xmlListDevice.setList[key, true]
                    if (setListEntry is SliderSetListEntry) {
                        tableLayout.addView(
                                StateChangingSeekBarFullWidth(context, stateUiService, applicationProperties, DimmableBehavior.continuousBehaviorFor(device, key, connectionId).get(), fieldTableRow)
                                        .createRow(LayoutInflater.from(context), device))
                    } else if (setListEntry is GroupSetListEntry) {
                        tableLayout.addView(StateChangingSpinnerActionRow(context, key, key, setListEntry.groupStates, xmlViewItem.getValueFor(device), key)
                                .createRow(device.xmlListDevice, connectionId, tableLayout))
                    }
                }
            })
        }
    }

    private fun registerFieldListener(fieldName: String, listener: FieldNameAddedToDetailListener) {
        val fieldToSet = fieldName.toLowerCase(Locale.getDefault())
        fieldNameAddedListeners.getOrPut(fieldToSet, { mutableListOf() }).add(listener)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OverviewDeviceAdapter::class.java)
    }
}
