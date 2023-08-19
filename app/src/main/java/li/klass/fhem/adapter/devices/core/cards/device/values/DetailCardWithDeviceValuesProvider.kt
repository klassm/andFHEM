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
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TableLayout
import android.widget.TableRow
import androidx.cardview.widget.CardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.DevStateIconAdder
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItemSorter
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.GenericDetailActionProviders
import li.klass.fhem.adapter.devices.genericui.HolderActionRow
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
import li.klass.fhem.databinding.DeviceDetailCardTableBinding
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.RGBSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.util.ApplicationProperties
import javax.inject.Inject

class DetailCardWithDeviceValuesProvider @Inject constructor(
        private val deviceViewItemSorter: DeviceViewItemSorter,
        private val xmlDeviceItemProvider: XmlDeviceItemProvider,
        private val devStateIconAdder: DevStateIconAdder,
        private val toggleableStrategy: ToggleableStrategy,
        private val dimmableStrategy: DimmableStrategy,
        private val stateUiService: StateUiService,
        private val applicationProperties: ApplicationProperties,
        private val detailActionProviders: GenericDetailActionProviders) {

    suspend fun createCard(device: FhemDevice, connectionId: String?, caption: Int,
                           itemProvider: ItemProvider, context: Context, expandHandler: ExpandHandler): CardView {
        val binding =
            DeviceDetailCardTableBinding.inflate(LayoutInflater.from(context), null, false)

        val captionTextView = binding.cardCaption
        captionTextView.setText(caption)

        fillTableWithButton(
            device,
            connectionId,
            caption,
            itemProvider,
            context,
            expandHandler,
            binding
        )

        return binding.root
    }

    private suspend fun fillTableWithButton(
        device: FhemDevice, connectionId: String?, caption: Int,
        itemProvider: ItemProvider, context: Context, expandHandler: ExpandHandler,
        binding: DeviceDetailCardTableBinding
    ) {
        val expandSaveKey = caption.toString()
        val limitedItems = getSortedClassItems(device, itemProvider, false, context)
        val allItems = getSortedClassItems(device, itemProvider, true, context)
        val canExpand = limitedItems.isNotEmpty() && limitedItems.size != allItems.size
        val isExpanded = expandHandler.isExpanded(expandSaveKey)
        val table = binding.table
        val providers = detailActionProviders.providers
            .filter { it.supports(device.xmlListDevice) }
            .toList()

        binding.expandButton.apply {
            visibility = if (canExpand) View.VISIBLE else View.GONE
            text =
                context.getString(if (isExpanded) R.string.detailCardUnexpand else R.string.detailCardExpand)
            setOnClickListener {
                expandHandler.setExpanded(expandSaveKey, !expandHandler.isExpanded(expandSaveKey))
                GlobalScope.launch(Dispatchers.Main) {
                    fillTableWithButton(
                        device,
                        connectionId,
                        caption,
                        itemProvider,
                        context,
                        expandHandler,
                        binding
                    )
                }
            }
        }

        fillTable(device, connectionId, table, if (isExpanded) allItems else limitedItems, providers, context)

        if (table.childCount == 0) {
            binding.root.visibility = View.GONE
        }
    }

    private suspend fun fillTable(device: FhemDevice, connectionId: String?, table: TableLayout, items: List<XmlDeviceViewItem>, providers: List<GenericDetailActionProvider>, context: Context) {

        table.removeAllViews()

        items.map { it to createHolderWithRow(it, device, context) }
                .filter { it.second != null }
                .map { it.first to it.second!! }
                .forEach { (item, holder) ->
                    addRow(table, holder.row)
                    addActionIfRequired(device, connectionId, table, item, holder.row, providers, context)
                }

        table.visibility = if (table.childCount == 0) View.GONE else View.VISIBLE
    }

    private fun getSortedClassItems(device: FhemDevice, itemProvider: ItemProvider, showUnknown: Boolean, context: Context): List<XmlDeviceViewItem> {
        val xmlViewItems = itemProvider.itemsFor(xmlDeviceItemProvider, device, showUnknown, context)
        return deviceViewItemSorter.sortedViewItemsFrom(xmlViewItems)
    }


    private fun createHolderWithRow(item: XmlDeviceViewItem, device: FhemDevice, context: Context): GenericDeviceTableRowHolder? {
        val value = item.value
        if (value.isBlank()) {
            return null
        }

        val holder = createEmptyRow(LayoutInflater.from(context), R.layout.device_detail_generic_table_row)
        holder.description.text = item.desc
        holder.value.text = value
        devStateIconAdder.addDevStateIconIfRequired(value, device, holder.devStateIcon)

        return holder
    }

    private fun createEmptyRow(inflater: LayoutInflater, resource: Int): GenericDeviceTableRowHolder {
        val tableRow = inflater.inflate(resource, null) as TableRow
        return GenericDeviceTableRowHolder(
                tableRow,
                tableRow.findViewById(R.id.description),
                tableRow.findViewById(R.id.value),
            tableRow.findViewById(R.id.devStateIcon)
        )
    }

    private fun addRow(table: TableLayout, row: TableRow) {
        row.animation = AnimationUtils.loadAnimation(table.context, android.R.anim.fade_in)
        table.addView(row)
    }


    private fun addActionIfRequired(
        device: FhemDevice,
        connectionId: String?,
        table: TableLayout,
        item: XmlDeviceViewItem,
        row: TableRow,
        providers: List<GenericDetailActionProvider>,
        context: Context
    ) {
        val xmlListDevice = device.xmlListDevice

        val attributeActions = providers.mapNotNull { it.stateAttributeActionFor(item) }
            .toList()

        if (attributeActions.isNotEmpty()) {
            attributeActions
                .filter { it.supports(xmlListDevice) }
                .forEach {
                    addRow(
                        table,
                        it.createRow(
                            xmlListDevice,
                            connectionId,
                            item.key,
                            item.value,
                            context,
                            table
                        )
                    )
                }
        }

        if (item.sortKey.equals("state", ignoreCase = true)) {
            if (dimmableStrategy.supports(device)) {
                addRow(table, dimmableStrategy.createDetailView(device, row, context, connectionId))
            } else if (toggleableStrategy.supports(device)) {
                addRow(table, toggleableStrategy.createDetailView(device, context, connectionId))
            }
            if (device.webCmd.isNotEmpty()) {
                addRow(table, WebCmdActionRow(stateUiService, context, HolderActionRow.LAYOUT_DETAIL).createRow(context, table, device, connectionId))
            }
            return
        }

        val setListEntry = device.setList[item.key, true]

        if (setListEntry is SliderSetListEntry) {
            addRow(
                table, StateChangingSeekBarFullWidth(
                    context,
                    stateUiService,
                    applicationProperties,
                    DimmableBehavior.continuousBehaviorFor(device, item.key, connectionId)!!,
                    row
                )
                    .createRow(LayoutInflater.from(context), device.xmlListDevice)
            )
        } else if (setListEntry is GroupSetListEntry) {
            val groupStates = setListEntry.groupStates
            if (groupStates.size <= 1) return

            if ((groupStates.contains("on") && groupStates.contains("off") || groupStates.contains("ON") && groupStates.contains("OFF")) && groupStates.size < 5) {
                addRow(table, OnOffSubStateActionRow(AbstractOnOffActionRow.LAYOUT_DETAIL, setListEntry.key, connectionId, stateUiService)
                        .createRow(device, context))
            } else {
                addRow(table, StateChangingSpinnerActionRow(context, stateUiService, null, item.desc, groupStates, item.value, item.key)
                        .createRow(xmlListDevice, connectionId, table))
            }
        } else if (setListEntry is RGBSetListEntry) {
            addRow(table, StateChangingColorPickerRow(stateUiService, xmlListDevice, connectionId, setListEntry)
                    .createRow(context, LayoutInflater.from(context), table))
        }
    }
}