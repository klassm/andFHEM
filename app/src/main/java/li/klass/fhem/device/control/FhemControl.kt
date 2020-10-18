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
        val action: DeviceAction,
        val device: FhemDevice
) {
    fun toStatelessControl(pendingIntent: PendingIntent): Control =
            Control.StatelessBuilder(controlId.androidControlId, pendingIntent)
                    .setTitle(title)
                    .setStructure(structure)
                    .setDeviceType(action.deviceType)
                    .build()

    fun toStatefulControl(pendingIntent: PendingIntent, controlContext: ControlContext): Control =
            Control.StatefulBuilder(controlId.androidControlId, pendingIntent)
                    .setTitle(title)
                    .setStructure(structure)
                    .setZone(device.getRooms().firstOrNull())
                    .setDeviceType(action.deviceType)
                    .setControlTemplate(action.controlTemplateFor(device, controlContext))
                    .setStatus(Control.STATUS_OK)
                    .build()
}

@RequiresApi(Build.VERSION_CODES.R)
fun FhemDevice.toControl(controlContext: ControlContext, connection: FHEMServerSpec): FhemControl? {
    val action = DeviceAction.actionFor(this, controlContext) ?: return null
    return FhemControl(
            controlId = ControlId(connection.id, name),
            title = aliasOrName,
            structure = connection.name,
            action = action,
            device = this
    )
}