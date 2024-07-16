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
import android.view.*
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil.showSwitchOptionsMenu
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.OnTargetStateSelectedCallback
import li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG
import li.klass.fhem.constants.Actions.SHOW_TOAST
import li.klass.fhem.constants.BundleExtraKeys.STRING_ID
import li.klass.fhem.databinding.TimerDetailBinding
import li.klass.fhem.devices.backend.at.*
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.fragments.device.DeviceNameListFragment
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.util.getNavigationResult
import li.klass.fhem.widget.TimePickerWithSeconds.getFormattedValue
import li.klass.fhem.widget.TimePickerWithSecondsDialog
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isBlank
import org.joda.time.LocalTime
import javax.inject.Inject

class TimerDetailFragment @Inject constructor(
        private val deviceListService: DeviceListService,
        private val atService: AtService
) : BaseFragment() {
    private var timerDevice: TimerDevice? = null

    @Transient
    private var targetDevice: FhemDevice? = null

    private val args: TimerDetailFragmentArgs by navArgs()

    private lateinit var binding: TimerDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            binding = TimerDetailBinding.bind(view)
            return view
        }

        binding = TimerDetailBinding.inflate(inflater, container, false)
        val context = activity ?: return null

        bindRepetitionSpinner(context)
        bindSelectDeviceButton()
        bindTimerTypeSpinner(context)
        bindSwitchTimeButton()
        bindIsActiveCheckbox()
        bindTargetStateButton()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timerNameInput = binding.timerNameInput
        setTimerName("")
        if (isModify) {
            timerNameInput.isEnabled = false
        }
        binding.targetDeviceName.text = ""

        if (isModify && targetDevice == null && args.deviceName != null) {
            setTimerDeviceValuesForName(args.deviceName!!)
        }

        updateTargetDevice(targetDevice)
        updateTimerInformation(timerDevice)
        updateTargetStateRowVisibility()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.timer_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reset -> {
                timerDevice ?: return false
                setValuesForCurrentTimerDevice(timerDevice!!)
                return true
            }
            R.id.save -> {
                save()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun bindTargetStateButton() {
        binding.targetStateSet.setOnClickListener {
            onTargetStateClick()
        }
    }

    private fun onTargetStateClick() {
        val context = activity ?: return
        val device = targetDevice ?: return
        showSwitchOptionsMenu(context, device, object : OnTargetStateSelectedCallback {
            override suspend fun onStateSelected(device: FhemDevice, targetState: String) {
                setTargetState(targetState)
            }

            override suspend fun onSubStateSelected(
                device: FhemDevice,
                state: String,
                subState: String
            ) {
                coroutineScope {
                    onStateSelected(device, "$state $subState")
                }
            }

            override suspend fun onNothingSelected(device: FhemDevice) {}
        })
    }

    private fun bindSelectDeviceButton() {
        getNavigationResult<FhemDevice>()?.observe(viewLifecycleOwner, { device ->
            updateTargetDevice(device)
        })
        binding.targetDeviceSet.setOnClickListener {
            findNavController()
                .navigate(
                    TimerDetailFragmentDirections.actionTimerDetailFragmentToDeviceNameSelectionFragment(
                        DEVICE_FILTER, null
                    )
                )
        }
    }

    private fun bindIsActiveCheckbox() {
        val isActiveCheckbox = binding.isActive
        if (!isModify) {
            isActiveCheckbox.isChecked = true
            isActiveCheckbox.isEnabled = false
        }
    }

    private fun bindSwitchTimeButton() {
        binding.switchTimeSet.setOnClickListener {
            val switchTime = getSwitchTime() ?: LocalTime.now()
            activity?.let {
                TimePickerWithSecondsDialog(it,
                    switchTime.hourOfDay,
                    switchTime.minuteOfHour,
                    switchTime.secondOfMinute,
                    object : TimePickerWithSecondsDialog.TimePickerWithSecondsListener {
                        override fun onTimeChanged(
                            okClicked: Boolean,
                            hours: Int,
                            minutes: Int,
                            seconds: Int,
                            formattedText: String
                        ) {
                            setSwitchTime(LocalTime(hours, minutes, seconds))
                        }
                    }).show()
            }
        }
    }

    private fun bindTimerTypeSpinner(context: Context) {

        binding.timerType.adapter = ArrayAdapter<String>(context, R.layout.spinnercontent).apply {
            TimerType.values()
                .map { context.getString(it.text) }
                .forEach { this.add(it) }
        }
    }

    private fun bindRepetitionSpinner(context: Context) {
        val repetitionSpinner = binding.timerRepetition
        val repetitionAdapter = ArrayAdapter<String>(context, R.layout.spinnercontent)
        AtRepetition.values()
            .forEach { repetitionAdapter.add(context.getString(it.stringId)) }
        repetitionSpinner.adapter = repetitionAdapter
    }

    private fun save() {
        val safeContext = context ?: return

        val switchTime = getSwitchTime()
        val timerDeviceName = getTimerName()

        if (targetDevice == null || isBlank(getTargetState()) || switchTime == null || timerDeviceName == null) {
            activity?.sendBroadcast(
                Intent(SHOW_TOAST)
                    .putExtra(STRING_ID, R.string.incompleteConfigurationError).apply { setPackage(activity?.packageName) }
            )
            return
        }

        if (!isModify) {
            if (timerDeviceName.contains(" ")) {
                DialogUtil.showAlertDialog(
                    safeContext,
                    R.string.error,
                    R.string.error_timer_name_spaces
                )
                return
            }
        }

        val timerDevice = TimerDevice(
            name = timerDeviceName,
            isActive = binding.isActive.isChecked,
            definition = TimerDefinition(
                switchTime = switchTime,
                repetition = getRepetition(),
                type = getType(),
                targetDeviceName = targetDevice!!.name,
                targetState = getTargetState(),
                targetStateAppendix = "" // TODO never set?
            ),
            next = timerDevice?.next ?: ""
        )

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                if (isModify) {
                    atService.modify(timerDevice)
                } else {
                    atService.createNew(timerDevice)
                }
            }
            findNavController().popBackStack()
        }
    }

    private fun updateTargetDevice(targetDevice: FhemDevice?) {
        if (targetDevice == null) {
            return
        }
        this@TimerDetailFragment.targetDevice = targetDevice
        binding.targetDeviceName.text = targetDevice.name

        if (!updateTargetStateRowVisibility()) {
            setTargetState(getString(R.string.unknown))
        }
    }

    private fun updateTargetStateRowVisibility(): Boolean {
        return if (targetDevice == null) {
            binding.targetStateRow.visibility = View.GONE
            false
        } else {
            binding.targetStateRow.visibility = View.VISIBLE
            true
        }
    }

    private fun setTimerDeviceValuesForName(timerDeviceName: String) {
        checkNotNull(timerDeviceName)
        val myActivity = activity ?: return

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                atService.getTimerDeviceFor(timerDeviceName)
            }?.let {
                setValuesForCurrentTimerDevice(it)
                myActivity.sendBroadcast(Intent(DISMISS_EXECUTING_DIALOG).apply { setPackage(activity?.packageName) })
            }
        }
    }

    private fun setValuesForCurrentTimerDevice(device: TimerDevice) {
        this.timerDevice = device
        updateTimerInformation(timerDevice)

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                deviceListService.getDeviceForName(device.definition.targetDeviceName)
            }?.let {
                updateTargetDevice(it)
            }
        }
    }

    private fun updateTimerInformation(timerDevice: TimerDevice?) {
        timerDevice ?: return
        val definition = timerDevice.definition
        setType(definition.type)
        setRepetition(definition.repetition)
        binding.isActive.isChecked = timerDevice.isActive
        setSwitchTime(definition.switchTime)
        setTimerName(timerDevice.name)
        setTargetState(
            listOfNotNull(
                definition.targetState,
                definition.targetStateAppendix
            ).joinToString(" ")
        )
    }

    private fun setTimerName(timerDeviceName: String) {
        binding.timerNameInput.setText(timerDeviceName)
    }

    private fun getTimerName(): String? =
        StringUtils.trimToNull(binding.timerNameInput.text.toString())

    private fun setTargetState(targetState: String) {
        binding.targetState.setText(targetState)
    }

    private fun getTargetState(): String = binding.targetState.text.toString()

    private fun setSwitchTime(switchTime: LocalTime) {
        binding.switchTimeContent.text = getFormattedValue(
            switchTime.hourOfDay,
            switchTime.minuteOfHour,
            switchTime.secondOfMinute
        )
    }

    private fun getSwitchTime(): LocalTime? {
        val text = binding.switchTimeContent.text.toString()
        val parts = text.split(":").toList()
        if (parts.size != 3) {
            return null
        }
        return LocalTime(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        )
    }

    private fun setRepetition(repetition: AtRepetition) {
        binding.timerRepetition.setSelection(repetition.ordinal)
    }

    private fun getRepetition(): AtRepetition =
        AtRepetition.values()[binding.timerRepetition.selectedItemPosition]

    private fun setType(type: TimerType) {
        binding.timerType.setSelection(type.ordinal)
    }

    private fun getType(): TimerType =
        TimerType.values()[binding.timerType.selectedItemPosition]

    override suspend fun update(refresh: Boolean) {}

    override fun getTitle(context: Context) = context.getString(R.string.timer)

    private val isModify: Boolean
        get() = args.deviceName?.isNotEmpty() ?: false

    companion object {
        private val DEVICE_FILTER = object : DeviceNameListFragment.DeviceFilter {
            override fun isSelectable(device: FhemDevice): Boolean = device.setList.size() > 0
        }
    }
}
