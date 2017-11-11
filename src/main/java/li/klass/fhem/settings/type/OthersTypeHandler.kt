package li.klass.fhem.settings.type

import li.klass.fhem.R
import javax.inject.Inject

class OthersTypeHandler @Inject constructor()
    : SettingsTypeHandler("others") {

    override fun getResource(): Int = R.xml.settings_others
}