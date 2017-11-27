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

package li.klass.fhem.adapter.devices.core.cards.device.values

import android.content.Context
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.DevStateIconAdder
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItemSorter
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProviders
import li.klass.fhem.adapter.devices.genericui.StateChangingColorPickerRow
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow
import li.klass.fhem.adapter.devices.genericui.onoff.AbstractOnOffActionRow
import li.klass.fhem.adapter.devices.genericui.onoff.OnOffSubStateActionRow
import li.klass.fhem.adapter.devices.strategy.DimmableStrategy
import li.klass.fhem.adapter.devices.strategy.ToggleableStrategy
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.RGBSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.update.backend.device.configuration.DeviceDescMapping
import li.klass.fhem.util.ApplicationProperties
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class DetailCardWithDeviceValuesProvider @Inject constructor(
        private val deviceViewItemSorter: DeviceViewItemSorter,
        private val xmlDeviceItemProvider: XmlDeviceItemProvider,
        private val deviceDescMapping: DeviceDescMapping,
        private val devStateIconAdder: DevStateIconAdder,
        private val toggleableStrategy: ToggleableStrategy,
        private val dimmableStrategy: DimmableStrategy,
        private val stateUiService: StateUiService,
        private val applicationProperties: ApplicationProperties,
        private val detailActionProviders: GenericDetailActionProviders) {

    fun createCard(device: GenericDevice, connectionId: String?, caption: Int,
                   itemProvider: ItemProvider, context: Context): CardView {
        val card = context.layoutInflater.inflate(R.layout.device_detail_card_table, null)

        val providers = detailActionProviders.providers
                .filter { it.supports(device.xmlListDevice) }
                .toList()

        val hasConfiguration = device.deviceConfiguration.isPresent

        val captionTextView = card.findViewById<TextView>(R.id.cardCaption)
        captionTextView.setText(caption)

        val table = card.findViewById<TableLayout>(R.id.table)

        var showExpandButton = hasConfiguration
        var itemsToShow = getSortedClassItems(device, itemProvider, false, context)
        val allItems = getSortedClassItems(device, itemProvider, true, context)
        if (itemsToShow.isEmpty() || itemsToShow.size == allItems.size) {
            itemsToShow = allItems
            showExpandButton = false
        }
        fillTable(device, connectionId, table, itemsToShow, providers, context)

        val button = card.findViewById<Button>(R.id.expandButton)
        button.visibility = if (showExpandButton) View.VISIBLE else View.GONE
        button.setOnClickListener {
            fillTable(device, connectionId, table, getSortedClassItems(device, itemProvider, true, context), providers, context)
            button.visibility = View.GONE
        }

        if (itemsToShow.isEmpty()) {
            card.visibility = View.GONE
        }

        return card as CardView
    }

    private fun fillTable(device: GenericDevice, connectionId: String?, table: TableLayout, items: List<DeviceViewItem>, providers: List<GenericDetailActionProvider>, context: Context) {
        table.removeAllViews()

        for (item in items) {
            val holder = createTableRow(LayoutInflater.from(context), R.layout.device_detail_generic_table_row)
            fillTableRow(holder, item, device, context)
            addRow(table, holder.row)
            addActionIfRequired(device, connectionId, table, item, holder.row, providers, context)
        }

        table.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun getSortedClassItems(device: FhemDevice, itemProvider: ItemProvider, showUnknown: Boolean, context: Context): List<DeviceViewItem> {
        val xmlViewItems = itemProvider.itemsFor(xmlDeviceItemProvider, device, showUnknown, context)
        return deviceViewItemSorter.sortedViewItemsFrom(xmlViewItems)
    }


    private fun fillTableRow(holder: GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder, item: DeviceViewItem, device: FhemDevice, context: Context) {
        val value = item.getValueFor(device)
        val description = item.getName(deviceDescMapping, context)
        holder.description.text = description
        holder.value.text = value.toString()
        if (value == null || value == "") {
            holder.row.visibility = View.GONE
        } else {
            holder.row.visibility = View.VISIBLE
        }
        devStateIconAdder.addDevStateIconIfRequired(value, device, holder.devStateIcon)
    }

    private fun createTableRow(inflater: LayoutInflater, resource: Int): GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder {
        val holder = GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder()
        val tableRow = inflater.inflate(resource, null) as TableRow
        holder.row = tableRow
        holder.description = tableRow.findViewById(R.id.description)
        holder.value = tableRow.findViewById(R.id.value)
        holder.devStateIcon = tableRow.findViewById(R.id.devStateIcon)
        return holder
    }

    private fun addRow(table: TableLayout, row: TableRow) {
        row.animation = AnimationUtils.loadAnimation(table.context, android.R.anim.fade_in)
        table.addView(row)
    }


    private fun addActionIfRequired(device: GenericDevice, connectionId: String?, table: TableLayout,
                                    item: DeviceViewItem, row: TableRow, providers: List<GenericDetailActionProvider>, context: Context) {
        val xmlListDevice = device.xmlListDevice ?: return

        val attributeActions = providers
                .map { it.stateAttributeActionFor(item) }
                .filter { it.isPresent }
                .map { it.get() }
                .toList()

        if (!attributeActions.isEmpty()) {
            for (action in attributeActions) {
                if (action.supports(xmlListDevice)) {
                    addRow(table, action.createRow(xmlListDevice, connectionId, item.key, item.getValueFor(device), context, table))
                    return
                }
            }
        }

        if (item.sortKey.equals("state", ignoreCase = true)) {
            if (dimmableStrategy.supports(device)) {
                addRow(table, dimmableStrategy.createDetailView(device, row, context, connectionId))
            } else if (toggleableStrategy.supports(device)) {
                addRow(table, toggleableStrategy.createDetailView(device, context, connectionId))
            }
            if (!device.webCmd.isEmpty()) {
                addRow(table, WebCmdActionRow(WebCmdActionRow.LAYOUT_DETAIL, context).createRow(context, table, device, connectionId))
            }
            return
        }

        val setListEntry = device.xmlListDevice.setList[item.key, true]

        if (setListEntry is SliderSetListEntry) {
            addRow(table, StateChangingSeekBarFullWidth(
                    context, stateUiService, applicationProperties, DimmableBehavior.continuousBehaviorFor(device, item.key, connectionId).get(), row)
                    .createRow(LayoutInflater.from(context), device))
        } else if (setListEntry is GroupSetListEntry) {
            val groupStates = setListEntry.groupStates
            if (groupStates.size <= 1) return

            if ((groupStates.contains("on") && groupStates.contains("off") || groupStates.contains("ON") && groupStates.contains("OFF")) && groupStates.size < 5) {
                addRow(table, OnOffSubStateActionRow(AbstractOnOffActionRow.LAYOUT_DETAIL, setListEntry.key, connectionId)
                        .createRow(device, context))
            } else {
                addRow(table, StateChangingSpinnerActionRow(context, null, item.getName(deviceDescMapping, context), groupStates, item.getValueFor(device), item.key)
                        .createRow(xmlListDevice, connectionId, table))
            }
        } else if (setListEntry is RGBSetListEntry) {
            addRow(table, StateChangingColorPickerRow(stateUiService, xmlListDevice, connectionId, setListEntry)
                    .createRow(context, LayoutInflater.from(context), table))
        }
    }
}