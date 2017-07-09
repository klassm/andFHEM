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

package li.klass.fhem.fragments.core

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.CardView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.room_detail.view.*
import kotlinx.android.synthetic.main.room_device_content.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.DeviceGroupAdapter
import li.klass.fhem.adapter.rooms.ViewableElementsCalculator
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.Actions.FAVORITE_ADD
import li.klass.fhem.constants.Actions.FAVORITE_REMOVE
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.IS_FAVORITE
import li.klass.fhem.constants.PreferenceKeys.DEVICE_LIST_RIGHT_PADDING
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.domain.core.DeviceType
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.fhem.DataConnectionSwitch
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.service.intent.FavoritesIntentService
import li.klass.fhem.service.room.RoomListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.FhemResultReceiver
import li.klass.fhem.util.device.DeviceActionUtil
import li.klass.fhem.widget.notification.NotificationSettingView
import org.apache.commons.lang3.time.StopWatch
import org.jetbrains.anko.coroutines.experimental.bg
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

abstract class DeviceListFragment : BaseFragment() {

    @Inject
    lateinit var dataConnectionSwitch: DataConnectionSwitch
    @Inject
    lateinit var applicationProperties: ApplicationProperties
    @Inject
    lateinit var viewableElementsCalculator: ViewableElementsCalculator
    @Inject
    lateinit var advertisementService: AdvertisementService
    @Inject
    lateinit var roomListUpdateService: RoomListUpdateService

    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            val inflater = actionMode.menuInflater
            inflater.inflate(R.menu.device_menu, menu)
            if (isClickedDeviceFavorite.get()) {
                menu.removeItem(R.id.menu_favorites_add)
            } else {
                menu.removeItem(R.id.menu_favorites_remove)
            }
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.menu_favorites_add -> {
                    val favoriteAddIntent = Intent(FAVORITE_ADD)
                    favoriteAddIntent.setClass(activity, FavoritesIntentService::class.java)
                    favoriteAddIntent.putExtra(BundleExtraKeys.DEVICE, contextMenuClickedDevice.get())
                    favoriteAddIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, object : ResultReceiver(Handler()) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            if (resultCode != ResultCodes.SUCCESS) return

                            Toast.makeText(activity, R.string.context_favoriteadded, Toast.LENGTH_SHORT).show()
                        }
                    })
                    activity.startService(favoriteAddIntent)
                }
                R.id.menu_favorites_remove -> {
                    val favoriteRemoveIntent = Intent(FAVORITE_REMOVE)
                    favoriteRemoveIntent.setClass(activity, FavoritesIntentService::class.java)
                    favoriteRemoveIntent.putExtra(BundleExtraKeys.DEVICE, contextMenuClickedDevice.get())
                    favoriteRemoveIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, object : ResultReceiver(Handler()) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            if (resultCode != ResultCodes.SUCCESS) return

                            Toast.makeText(activity, R.string.context_favoriteremoved, Toast.LENGTH_SHORT).show()
                        }
                    })
                    activity.startService(favoriteRemoveIntent)
                }
                R.id.menu_rename -> DeviceActionUtil.renameDevice(activity, contextMenuClickedDevice.get())
                R.id.menu_delete -> DeviceActionUtil.deleteDevice(activity, contextMenuClickedDevice.get())
                R.id.menu_room -> DeviceActionUtil.moveDevice(activity, contextMenuClickedDevice.get())
                R.id.menu_alias -> DeviceActionUtil.setAlias(activity, contextMenuClickedDevice.get())
                R.id.menu_notification -> handleNotifications(contextMenuClickedDevice.get().name)
                else -> return false
            }
            actionMode.finish()
            update(false)
            return true
        }

        override fun onDestroyActionMode(actionMode: ActionMode) {
        }
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fun getNumberOfColumns(): Int {
            fun dpFromPx(px: Float): Float {
                return px / Resources.getSystem().displayMetrics.density
            }

            val displayMetrics = Resources.getSystem().displayMetrics
            val calculated = (dpFromPx(displayMetrics.widthPixels.toFloat()) / 400F).toInt()
            return when {
                calculated < 1 -> 1
                else -> calculated
            }
        }

        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView

        val view = inflater!!.inflate(R.layout.room_detail, container, false)
        advertisementService.addAd(view, activity)

        assert(view != null)

        val emptyView = view!!.findViewById(R.id.emptyView) as LinearLayout
        fillEmptyView(emptyView, container!!)

        if (!isNavigation) {
            val rightPadding = applicationProperties.getIntegerSharedPreference(DEVICE_LIST_RIGHT_PADDING, 0, activity)
            view.setPadding(view.paddingLeft, view.paddingTop,
                    rightPadding, view.paddingBottom)
        }

        view.devices.adapter = DeviceGroupAdapter(emptyList(),
                configuration = DeviceGroupAdapter.Configuration(
                        deviceResourceId = R.layout.room_device_content,
                        bind = this@DeviceListFragment::createDeviceView))

        view.devices.layoutManager = StaggeredGridLayoutManager(getNumberOfColumns(), VERTICAL)

        return view
    }

    override fun onResume() {
        super.onResume()
        LOGGER.info("onResume - fragment {} resumes", javaClass.name)
    }

    override fun canChildScrollUp(): Boolean {
        return ViewCompat.canScrollVertically(view?.devices, -1) || super.canChildScrollUp()
    }

    protected open fun fillEmptyView(view: LinearLayout, viewGroup: ViewGroup) {
        val emptyView = LayoutInflater.from(activity).inflate(R.layout.empty_view, viewGroup, false)!!
        val emptyText = emptyView.findViewById(R.id.emptyText) as TextView
        emptyText.setText(R.string.noDevices)

        view.addView(emptyView)
    }

    override fun update(doUpdate: Boolean) {

        if (doUpdate) {
            activity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))
        }

        Log.i(DeviceListFragment::class.java.name, "request device list update (doUpdate=$doUpdate)")

        async(UI) {
            val elements = bg {
                if (doUpdate) {
                    activity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))
                    executeRemoteUpdate()
                    activity.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
                    activity.sendBroadcast(Intent(Actions.UPDATE_NAVIGATION))
                }
                val deviceList = getRoomDeviceListForUpdate()
                viewableElementsCalculator.calculateElements(activity, deviceList)
            }.await()
            if (view != null) {
                updateWith(elements, view!!)
            }
        }
    }

    abstract fun getRoomDeviceListForUpdate(): RoomDeviceList

    abstract fun executeRemoteUpdate()

    private fun updateWith(elements: List<ViewableElementsCalculator.Element>, view: View) {
        val stopWatch = StopWatch()
        stopWatch.start()
        (view.devices.adapter as DeviceGroupAdapter)
                .updateWidth(elements)
        LOGGER.debug("updateWith - adapter is set, time=${stopWatch.time}")

        if (elements.isEmpty()) {
            showEmptyView()
        } else {
            hideEmptyView()
        }
        LOGGER.debug("updateWith - adapter is set, time=${stopWatch.time}")

        val dummyConnectionNotification = view.findViewById(R.id.dummyConnectionNotification)
        if (!dataConnectionSwitch.isDummyDataActive(activity)) {
            dummyConnectionNotification.visibility = View.GONE
        } else {
            dummyConnectionNotification.visibility = View.VISIBLE
        }
        LOGGER.debug("updateWith - update dummyConnectionNotification, time=${stopWatch.time}")
    }

    private fun createDeviceView(device: FhemDevice, view: View) {
        fun firstChildOf(layout: CardView) = when (layout.childCount) {
            0 -> null
            else -> layout.getChildAt(0)
        }

        val stopWatch = StopWatch()
        stopWatch.start()

        val adapter = DeviceType.getAdapterFor(device)

        LOGGER.debug("bind - getAdapterFor device=${device.name}, time=${stopWatch.time}")

        val contentView = adapter.createOverviewView(firstChildOf(view.card), device, view.context)

        LOGGER.debug("bind - creating view for device=${device.name}, time=${stopWatch.time}")

        view.card.removeAllViews()
        view.card.addView(contentView)

        LOGGER.debug("bind - adding content view device=${device.name}, time=${stopWatch.time}")

        view.setOnClickListener { onClick(device) }
        view.setOnLongClickListener { onLongClick(device) }

        LOGGER.debug("bind - finished device=${device.name}, time=${stopWatch.time}")
    }


    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo)

        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        val tag = info.targetView.tag ?: return

        if (tag is FhemDevice) {
            contextMenuClickedDevice.set(tag)
            currentClickFragment.set(this)

            (activity as AppCompatActivity).startSupportActionMode(actionModeCallback)
        }
    }

    private fun handleNotifications(deviceName: String) {
        NotificationSettingView(activity, deviceName).show(activity)
    }

    private fun onClick(device: FhemDevice) {
        val adapter = DeviceType.getAdapterFor(device)
        if (adapter != null && adapter.supportsDetailView(device)) {
            if (actionMode != null) actionMode!!.finish()
            adapter.gotoDetailView(activity, device)
        }
    }

    private fun onLongClick(device: FhemDevice): Boolean {
        val intent = Intent(Actions.FAVORITES_IS_FAVORITES)
                .setClass(activity, FavoritesIntentService::class.java)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, object : FhemResultReceiver() {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                        contextMenuClickedDevice.set(device)
                        isClickedDeviceFavorite.set(resultData.getBoolean(IS_FAVORITE))
                        actionMode = (activity as AppCompatActivity).startSupportActionMode(actionModeCallback)
                    }
                })
        activity?.startService(intent)

        return true
    }

    companion object {
        protected var contextMenuClickedDevice = AtomicReference<FhemDevice>()
        protected var currentClickFragment = AtomicReference<DeviceListFragment>()
        protected var isClickedDeviceFavorite = AtomicBoolean(false)
        private val LOGGER = LoggerFactory.getLogger(DeviceListFragment::class.java)
    }
}
