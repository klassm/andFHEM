package li.klass.fhem.adapter.devices

import android.content.Context

import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil
import li.klass.fhem.adapter.devices.genericui.ButtonActionRow
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.StateChangingTargetStateSelectedCallback
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice

class StateChangeButtonActionRow(
        private val context: Context,
        private val device: FhemDevice,
        layout: Int,
        private val connectionId: String?,
        private val stateUiService: StateUiService
) : ButtonActionRow(context, R.string.set, layout) {

    override fun onButtonClick() {
        AvailableTargetStatesDialogUtil.showSwitchOptionsMenuFor(
                context, device, StateChangingTargetStateSelectedCallback(context, stateUiService, connectionId))
    }
}
