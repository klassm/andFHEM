package li.klass.fhem.settings.type

import li.klass.fhem.R
import javax.inject.Inject

class CloudMessagingTypeHandler @Inject constructor() : SettingsTypeHandler("cloud_messaging") {
    override fun getResource(): Int = R.xml.preferences_cloud_messaging
}