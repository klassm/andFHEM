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

package li.klass.fhem.fragments.device

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.DeviceGroupAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.databinding.DeviceNameSelectionBinding
import li.klass.fhem.devices.list.backend.ViewableElementsCalculator
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import java.io.Serializable

abstract class DeviceNameListFragment(
        private val deviceListService: DeviceListService,
        private val viewableElementsCalculator: ViewableElementsCalculator,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val appWidgetUpdateService: AppWidgetUpdateService
) : BaseFragment() {

    open val roomName: String? = null
    open val deviceName: String? = null
    open val deviceFilter: DeviceFilter = object : DeviceFilter {
        override fun isSelectable(device: FhemDevice) = true
    }
    open val emptyTextId: Int = R.string.devicelist_empty

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        container ?: return superView
        if (superView != null) return superView

        val view = inflater.inflate(layout, container, false)!!

        view.findViewById<LinearLayout>(R.id.emptyView)?.let {
            fillEmptyView(it, emptyTextId, container)
        }

        return view
    }

    override fun canChildScrollUp(): Boolean {
        if (view?.findViewById<RecyclerView>(R.id.devices)?.canScrollVertically(-1) == true) {
            return true
        }
        return super.canChildScrollUp()
    }

    protected abstract fun onDeviceNameClick(child: FhemDevice)

    override suspend fun update(refresh: Boolean) {
        val myActivity = activity ?: return
        coroutineScope {
            if (refresh) {
                withContext(Dispatchers.IO) {
                    deviceListUpdateService.updateAllDevices()
                    appWidgetUpdateService.updateAllWidgets()
                }
            }

            val elements = withContext(Dispatchers.IO) {
                val deviceList = when {
                    roomName != null -> deviceListService.getDeviceListForRoom(roomName!!)
                    else -> deviceListService.getAllRoomsDeviceList()
                }.filter(deviceFilter::isSelectable)

                val elements = viewableElementsCalculator.calculateElements(myActivity, deviceList)
                elements
            }

            deviceListReceived(elements)
        }
    }

    private fun deviceListReceived(elements: List<ViewableElementsCalculator.Element>) {
        val devicesView = view?.findViewById<RecyclerView>(R.id.devices) ?: return
        devicesView.adapter = DeviceGroupAdapter(elements, DeviceGroupAdapter.Configuration(
                deviceResourceId = R.layout.device_name_selection,
                bind = { device, view ->
                    val binding = DeviceNameSelectionBinding.bind(view)
                    binding.name.text = device.aliasOrName
                    binding.root.setOnClickListener { onDeviceNameClick(device) }
                    binding.card.setBackgroundColor(
                        when (deviceName?.equals(device.name)) {
                            true -> ContextCompat.getColor(
                                devicesView.context,
                                R.color.android_green
                            )

                            else -> ContextCompat.getColor(
                                devicesView.context,
                                android.R.color.transparent
                            )
                        }
                    )
                    binding.root.tag = device.name
                }
        ))
        devicesView.layoutManager = StaggeredGridLayoutManager(getNumberOfColumns(), StaggeredGridLayoutManager.VERTICAL)
        view?.invalidate()

        val childViewToSelect = elements.indexOfFirst {
            it is ViewableElementsCalculator.Element.Device
                    && it.device.name == deviceName
        }
        devicesView.scrollToPosition(childViewToSelect)

        if (elements.isNotEmpty()) {
            hideEmptyView()
        } else {
            showEmptyView()
        }
    }

    open fun getNumberOfColumns(): Int {
        fun dpFromPx(px: Float): Float = px / Resources.getSystem().displayMetrics.density

        val displayMetrics = Resources.getSystem().displayMetrics
        val calculated = (dpFromPx(displayMetrics.widthPixels.toFloat()) / 250).toInt()
        return when {
            calculated < 1 -> 1
            else -> calculated
        }
    }

    abstract val layout: Int

    interface DeviceFilter : Serializable {
        fun isSelectable(device: FhemDevice): Boolean
    }
}
