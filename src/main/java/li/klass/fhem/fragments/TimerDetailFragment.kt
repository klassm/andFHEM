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

package li.klass.fhem.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import com.google.common.base.Joiner
import com.google.common.base.Optional
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.base.Splitter
import com.google.common.base.Strings
import com.google.common.collect.ImmutableList
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil.showSwitchOptionsMenu
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.OnTargetStateSelectedCallback
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.AtDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.FragmentType.DEVICE_SELECTION
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.fragments.device.DeviceNameListFragment
import li.klass.fhem.service.intent.DeviceIntentService
import li.klass.fhem.service.intent.RoomListIntentService
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.util.FhemResultReceiver
import li.klass.fhem.widget.TimePickerWithSeconds.getFormattedValue
import li.klass.fhem.widget.TimePickerWithSecondsDialog
import org.apache.commons.lang3.StringUtils.isBlank

class TimerDetailFragment : BaseFragment() {
    var timerDevice: AtDevice? = null

    @Transient private var targetDevice: FhemDevice? = null
    private var savedTimerDeviceName: String? = null

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun setArguments(args: Bundle) {
        if (args.containsKey(DEVICE_NAME)) {
            savedTimerDeviceName = args.getString(DEVICE_NAME)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            return view
        }
        view = inflater!!.inflate(R.layout.timer_detail, container, false)

        bindRepetitionSpinner(view)
        bindSelectDeviceButton(view)
        bindTimerTypeSpinner(view)
        bindSwitchTimeButton(view)
        bindIsActiveCheckbox(view)
        bindTargetStateButton(view)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (view == null) {
            return
        }

        val timerNameInput = getTimerNameInput(view)
        setTimerName("", view)
        if (isModify) {
            timerNameInput.isEnabled = false
        }
        setTargetDeviceName("", view)

        if (isModify && targetDevice == null && savedTimerDeviceName != null) {
            setTimerDeviceValuesForName(savedTimerDeviceName!!)
        }

        updateTargetDevice(targetDevice, view)
        updateTimerInformation(timerDevice)
        updateTargetStateRowVisibility(view)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.timer_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
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

    private fun bindTargetStateButton(view: View) {
        getTargetStateChangeButton(view).setOnClickListener { _ ->
            showSwitchOptionsMenu<FhemDevice>(activity, targetDevice, object : OnTargetStateSelectedCallback<FhemDevice> {
                override fun onStateSelected(device: FhemDevice, targetState: String) {
                    setTargetState(targetState, view)
                }

                override fun onSubStateSelected(device: FhemDevice, state: String, subState: String) {
                    onStateSelected(device, state + " " + subState)
                }

                override fun onNothingSelected(device: FhemDevice) {}
            })
        }
    }

    private fun bindSelectDeviceButton(view: View) {
        getTargetDeviceChangeButton(view).setOnClickListener {
            activity.sendBroadcast(Intent(SHOW_FRAGMENT)
                    .putExtra(FRAGMENT, DEVICE_SELECTION)
                    .putExtra(BundleExtraKeys.DEVICE_FILTER, DEVICE_FILTER)
                    .putExtra(CALLING_FRAGMENT, FragmentType.TIMER_DETAIL)
                    .putExtra(RESULT_RECEIVER, object : FhemResultReceiver() {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            if (resultCode != ResultCodes.SUCCESS) return

                            if (resultData == null || !resultData.containsKey(CLICKED_DEVICE)) return

                            updateTargetDevice(resultData.get(CLICKED_DEVICE) as FhemDevice, view)
                        }
                    }))
        }
    }

    private fun bindIsActiveCheckbox(view: View) {
        val isActiveCheckbox = getIsActiveCheckbox(view)
        if (!isModify) {
            isActiveCheckbox.isChecked = true
            isActiveCheckbox.isEnabled = false
        }
    }

    private fun bindSwitchTimeButton(view: View) {
        val switchTimeChangeButton = view.findViewById(R.id.switchTimeSet) as Button
        switchTimeChangeButton.setOnClickListener {
            val switchTime = getSwitchTime(view).or(SwitchTime(0, 0, 0))
            TimePickerWithSecondsDialog(activity, switchTime.hour, switchTime.minute, switchTime.second, TimePickerWithSecondsDialog.TimePickerWithSecondsListener { _, newHour, newMinute, newSecond, _ -> setSwitchTime(newHour, newMinute, newSecond, view) }).show()
        }
    }

    private fun bindTimerTypeSpinner(view: View) {
        val typeSpinner = getTypeSpinner(view)

        val timerTypeAdapter = ArrayAdapter<String>(activity, R.layout.spinnercontent)

        for (type in AtDevice.TimerType.values()) {
            timerTypeAdapter.add(view.context.getString(type.text))
        }
        typeSpinner.adapter = timerTypeAdapter
    }

    private fun bindRepetitionSpinner(view: View) {
        val repetitionSpinner = getRepetitionSpinner(view)
        val repetitionAdapter = ArrayAdapter<String>(activity, R.layout.spinnercontent)
        for (atRepetition in AtDevice.AtRepetition.values()) {
            repetitionAdapter.add(view.context.getString(atRepetition.text))
        }
        repetitionSpinner.adapter = repetitionAdapter
    }

    private fun save() {
        val view = view ?: return

        val switchTimeOptional = getSwitchTime(view)
        if (targetDevice == null || isBlank(getTargetState(view)) || !switchTimeOptional.isPresent) {
            activity.sendBroadcast(Intent(SHOW_TOAST)
                    .putExtra(STRING_ID, R.string.incompleteConfigurationError))
            return
        }

        val timerDeviceName = getTimerName(view)
        if (!isModify) {
            if (timerDeviceName.contains(" ")) {
                DialogUtil.showAlertDialog(activity, R.string.error, R.string.error_timer_name_spaces)
                return
            }
        }

        val action = if (isModify) DEVICE_TIMER_MODIFY else DEVICE_TIMER_NEW
        val switchTime = switchTimeOptional.get()

        activity.startService(Intent(action)
                .setClass(activity, DeviceIntentService::class.java)
                .putExtra(RESULT_RECEIVER, object : FhemResultReceiver() {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultData != null && resultCode == ResultCodes.SUCCESS) {
                            back()
                            savedTimerDeviceName = timerDeviceName
                        }
                    }
                })
                .putExtra(TIMER_TARGET_DEVICE_NAME, targetDevice!!.name)
                .putExtra(TIMER_TARGET_STATE, getTargetState(view))
                .putExtra(TIMER_HOUR, switchTime.hour)
                .putExtra(TIMER_MINUTE, switchTime.minute)
                .putExtra(TIMER_SECOND, switchTime.second)
                .putExtra(TIMER_REPETITION, getRepetition(view).name)
                .putExtra(TIMER_TYPE, getType(view).name)
                .putExtra(TIMER_IS_ACTIVE, getIsActive(view))
                .putExtra(DEVICE_NAME, timerDeviceName))
    }

    private fun updateTargetDevice(targetDevice: FhemDevice?, view: View?) {
        if (view == null || targetDevice == null) {
            return
        }
        this@TimerDetailFragment.targetDevice = targetDevice
        setTargetDeviceName(targetDevice.name, view)

        if (!updateTargetStateRowVisibility(view)) {
            setTargetState(getString(R.string.unknown), view)
        }
    }

    private fun updateTargetStateRowVisibility(view: View?): Boolean {
        if (view == null) return false

        val targetDeviceRow = view.findViewById(R.id.targetStateRow)
        if (targetDevice == null) {
            targetDeviceRow.visibility = View.GONE
            return false
        } else {
            targetDeviceRow.visibility = View.VISIBLE
            return true
        }
    }

    private fun setTimerDeviceValuesForName(timerDeviceName: String) {
        checkNotNull(timerDeviceName)

        activity.startService(Intent(GET_DEVICE_FOR_NAME)
                .setClass(activity, RoomListIntentService::class.java)
                .putExtra(DEVICE_NAME, timerDeviceName)
                .putExtra(RESULT_RECEIVER, object : FhemResultReceiver() {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultData == null || resultCode != ResultCodes.SUCCESS || !resultData.containsKey(DEVICE)) {
                            return
                        }
                        val device = resultData.getSerializable(DEVICE) as FhemDevice
                        if (device !is AtDevice) {
                            Log.e(TAG, "expected an AtDevice, but got " + device)
                            return
                        }

                        setValuesForCurrentTimerDevice(device)

                        val activity = activity
                        activity?.sendBroadcast(Intent(DISMISS_EXECUTING_DIALOG))
                    }
                }))
    }

    private fun setValuesForCurrentTimerDevice(atDevice: AtDevice) {
        this.timerDevice = atDevice

        activity.startService(Intent(GET_DEVICE_FOR_NAME)
                .setClass(activity, RoomListIntentService::class.java)
                .putExtra(DEVICE_NAME, atDevice.targetDevice)
                .putExtra(RESULT_RECEIVER, object : FhemResultReceiver() {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultData != null && resultCode == ResultCodes.SUCCESS && resultData.containsKey(DEVICE)) {
                            updateTargetDevice(resultData.get(DEVICE) as FhemDevice, this@TimerDetailFragment.view)
                        }
                    }
                }))

        updateTimerInformation(timerDevice)
    }

    private fun updateTimerInformation(timerDevice: AtDevice?) {
        val view = view
        if (view != null && timerDevice != null) {
            setType(timerDevice.timerType, view)
            setRepetition(timerDevice.repetition, view)
            setIsActive(timerDevice.isActive, view)
            setSwitchTime(timerDevice.hours, timerDevice.minutes, timerDevice.seconds, view)
            setTimerName(timerDevice.name, view)
            setTargetState(Joiner.on(" ").skipNulls().join(timerDevice.targetState, timerDevice.targetStateAddtionalInformation), view)
        }
    }

    private fun setTimerName(timerDeviceName: String, view: View) {
        getTimerNameInput(view).setText(timerDeviceName)
    }

    private fun getTimerName(view: View): String {
        return getTimerNameInput(view).text.toString()
    }

    private fun setTargetState(targetState: String, view: View) {
        getTargetStateTextView(view).setText(targetState)
    }

    private fun getTargetState(view: View): String {
        return getTargetStateTextView(view).text.toString()
    }

    private fun setSwitchTime(hour: Int, minute: Int, second: Int, view: View) {
        getSwitchTimeTextView(view).text = getFormattedValue(hour, minute, second)
    }

    private fun getSwitchTime(view: View): Optional<SwitchTime> {
        val text = getSwitchTimeTextView(view).text.toString()
        val parts = ImmutableList.copyOf(Splitter.on(":").split(text))
        if (parts.size != 3) {
            return Optional.absent<SwitchTime>()
        }
        return Optional.of(SwitchTime(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        ))
    }

    private fun setIsActive(isActive: Boolean, view: View) {
        getIsActiveCheckbox(view).isChecked = isActive
    }

    private fun getIsActive(view: View): Boolean {
        return getIsActiveCheckbox(view).isChecked
    }

    private fun setRepetition(repetition: AtDevice.AtRepetition, view: View) {
        getRepetitionSpinner(view).setSelection(repetition.ordinal)
    }

    private fun getRepetition(view: View): AtDevice.AtRepetition {
        return AtDevice.AtRepetition.values()[getRepetitionSpinner(view).selectedItemPosition]
    }

    private fun setType(type: AtDevice.TimerType, view: View) {
        getTypeSpinner(view).setSelection(type.ordinal)
    }

    private fun getType(view: View): AtDevice.TimerType {
        return AtDevice.TimerType.values()[getTypeSpinner(view).selectedItemPosition]
    }

    override fun update(refresh: Boolean) {}

    override fun getTitle(context: Context): CharSequence? {
        return context.getString(R.string.timer)
    }

    private fun getTimerNameInput(view: View): EditText {
        return view.findViewById(R.id.timerNameInput) as EditText
    }

    private fun getRepetitionSpinner(view: View): Spinner {
        return view.findViewById(R.id.timerRepetition) as Spinner
    }

    private fun getIsActiveCheckbox(view: View): CheckBox {
        return view.findViewById(R.id.isActive) as CheckBox
    }

    private fun getTypeSpinner(view: View): Spinner {
        return view.findViewById(R.id.timerType) as Spinner
    }

    private fun getSwitchTimeTextView(view: View): TextView {
        return view.findViewById(R.id.switchTimeContent) as TextView
    }

    private fun getTargetDeviceTextView(view: View): TextView {
        return view.findViewById(R.id.targetDeviceName) as TextView
    }

    private fun getTargetDeviceChangeButton(view: View): Button {
        return view.findViewById(R.id.targetDeviceSet) as Button
    }

    private fun getTargetStateTextView(view: View): EditText {
        return view.findViewById(R.id.targetState) as EditText
    }

    private fun getTargetStateChangeButton(view: View): Button {
        return view.findViewById(R.id.targetStateSet) as Button
    }

    private fun setTargetDeviceName(string: String, view: View) {
        getTargetDeviceTextView(view).text = string
    }

    private val isModify: Boolean
        get() = !Strings.isNullOrEmpty(savedTimerDeviceName)

    private class SwitchTime internal constructor(internal val hour: Int, internal val minute: Int, internal val second: Int)

    companion object {

        private val DEVICE_FILTER = object : DeviceNameListFragment.DeviceFilter {
            override fun isSelectable(device: FhemDevice): Boolean {
                return device.setList.size() > 0
            }
        }

        private val TAG = TimerDetailFragment::class.java.name
    }
}
