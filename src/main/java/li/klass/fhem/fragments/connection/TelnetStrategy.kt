package li.klass.fhem.fragments.connection

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.connection_detail.view.*
import kotlinx.android.synthetic.main.connection_telnet.view.*
import li.klass.fhem.R
import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.service.connection.SaveData
import org.apache.commons.lang3.StringUtils.trimToNull

class TelnetStrategy(context: Context) : ConnectionStrategy(context) {
    override fun saveDataFor(view: View): SaveData? {
        val name = trimToNull(view.connectionName.text.toString())
        val ip = trimToNull(view.ip.text.toString())
        val port = trimToNull(view.port.text.toString())
        val password = trimToNull(view.password.text.toString())

        if (!enforceNotEmpty(R.string.connectionName, name)
                || !enforceNotEmpty(R.string.ip, ip)
                || !enforceNotEmpty(R.string.connectionPort, port)) {
            return null
        }
        return SaveData.TelnetSaveData(
                name = name,
                ip = ip,
                password = password,
                port = port.toInt()
        )
    }

    override fun fillView(view: View, fhemServerSpec: FHEMServerSpec) {
        view.connectionName.setText(fhemServerSpec.name)
        view.ip.setText(fhemServerSpec.ip)
        view.port.setText(fhemServerSpec.port)
        view.password.setText(fhemServerSpec.password)
    }
}