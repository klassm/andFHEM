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

package li.klass.fhem.devices.list.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter
import li.klass.fhem.adapter.rooms.DeviceGroupAdapter
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.constants.Actions
import li.klass.fhem.databinding.RoomDeviceContentBinding
import li.klass.fhem.devices.list.backend.ViewableElementsCalculator
import li.klass.fhem.devices.list.backend.ViewableRoomDeviceListProvider
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.fragments.device.DeviceNameListFragmentDirections
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.device.DeviceActionUIService
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
abstract class DeviceListFragment(
        private val dataConnectionSwitch: DataConnectionSwitch,
        private val applicationProperties: ApplicationProperties,
        private val viewableRoomDeviceListProvider: ViewableRoomDeviceListProvider,
        private val advertisementService: AdvertisementService,
        private val favoritesService: FavoritesService,
        private val genericOverviewDetailDeviceAdapter: GenericOverviewDetailDeviceAdapter,
        private val deviceActionUiService: DeviceActionUIService
) : BaseFragment() {
    private var actionMode: ActionMode? = null

    private val viewModel by navGraphViewModels<DeviceListFragmentViewModel>(R.id.nav_graph)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        val myActivity = activity ?: return superView
        if (superView != null) return superView

        val view = inflater.inflate(R.layout.room_detail_page, container, false) ?: return null
        advertisementService.addAd(view, myActivity)

        val emptyView = view.findViewById<LinearLayout>(R.id.emptyView)
        fillEmptyView(emptyView, container!!)

        deviceListFor(view)?.adapter = DeviceGroupAdapter(
            emptyList(),
            configuration = DeviceGroupAdapter.Configuration(
                deviceResourceId = R.layout.room_device_content,
                bind = this@DeviceListFragment::createDeviceView
            )
        )

        deviceListFor(view)?.layoutManager =
            StaggeredGridLayoutManager(getNumberOfColumns(), VERTICAL)

        return view
    }

    private fun deviceListFor(view: View?): RecyclerView? = view?.findViewById(R.id.devices)

    private fun getNumberOfColumns(): Int {
        fun dpFromPx(px: Float): Float = px / Resources.getSystem().displayMetrics.density

        val displayMetrics = Resources.getSystem().displayMetrics
        val calculated = (dpFromPx(displayMetrics.widthPixels.toFloat()) / getColumnWidth()).toInt()
        return when {
            calculated < 1 -> 1
            else -> calculated
        }
    }

    private fun getColumnWidth(): Float =
            applicationProperties.getIntegerSharedPreference(SettingsKeys.DEVICE_COLUMN_WIDTH, DEFAULT_COLUMN_WIDTH).toFloat()

    override fun onResume() {
        super.onResume()
        val layoutManager = deviceListFor(view)?.layoutManager as StaggeredGridLayoutManager
        layoutManager.spanCount = getNumberOfColumns()
        LOGGER.info("onResume - fragment {} resumes", javaClass.name)
    }

    override fun canChildScrollUp(): Boolean =
        deviceListFor(view)?.canScrollVertically(-1) ?: false || super.canChildScrollUp()

    protected open fun fillEmptyView(view: LinearLayout, viewGroup: ViewGroup) {
        val emptyView = LayoutInflater.from(activity).inflate(R.layout.empty_view, viewGroup, false)!!
        val emptyText = emptyView.findViewById<TextView>(R.id.emptyText)
        emptyText.setText(R.string.noDevices)

        view.addView(emptyView)
    }

    override suspend fun update(refresh: Boolean) {
        val myActivity = activity ?: return
        if (refresh) {
            myActivity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))
        }

        Log.i(DeviceListFragment::class.java.name, "request device list update (doUpdate=$refresh)")

        coroutineScope {
            if (refresh) {
                myActivity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))
                withContext(Dispatchers.IO) {
                    executeRemoteUpdate(myActivity)
                }
                myActivity.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            }
            val elements = withContext(Dispatchers.IO) {
                val deviceList = getRoomDeviceListForUpdate(myActivity)
                viewableRoomDeviceListProvider.provideFor(myActivity, deviceList)
            }

            if (view != null) {
                updateWith(elements, requireView())
            }

            roomNameSaveKey?.let { saveKey ->
                viewModel.getState(saveKey)?.let {
                    deviceListFor(view)?.layoutManager?.onRestoreInstanceState(it)
                    viewModel.setState(saveKey, null)
                }
            }

        }
    }

    abstract fun getRoomDeviceListForUpdate(context: Context): RoomDeviceList

    abstract fun executeRemoteUpdate(context: Context)

    private fun updateWith(elements: List<ViewableElementsCalculator.Element>, view: View) {
        val stopWatch = StopWatch()
        stopWatch.start()
        (deviceListFor(view)?.adapter as DeviceGroupAdapter).updateWith(elements)
        LOGGER.debug("updateWith - adapter is set, time=${stopWatch.time}")

        if (elements.isEmpty()) {
            showEmptyView()
        } else {
            hideEmptyView()
        }
        LOGGER.debug("updateWith - adapter is set, time=${stopWatch.time}")

        val dummyConnectionNotification =
                view.findViewById<RelativeLayout>(R.id.dummyConnectionNotification)
        if (!dataConnectionSwitch.isDummyDataActive()) {
            dummyConnectionNotification.visibility = View.GONE
        } else {
            dummyConnectionNotification.visibility = View.VISIBLE
        }

        view.findViewById<Button>(R.id.configureServers).setOnClickListener {
            findNavController().navigate(
                DeviceNameListFragmentDirections.actionToConnectionList()
            )
        }

        LOGGER.debug("updateWith - update dummyConnectionNotification, time=${stopWatch.time}")
    }

    private fun firstChildOf(layout: CardView) = when (layout.childCount) {
        0 -> null
        else -> layout.getChildAt(0)
    }

    private fun createDeviceView(device: FhemDevice, view: View) {
        LOGGER.info("createDeviceView(name=${device.name})")
        val binding = RoomDeviceContentBinding.bind(view)

        val stopWatch = StopWatch()
        stopWatch.start()

        LOGGER.debug("bind - getAdapterFor device=${device.name}, time=${stopWatch.time}")

        val contentView = genericOverviewDetailDeviceAdapter.createOverviewView(
            firstChildOf(binding.card),
            device,
            view.context
        )

        LOGGER.debug("bind - creating view for device=${device.name}, time=${stopWatch.time}")

        binding.card.removeAllViews()
        binding.card.addView(contentView)

        LOGGER.debug("bind - adding content view device=${device.name}, time=${stopWatch.time}")

        view.setOnClickListener { onClick(device) }
        view.setOnLongClickListener { onLongClick(device) }

        LOGGER.debug("bind - finished device=${device.name}, time=${stopWatch.time}")
    }

    private fun onClick(device: FhemDevice) {
        actionMode?.finish()
        navigateTo(device)
    }

    private fun onLongClick(device: FhemDevice): Boolean {
        val myActivity = activity ?: return false
        GlobalScope.launch(Dispatchers.Main) {
            val isFavorite = withContext(Dispatchers.IO) {
                favoritesService.isFavorite(device.name)
            }
            val callback = DeviceListActionModeCallback(favoritesService, deviceActionUiService,
                    device, isFavorite, myActivity, updateListener = {
                updateAsync(false)
            })
            actionMode = (activity as AppCompatActivity).startSupportActionMode(callback)
        }
        return true
    }

    override fun onDestroyView() {
        roomNameSaveKey?.let { saveKey ->
            viewModel.setState(saveKey, deviceListFor(view)?.layoutManager?.onSaveInstanceState())
        }

        super.onDestroyView()
    }

    abstract fun navigateTo(device: FhemDevice)

    abstract val roomNameSaveKey: String?

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DeviceListFragment::class.java)
        const val DEFAULT_COLUMN_WIDTH = 350
    }
}
