package li.klass.fhem.device.control

class ControlId(val connectionId: String, val deviceName: String) {

    val androidControlId = "${connectionId}${delimiter}${deviceName}"

    companion object {
        private const val delimiter = "::::"

        fun fromAndroid(controlId: String): ControlId {
            val parts = controlId.split(delimiter)
            return ControlId(parts[0], parts[1])
        }
    }
}