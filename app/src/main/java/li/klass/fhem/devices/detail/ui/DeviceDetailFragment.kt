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

package li.klass.fhem.devices.detail.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG
import li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragment
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragmentViewModel
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.device.DeviceActionUIService
import li.klass.fhem.widget.notification.NotificationSettingView
import javax.inject.Inject

class DeviceDetailFragment @Inject constructor(
        private val favoritesService: FavoritesService,
        private val advertisementService: AdvertisementService,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val deviceListService: DeviceListService,
        private val appWidgetUpdateService: AppWidgetUpdateService,
        private val genericOverviewDetailAdapter: GenericOverviewDetailDeviceAdapter,
        private val deviceActionUIService: DeviceActionUIService,
        deviceNameListNavigationFragment: DeviceNameListNavigationFragment
) : BaseFragment() {
    private var device: FhemDevice? = null
    val args: DeviceDetailFragmentArgs by navArgs()

    private val navigationViewModel by navGraphViewModels<DeviceNameListNavigationFragmentViewModel>(R.id.nav_graph)
    private val deviceDetailViewModel by navGraphViewModels<DeviceDetailFragmentViewModel>(R.id.nav_graph)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigationViewModel.selectedDevice.value = args.deviceName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceDetailViewModel.reset()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView
        val myActivity = activity ?: return superView

        val view = inflater.inflate(R.layout.device_detail_view_page, container, false)
        advertisementService.addAd(view, myActivity)

        return view
    }

    override suspend fun update(refresh: Boolean) {
        hideEmptyView()
        val name = args.deviceName

        val myActivity = activity ?: return
        if (refresh) myActivity.sendBroadcast(Intent(SHOW_EXECUTING_DIALOG))

        coroutineScope {
            val device = withContext(Dispatchers.IO) {
                if (refresh) {
                    deviceListUpdateService.updateSingleDevice(name, args.connectionId)
                    appWidgetUpdateService.updateAllWidgets()
                }
                deviceListService.getDeviceForName(name, args.connectionId)
            }
            myActivity.sendBroadcast(Intent(DISMISS_EXECUTING_DIALOG).apply { setPackage(context?.packageName) })
            device?.let {
                this@DeviceDetailFragment.device = it
                val detailView = genericOverviewDetailAdapter.getDeviceDetailView(myActivity, it, args.connectionId, findNavController(), deviceDetailViewModel.expandHandler())
                myActivity.invalidateOptionsMenu()
                val contentView = findContentView()
                if (contentView != null) {
                    contentView.addView(detailView)
                    if (contentView.childCount > 1) {
                        contentView.removeViewAt(0)
                    }
                }
                setTitle(device.aliasOrName)

            }
        }
    }

    private fun findContentView(): LinearLayout? = view?.findViewById(R.id.deviceDetailView)

    override fun getTitle(context: Context) =
            device?.aliasOrName ?: args.deviceName

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (device != null) {
            inflater.inflate(R.menu.device_menu, menu)
            if (favoritesService.isFavorite(args.deviceName)) {
                menu.removeItem(R.id.menu_favorites_add)
            } else {
                menu.removeItem(R.id.menu_favorites_remove)
            }
            menu.removeItem(R.id.menu_rename)
            menu.removeItem(R.id.menu_delete)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val context = activity ?: return false
        when (item.itemId) {

            R.id.menu_favorites_add -> {
                callUpdating(favoritesService::addFavorite, R.string.context_favoriteadded)
            }
            R.id.menu_favorites_remove -> {
                callUpdating(favoritesService::removeFavorite, R.string.context_favoriteremoved)
            }
            R.id.menu_room -> deviceActionUIService.moveDevice(context, device!!)
            R.id.menu_alias -> deviceActionUIService.setAlias(context, device!!)
            R.id.menu_notification -> NotificationSettingView(activity, args.deviceName).show(activity)
            else -> return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun callUpdating(actionToCall: (String) -> Unit, toastStringId: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                actionToCall(args.deviceName)
            }
            showToast(toastStringId)
            update(false)
        }
    }

    private fun showToast(textStringId: Int) {
        Toast.makeText(activity, textStringId, Toast.LENGTH_SHORT).show()
    }

    override val navigationFragment: Fragment? = deviceNameListNavigationFragment

    override fun canChildScrollUp(): Boolean =
            super.canChildScrollUp() || (view?.findViewById<ScrollView>(R.id.scrollView)?.scrollY
                    ?: 0) > 0
}
