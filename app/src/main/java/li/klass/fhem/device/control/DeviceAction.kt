package li.klass.fhem.device.control

import android.os.Build
import android.service.controls.DeviceTypes
import android.service.controls.templates.*
import androidx.annotation.RequiresApi
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.domain.core.FhemDevice

@RequiresApi(Build.VERSION_CODES.R)
enum class DeviceAction {

    TOGGLE_RANGE {
        override fun supports(device: FhemDevice, controlContext: ControlContext): Boolean =
                controlContext.onOffBehavior.supports(device) && DimmableBehavior.supports(device)

        override fun controlTemplateFor(device: FhemDevice, controlContext: ControlContext): ControlTemplate {
            val dimmable = DimmableBehavior.behaviorFor(device, null)!!
            return ToggleRangeTemplate(
                    "toggle_${device.name}", ControlButton(controlContext.onOffBehavior.isOn(device), "toggle"),
                    RangeTemplate("dim_${device.name}", dimmable.dimLowerBound.toFloat(), dimmable.dimUpperBound.toFloat(), dimmable.currentDimPosition.toFloat(), dimmable.dimStep.toFloat(), null)
            )
        }

        override val deviceType: Int = DeviceTypes.TYPE_GENERIC_ON_OFF
    },

    TOGGLE {
        override fun supports(device: FhemDevice, controlContext: ControlContext): Boolean =
                controlContext.onOffBehavior.supports(device)

        override fun controlTemplateFor(device: FhemDevice, controlContext: ControlContext): ControlTemplate =
                ToggleTemplate("toggle_${device.name}", ControlButton(
                        controlContext.onOffBehavior.isOn(device),
                        "toggle"
                ))

        override val deviceType = DeviceTypes.TYPE_GENERIC_ON_OFF
    };

    abstract fun supports(device: FhemDevice, controlContext: ControlContext): Boolean
    abstract fun controlTemplateFor(device: FhemDevice, controlContext: ControlContext): ControlTemplate
    abstract val deviceType: Int

    companion object {
        fun actionFor(device: FhemDevice, controlContext: ControlContext): DeviceAction? =
                values().firstOrNull { it.supports(device, controlContext) }
    }
}