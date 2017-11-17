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

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.device_name_selection.view.*
import kotlinx.android.synthetic.main.room_detail.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.DeviceGroupAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.devices.list.backend.ViewableElementsCalculator
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.Serializable
import javax.inject.Inject

abstract class DeviceNameListFragment : BaseFragment() {

    protected var resultReceiver: ResultReceiver? = null

    @Inject
    lateinit var applicationProperties: ApplicationProperties
    @Inject
    lateinit var deviceListService: DeviceListService
    @Inject
    lateinit var viewableElementsCalculator: ViewableElementsCalculator
    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService
    @Inject
    lateinit var appWidgetUpdateService: AppWidgetUpdateService

    private var roomName: String? = null
    private var deviceName: String? = null
    protected var callingFragment: FragmentType? = null
    private var deviceFilter: DeviceFilter = object : DeviceFilter {
        override fun isSelectable(device: FhemDevice) = true
    }
    private var emptyTextId: Int = R.string.devicelist_empty

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        args ?: return

        roomName = args.getString(ROOM_NAME)
        resultReceiver = args.getParcelable<ResultReceiver>(RESULT_RECEIVER)
        deviceName = args.getString(DEVICE_NAME)
        callingFragment = args.getSerializable(CALLING_FRAGMENT) as FragmentType?
        if (args.containsKey(DEVICE_FILTER)) deviceFilter = args.getSerializable(DEVICE_FILTER) as DeviceFilter
        emptyTextId = if (args.containsKey(EMPTY_TEXT_ID)) args.getInt(EMPTY_TEXT_ID) else R.string.devicelist_empty
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        container ?: return superView
        if (superView != null) return superView

        val view = inflater.inflate(R.layout.device_name_list, container, false)!!

        val emptyView = view.findViewById<LinearLayout>(R.id.emptyView)
        fillEmptyView(emptyView, emptyTextId, container)

        return view
    }

    override fun canChildScrollUp(): Boolean {
        if (view?.devices?.canScrollVertically(-1) == true) {
            return true
        }
        return super.canChildScrollUp()
    }

    protected abstract fun onDeviceNameClick(child: FhemDevice)

    override fun update(refresh: Boolean) {
        val myActivity = activity ?: return
        async(UI) {
            val elements = bg {
                myActivity.sendBroadcast(Intent(SHOW_EXECUTING_DIALOG))

                if (refresh && !isNavigation) {
                    deviceListUpdateService.updateAllDevices()
                    appWidgetUpdateService.updateAllWidgets()
                    myActivity.sendBroadcast(Intent(UPDATE_NAVIGATION))
                }
                val deviceList = when {
                    roomName != null -> deviceListService.getDeviceListForRoom(roomName!!)
                    else -> deviceListService.getAllRoomsDeviceList()
                }.filter(myActivity, deviceFilter::isSelectable)

                val elements = viewableElementsCalculator.calculateElements(myActivity, deviceList)
                if (!isNavigation) {
                    myActivity.sendBroadcast(Intent(DISMISS_EXECUTING_DIALOG))
                }
                elements
            }.await()
            deviceListReceived(elements)
        }
    }

    protected fun deviceListReceived(elements: List<ViewableElementsCalculator.Element>) {
        val devicesView = view?.devices ?: return
        devicesView.adapter = DeviceGroupAdapter(elements, DeviceGroupAdapter.Configuration(
                deviceResourceId = if (isNavigation) R.layout.device_name_selection_navigation else R.layout.device_name_selection,
                bind = { device, view ->
                    view.name.text = device.aliasOrName
                    view.onClick { onDeviceNameClick(device) }
                    view.setBackgroundColor(when (deviceName?.equals(device.name)) {
                        true -> R.color.android_green
                        else -> android.R.color.transparent
                    })
                }
        ))
        devicesView.layoutManager = StaggeredGridLayoutManager(getNumberOfColumns(), StaggeredGridLayoutManager.VERTICAL)
        view?.invalidate()

        if (elements.isNotEmpty()) {
            hideEmptyView()
        } else {
            showEmptyView()
        }
    }

    private fun getNumberOfColumns(): Int {
        fun dpFromPx(px: Float): Float = px / Resources.getSystem().displayMetrics.density

        val displayMetrics = Resources.getSystem().displayMetrics
        val calculated = (dpFromPx(displayMetrics.widthPixels.toFloat()) / 250).toInt()
        return when {
            isNavigation -> 1
            calculated < 1 -> 1
            else -> calculated
        }
    }

    interface DeviceFilter : Serializable {
        fun isSelectable(device: FhemDevice): Boolean
    }
}
