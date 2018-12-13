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
import com.google.common.base.Preconditions.checkArgument
import com.google.common.collect.Lists.newArrayList
import kotlinx.android.synthetic.main.weekprofile.*
import kotlinx.android.synthetic.main.weekprofile.view.*
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.adapter.weekprofile.BaseWeekProfileAdapter
import li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME
import li.klass.fhem.constants.BundleExtraKeys.HEATING_CONFIGURATION
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.DialogUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject

abstract class BaseWeekProfileFragment<INTERVAL : BaseHeatingInterval<INTERVAL>> : BaseFragment() {

    private lateinit var deviceName: String
    private lateinit var heatingConfiguration: HeatingConfiguration<INTERVAL, *>
    private lateinit var weekProfile: WeekProfile<INTERVAL, *>

    @Inject
    lateinit var deviceListService: DeviceListService
    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService
    @Inject
    lateinit var genericDeviceService: GenericDeviceService

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        args ?: return

        checkArgument(args.containsKey(DEVICE_NAME))
        checkArgument(args.containsKey(HEATING_CONFIGURATION))

        deviceName = args.getString(DEVICE_NAME)
        @Suppress("UNCHECKED_CAST")
        heatingConfiguration = args.getSerializable(HEATING_CONFIGURATION)!! as HeatingConfiguration<INTERVAL, *>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView

        beforeCreateView()
        return inflater.inflate(R.layout.weekprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateAsync(false)

        getAdapter().registerWeekProfileChangedListener(object : BaseWeekProfileAdapter.WeekProfileChangedListener {
            override fun onWeekProfileChanged(weekProfile: WeekProfile<*, *>) {
                LOGGER.info("onWeekProfileChanged() - {}", weekProfile.toString())
                @Suppress("UNCHECKED_CAST")
                updateChangeButtonsHolderVisibility(weekProfile as WeekProfile<INTERVAL, *>)
            }
        })

        change_value_button_holder.save_weekprofile_button.setOnClickListener { onSave() }
        change_value_button_holder.reset_weekprofile_button.setOnClickListener { onReset() }
        weekprofile.adapter = getAdapter()
    }

    private fun onSave() {
        val commands = newArrayList(weekProfile.getStatesToSet())
        GlobalScope.launch(Dispatchers.Main) {
            async(Dispatchers.IO) {
                deviceListService.getDeviceForName(deviceName)
                        ?.xmlListDevice?.let {
                    genericDeviceService.setSubStates(it, commands, connectionId = null)
                }
            }.await()
            backToDevice()
        }
    }

    private fun onReset() {
        updateAsync(false)
    }

    private fun backToDevice() {
        val context = activity ?: return
        DialogUtil.showAlertDialog(context, R.string.doneTitle, R.string.switchDelayNotification, Runnable { back() })
    }

    override suspend fun update(refresh: Boolean) {
        activity ?: return
        coroutineScope {
            async(Dispatchers.IO) {
                if (refresh) {
                    deviceListUpdateService.updateSingleDevice(deviceName)
                }
                deviceListService.getDeviceForName(deviceName)
            }.await()?.let {
                weekProfile = heatingConfiguration.fillWith(it.xmlListDevice)
                updateChangeButtonsHolderVisibility(weekProfile)
            }
        }
    }

    private fun updateChangeButtonsHolderVisibility(weekProfile: WeekProfile<INTERVAL, *>) {
        updateAdapterWith(weekProfile)

        change_value_button_holder.visibility = when {
            weekProfile.getChangedDayProfiles().isEmpty() -> View.GONE
            else -> View.VISIBLE
        }
    }

    protected abstract fun updateAdapterWith(weekProfile: WeekProfile<INTERVAL, *>)

    protected abstract fun getAdapter(): BaseWeekProfileAdapter<*>

    protected open fun beforeCreateView() {}

    override fun mayPullToRefresh(): Boolean = false

    override fun mayUpdateFromBroadcast(): Boolean = false

    override fun getTitle(context: Context) = deviceName

    companion object {

        private val LOGGER = LoggerFactory.getLogger(BaseWeekProfileFragment::class.java)
    }
}
