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

package li.klass.fhem.fragments.weekprofile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.common.base.Supplier
import com.google.common.collect.Lists.newArrayList
import kotlinx.android.synthetic.main.weekprofile.*
import kotlinx.android.synthetic.main.weekprofile.view.*
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.adapter.weekprofile.BaseWeekProfileAdapter
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.devices.ui.DeviceNameSelectionAlert
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.DialogUtil
import org.slf4j.LoggerFactory
import java.io.Serializable
import javax.inject.Inject

interface HeatingConfigurationProvider<INTERVAL : BaseHeatingInterval<INTERVAL>> :
        Supplier<HeatingConfiguration<INTERVAL, *>>, Serializable

abstract class BaseWeekProfileFragment<INTERVAL : BaseHeatingInterval<INTERVAL>> : BaseFragment() {

    abstract val deviceName: String
    abstract val deviceDisplayName: String
    abstract val heatingConfigurationProvider: HeatingConfigurationProvider<INTERVAL>
    private lateinit var weekProfile: WeekProfile<INTERVAL, *>
    private lateinit var heatingConfiguration: HeatingConfiguration<INTERVAL, *>

    @Inject
    lateinit var deviceListService: DeviceListService
    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService
    @Inject
    lateinit var genericDeviceService: GenericDeviceService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView

        beforeCreateView()
        heatingConfiguration = heatingConfigurationProvider.get()
        return inflater.inflate(R.layout.weekprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateAsync(false)

        getAdapter().registerWeekProfileChangedListener(object :
                                                                BaseWeekProfileAdapter.WeekProfileChangedListener {
            override fun onWeekProfileChanged(weekProfile: WeekProfile<*, *>) {
                LOGGER.info("onWeekProfileChanged() - {}", weekProfile.toString())
                @Suppress("UNCHECKED_CAST") updateChangeButtonsHolderVisibility(
                        weekProfile as WeekProfile<INTERVAL, *>)
            }
        })

        change_value_button_holder.save_weekprofile_button.setOnClickListener { onSave() }
        change_value_button_holder.reset_weekprofile_button.setOnClickListener { onReset() }
        copy_from_device.setOnClickListener { onCopyFromDevice() }
        weekprofile.adapter = getAdapter()
    }

    private fun onSave() {
        val commands = newArrayList(weekProfile.statesToSet)
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                deviceListService.getDeviceForName(deviceName)?.xmlListDevice?.let {
                    genericDeviceService.setSubStates(it, commands, connectionId = null)
                }
            }
            backToDevice()
        }
    }

    private fun onReset() {
        updateAsync(false)
    }

    private fun onCopyFromDevice() {
        val givenDevice = deviceListService.getDeviceForName(deviceName) ?: return
        val devices = deviceListService.getAllRoomsDeviceList().allDevices.filter {
            it.xmlListDevice.type == givenDevice.xmlListDevice.type && it.xmlListDevice.getAttribute(
                    "model") == givenDevice.xmlListDevice.getAttribute("model")
        }
        val saveContext = context ?: return
        DeviceNameSelectionAlert.show(saveContext, devices) {
            if (it != null) {
                onCopyFromDevice(it)
            }
        }
    }

    private fun onCopyFromDevice(from: FhemDevice) {
        val fromProfile = heatingConfigurationProvider.get().fillWith(from.xmlListDevice)
        weekProfile.replaceDayProfilesWithDataFrom(fromProfile)
        getAdapter().apply {
            updateParentPositions()
            notifyDataSetInvalidated()
            notifyWeekProfileChangedListener()
        }
    }

    private fun backToDevice() {
        val context = activity ?: return
        DialogUtil.showAlertDialog(context, R.string.doneTitle, R.string.switchDelayNotification,
                                   Runnable {
                                       findNavController().popBackStack()
                                   })
    }

    override suspend fun update(refresh: Boolean) {
        activity ?: return
        coroutineScope {
            withContext(Dispatchers.IO) {
                if (refresh) {
                    deviceListUpdateService.updateSingleDevice(deviceName)
                }
                deviceListService.getDeviceForName(deviceName)
            }?.let {
                weekProfile = heatingConfiguration.fillWith(it.xmlListDevice)
                updateChangeButtonsHolderVisibility(weekProfile)
            }
        }
    }

    private fun updateChangeButtonsHolderVisibility(weekProfile: WeekProfile<INTERVAL, *>) {
        updateAdapterWith(weekProfile)

        change_value_button_holder.visibility = when {
            weekProfile.changedDayProfiles.isEmpty() -> View.GONE
            else                                     -> View.VISIBLE
        }
    }

    protected abstract fun updateAdapterWith(weekProfile: WeekProfile<INTERVAL, *>)

    protected abstract fun getAdapter(): BaseWeekProfileAdapter<*>

    protected open fun beforeCreateView() {}

    override fun mayPullToRefresh(): Boolean = false

    override fun mayUpdateFromBroadcast(): Boolean = false

    override fun getTitle(context: Context) = deviceDisplayName

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BaseWeekProfileFragment::class.java)
    }
}
