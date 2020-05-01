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

import android.content.Context
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import li.klass.fhem.R
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.devices.list.backend.ViewableElementsCalculator
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import javax.inject.Inject

class DeviceNameListNavigationFragment @Inject constructor(
        deviceListService: DeviceListService,
        viewableElementsCalculator: ViewableElementsCalculator,
        deviceListUpdateService: DeviceListUpdateService,
        appWidgetUpdateService: AppWidgetUpdateService
) : DeviceNameListFragment(deviceListService, viewableElementsCalculator, deviceListUpdateService, appWidgetUpdateService) {

    private val navigationViewModel by navGraphViewModels<DeviceNameListNavigationFragmentViewModel>(R.id.nav_graph)

    override fun onDeviceNameClick(child: FhemDevice) {
        if (isResumed) {
            findNavController().navigate(
                    DeviceNameListNavigationFragmentDirections.actionToDeviceDetailRedirect(
                            child.name,
                            null
                    )
            )
        }
    }

    override val layout: Int = R.layout.device_name_list

    override fun mayPullToRefresh(): Boolean = false

    override val canUpdateRemotely: Boolean = false

    override fun getTitle(context: Context): String? = null

    override fun getNumberOfColumns(): Int = 1

    override val deviceName: String?
        get() = navigationViewModel.selectedDevice.value
}
