package li.klass.fhem.device.control

import android.os.Build
import android.service.controls.DeviceTypes
import android.service.controls.templates.*
import android.service.controls.templates.TemperatureControlTemplate.FLAG_MODE_HEAT
import android.service.controls.templates.TemperatureControlTemplate.MODE_HEAT
import androidx.annotation.RequiresApi
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.util.ValueExtractUtil

data class DeviceControl(
        val controlTemplate: ControlTemplate,
        val deviceType: Int
)

@RequiresApi(Build.VERSION_CODES.R)
enum class DeviceAction {

    TOGGLE_RANGE {

        override fun controlFor(device: FhemDevice, controlContext: ControlContext): DeviceControl? {
            if (!controlContext.onOffBehavior.supports(device) || !DimmableBehavior.supports(device)) {
                return null
            }

            val dimmable = DimmableBehavior.behaviorFor(device, null)!!
            val template = ToggleRangeTemplate(
                    "toggle_${device.name}", ControlButton(controlContext.onOffBehavior.isOn(device), "toggle"),
                    RangeTemplate("dim_${device.name}", dimmable.dimLowerBound.toFloat(), dimmable.dimUpperBound.toFloat(), dimmable.currentDimPosition.toFloat(), dimmable.dimStep.toFloat(), null)
            )

            return DeviceControl(template, DeviceTypes.TYPE_GENERIC_ON_OFF)
        }
    },

    TOGGLE {
        override fun controlFor(device: FhemDevice, controlContext: ControlContext): DeviceControl? {
            if (!controlContext.onOffBehavior.supports(device)) {
                return null
            }
            val template = ToggleTemplate("toggle_${device.name}", ControlButton(
                    controlContext.onOffBehavior.isOn(device),
                    "toggle"
            ))


            return DeviceControl(template, DeviceTypes.TYPE_GENERIC_ON_OFF)
        }
    },

    TEMPERATURE_CONTROL {
        override fun controlFor(device: FhemDevice, controlContext: ControlContext): DeviceControl? {
            val setListState = device.setList.getFirstPresentStateOf("desired-temp", "desiredTemp")
                    ?: return null
            val setListEntry = device.setList[setListState] as? SliderSetListEntry ?: return null

            val state = device.xmlListDevice.stateValueFor(setListState)
            val temperature = ValueExtractUtil.extractLeadingFloat(state)
            val template = TemperatureControlTemplate("temperature_${device.name}", RangeTemplate(
                    "temperature_${setListState}_${device.name}",
                    setListEntry.start.toFloat(),
                    setListEntry.stop.toFloat(),
                    temperature,
                    setListEntry.step.toFloat(),
                    null
            ), MODE_HEAT, MODE_HEAT, FLAG_MODE_HEAT)

            return DeviceControl(template, DeviceTypes.TYPE_HEATER)
        }
    };

    abstract fun controlFor(device: FhemDevice, controlContext: ControlContext): DeviceControl?

    companion object {
        fun controlFor(device: FhemDevice, controlContext: ControlContext): DeviceControl? =
                values().asSequence()
                        .map { it.controlFor(device, controlContext) }
                        .filterNotNull()
                        .firstOrNull()
    }
}