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

package li.klass.fhem.timer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.google.common.collect.Lists
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.timer.TimerListAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.AtDevice
import li.klass.fhem.domain.core.DeviceType
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.device.DeviceActionUtil
import org.jetbrains.anko.coroutines.experimental.bg
import javax.inject.Inject

class TimerListFragment : BaseFragment() {
    private var contextMenuClickedDevice: AtDevice? = null
    private var listView: ListView? = null

    private var createNewDeviceCalled = false

    @Inject
    lateinit var deviceListService: DeviceListService
    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService
    @Inject
    lateinit var appWidgetUpdateService: AppWidgetUpdateService

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            return view
        }
        val context = activity

        val listAdapter = TimerListAdapter(context, Lists.newArrayList<AtDevice>())

        val layout = inflater.inflate(R.layout.timer_overview, container, false)
        val emptyView = layout.findViewById<TextView>(android.R.id.empty)
        listView = layout.findViewById(R.id.list)

        listView!!.emptyView = emptyView
        listView!!.adapter = listAdapter
        registerForContextMenu(listView!!)
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, myView, _, _ ->
            val device = myView.tag as AtDevice

            activity?.sendBroadcast(Intent(Actions.SHOW_FRAGMENT)
                    .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.TIMER_DETAIL)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.name))
        }

        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.timers_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.timer_add) {
            createNewDeviceCalled = true

            val intent = Intent(Actions.SHOW_FRAGMENT)
            intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.TIMER_DETAIL)
            activity?.sendBroadcast(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (createNewDeviceCalled) {
            createNewDeviceCalled = false
            update(true)
        } else {
            update(false)
        }
    }

    override fun canChildScrollUp(): Boolean {
        if (listView?.canScrollVertically(-1) == true) {
            return true
        }
        return super.canChildScrollUp()
    }

    override fun update(refresh: Boolean) {
        val myActivity = activity ?: return
        async(UI) {
            val allRoomsDeviceList = bg {
                if (refresh) {
                    deviceListUpdateService.updateAllDevices()
                    appWidgetUpdateService.updateAllWidgets()
                }
                deviceListService.getAllRoomsDeviceList()

            }.await()
            adapter?.updateData(allRoomsDeviceList.getDevicesOfType(DeviceType.AT))
            myActivity.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
        }
    }

    private val adapter: TimerListAdapter?
        get() {
            if (view == null) return null
            val listView = view!!.findViewById<ListView>(R.id.list)
            return listView.adapter as TimerListAdapter
        }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo)

        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        val atDevice = info.targetView.tag as AtDevice

        Log.e(TAG, atDevice.name + " context menu")

        menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_delete)

        this.contextMenuClickedDevice = atDevice
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            CONTEXT_MENU_DELETE -> {
                DeviceActionUtil.deleteDevice(activity, contextMenuClickedDevice)
                return true
            }
        }

        return super.onContextItemSelected(item)
    }

    override fun getTitle(context: Context): CharSequence? = context.getString(R.string.timer)

    companion object {
        private val TAG = TimerListFragment::class.java.name
        private val CONTEXT_MENU_DELETE = 1
    }
}
