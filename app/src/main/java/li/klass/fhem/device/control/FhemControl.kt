package li.klass.fhem.device.control

import android.app.PendingIntent
import android.os.Build
import android.service.controls.Control
import androidx.annotation.RequiresApi
import li.klass.fhem.domain.core.FhemDevice

@RequiresApi(Build.VERSION_CODES.R)
data class FhemControl(
        val controlId: String,
        val title: String,
        val structure: String?,
        val action: DeviceAction,
        val device: FhemDevice
) {
    fun toStatelessControl(pendingIntent: PendingIntent): Control =
            Control.StatelessBuilder(controlId, pendingIntent)
                    .setTitle(title)
                    .setStructure(structure)
                    .setDeviceType(action.deviceType)
                    .build()

    fun toStatefulControl(pendingIntent: PendingIntent, controlContext: ControlContext): Control =
            Control.StatefulBuilder(controlId, pendingIntent)
                    .setTitle(title)
                    .setSubtitle(device.roomConcatenated)
                    .setStructure(structure)
                    .setDeviceType(action.deviceType)
                    .setControlTemplate(action.controlTemplateFor(device, controlContext))
                    .setStatus(Control.STATUS_OK)
                    .build()
}

@RequiresApi(Build.VERSION_CODES.R)
fun FhemDevice.toControl(controlContext: ControlContext): FhemControl? {
    val action = DeviceAction.actionFor(this, controlContext) ?: return null
    return FhemControl(
            controlId = name,
            title = aliasOrName,
            structure = getRooms().firstOrNull(),
            action = action,
            device = this
    )
}