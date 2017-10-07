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
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.common.base.Strings
import com.google.common.collect.FluentIterable.from
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction
import li.klass.fhem.adapter.devices.genericui.HolderActionRow
import li.klass.fhem.adapter.devices.genericui.WebCmdActionRow
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.genericview.DetailViewSettings
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition.BY_NAME
import org.jetbrains.anko.coroutines.experimental.bg

abstract class ExplicitOverviewDetailDeviceAdapter : OverviewDeviceAdapter() {

    private val detailViewLayout: Int
        get() = R.layout.device_detail_explicit

    init {
        afterPropertiesSet()
        registerFieldListener("state", object : FieldNameAddedToDetailListener() {
            override fun onFieldNameAdded(context: Context, tableLayout: TableLayout, field: String,
                                          device: FhemDevice, connectionId: String?, fieldTableRow: TableRow) {
                createWebCmdTableRowIfRequired(LayoutInflater.from(context), tableLayout, device, connectionId)
            }
        })
    }

    protected open fun afterPropertiesSet() {}

    private fun createWebCmdTableRowIfRequired(inflater: LayoutInflater, layout: TableLayout,
                                               device: FhemDevice, connectionId: String?) {
        if (device.webCmd.isEmpty()) return
        val context = inflater.context

        layout.addView(WebCmdActionRow(HolderActionRow.LAYOUT_DETAIL, context)
                .createRow(context, layout, device, connectionId))
    }

    override fun supportsDetailView(device: FhemDevice): Boolean = true

    override fun getDeviceDetailView(context: Context, device: FhemDevice, graphDefinitions: Set<SvgGraphDefinition>, connectionId: String?): View {
        val view = LayoutInflater.from(context).inflate(detailViewLayout, null)
        fillDeviceDetailView(context, view, device, connectionId)

        return view
    }

    private fun fillDeviceDetailView(context: Context, view: View, device: FhemDevice, connectionId: String?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val layout = view.findViewById<View>(R.id.generic) as TableLayout

        try {
            val annotation = device.javaClass.getAnnotation(DetailViewSettings::class.java)
            val items = getSortedAnnotatedClassItems(device, context)

            for (item in items) {
                val name = item.sortKey

                if (annotation != null) {
                    if (name.equals("state", ignoreCase = true) && !annotation.showState) {
                        continue
                    }

                    if (name.equals("measured", ignoreCase = true) && !annotation.showMeasured) {
                        continue
                    }
                }

                if (item.isShowInDetail) {
                    val holder = createTableRow(inflater, R.layout.device_detail_generic_table_row)
                    fillTableRow(holder, item, device, context)
                    layout.addView(holder.row)
                    notifyFieldListeners(context, device, connectionId, layout, name, holder.row)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "exception occurred while setting device overview values", e)
        }

        addDetailGraphButtons(context, view, device, emptySet(), connectionId, inflater)

        addDetailActionButtons(context, view, device, inflater, connectionId)

        val otherStuffView = view.findViewById<View>(R.id.otherStuff) as LinearLayout
        fillOtherStuffDetailLayout(context, otherStuffView, device, inflater)

        updateGeneralDetailsNotificationText(context, view, device)
    }

    private fun notifyFieldListeners(context: Context, device: FhemDevice, connectionId: String?, layout: TableLayout, fieldName: String, fieldTableRow: TableRow) {
        if (!fieldNameAddedListeners.containsKey(fieldName)) {
            return
        }

        val listeners = fieldNameAddedListeners[fieldName]
        listeners?.forEach {
            if (it.supportsDevice(device)) {
                it.onFieldNameAdded(context, layout, fieldName, device, connectionId, fieldTableRow)
            }
        }
    }

    private fun addDetailGraphButtons(context: Context, view: View, device: FhemDevice, graphDefinitions: Set<SvgGraphDefinition>, connectionId: String?, inflater: LayoutInflater) {
        val graphLayout = view.findViewById<View>(R.id.graphButtons) as LinearLayout
        val definitions = from(graphDefinitions).toSortedList(BY_NAME)
        if (definitions.isEmpty()) {
            graphLayout.visibility = View.GONE
            return
        }
        graphLayout.visibility = View.VISIBLE
        graphLayout.removeAllViews()
        for (svgGraphDefinition in definitions) {
            addGraphButton(context, graphLayout, inflater, device, connectionId, svgGraphDefinition)
        }
    }

    private fun addDetailActionButtons(context: Context, view: View, device: FhemDevice, inflater: LayoutInflater, connectionId: String?) {
        val actionLayout = view.findViewById<LinearLayout>(R.id.actionButtons)
        async(UI) {
            val actions = bg {
                provideDetailActions()
                        .filter { it.isVisible(device, context) }
            }.await()

            if (actions.isEmpty()) {
                actionLayout.visibility = View.GONE
            }
            actions.map { it.createView(context, inflater, device, actionLayout, connectionId) }
                    .forEach { actionLayout.addView(it) }
        }
    }

    protected open fun fillOtherStuffDetailLayout(context: Context, layout: LinearLayout, device: FhemDevice, inflater: LayoutInflater) {}

    private fun updateGeneralDetailsNotificationText(myContext: Context, view: View, device: FhemDevice) {
        async(UI) {
            val text = bg {
                getGeneralDetailsNotificationText(myContext, device)
            }.await()
            val notificationView = view.findViewById<TextView>(R.id.general_details_notification)

            if (Strings.isNullOrEmpty(text)) {
                notificationView.visibility = View.GONE
            } else {
                notificationView.text = text
                notificationView.visibility = View.VISIBLE
            }
        }
    }

    private fun addGraphButton(context: Context, graphLayout: LinearLayout,
                               inflater: LayoutInflater, device: FhemDevice, connectionId: String?, svgGraphDefinition: SvgGraphDefinition) {
        val button = inflater.inflate(R.layout.button_device_detail, graphLayout, false) as Button

        fillGraphButton(context, device, connectionId, svgGraphDefinition, button)
        graphLayout.addView(button)
    }

    protected open fun getGeneralDetailsNotificationText(context: Context, device: FhemDevice): String? =
            null

    override fun onFillDeviceDetailIntent(context: Context, device: FhemDevice, intent: Intent): Intent =
            intent

    protected open fun provideDetailActions(): MutableList<DeviceDetailViewAction> = Lists.newArrayList()

    override fun attachGraphs(context: Context, detailView: View, graphDefinitions: ImmutableSet<SvgGraphDefinition>, connectionId: String?, device: FhemDevice) {
        addDetailGraphButtons(context, detailView, device, graphDefinitions, connectionId, LayoutInflater.from(context))
    }

    companion object {
        private val TAG = ExplicitOverviewDetailDeviceAdapter::class.java.name
    }
}
