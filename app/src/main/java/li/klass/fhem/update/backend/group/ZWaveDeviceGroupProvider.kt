package li.klass.fhem.update.backend.group

import android.content.Context
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import javax.inject.Inject

class ZWaveDeviceGroupProvider @Inject constructor() : DeviceGroupProvider("ZWave") {
    override fun groupFor(xmlListDevice: XmlListDevice, context: Context): String? {
        val isThermostat = xmlListDevice.getState("model")?.contains("Thermostat") ?: false

        return when (isThermostat) {
            true -> DeviceFunctionality.HEATING
            false -> null
        }?.getCaptionText(context)
    }
}