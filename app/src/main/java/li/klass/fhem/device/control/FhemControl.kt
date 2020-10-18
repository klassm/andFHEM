package li.klass.fhem.device.control

import android.app.PendingIntent
import android.os.Build
import android.service.controls.Control
import androidx.annotation.RequiresApi
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.domain.core.FhemDevice

@RequiresApi(Build.VERSION_CODES.R)
data class FhemControl(
        val controlId: ControlId,
        val title: String,
        val structure: String?,
        val zone: String?,
        val deviceControl: DeviceControl,
        val device: FhemDevice
) {
    fun toStatelessControl(pendingIntent: PendingIntent): Control =
            Control.StatelessBuilder(controlId.androidControlId, pendingIntent)
                    .setTitle(title)
                    .setStructure(structure)
                    .setZone(zone)
                    .setDeviceType(deviceControl.deviceType)
                    .build()

    fun toStatefulControl(pendingIntent: PendingIntent, controlContext: ControlContext): Control =
            Control.StatefulBuilder(controlId.androidControlId, pendingIntent)
                    .setTitle(title)
                    .setStructure(structure)
                    .setZone(zone)
                    .setDeviceType(deviceControl.deviceType)
                    .setControlTemplate(deviceControl.controlTemplate)
                    .setStatus(Control.STATUS_OK)
                    .build()
}

@RequiresApi(Build.VERSION_CODES.R)
fun FhemDevice.toControl(controlContext: ControlContext, connection: FHEMServerSpec): FhemControl? {
    val deviceControl = DeviceAction.controlFor(this, controlContext) ?: return null
    return FhemControl(
            controlId = ControlId(connection.id, name),
            title = aliasOrName,
            structure = connection.name,
            zone = getRooms().firstOrNull(),
            deviceControl = deviceControl,
            device = this
    )
}