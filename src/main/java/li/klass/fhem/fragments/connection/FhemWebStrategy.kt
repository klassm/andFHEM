package li.klass.fhem.fragments.connection

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.connection_detail.view.*
import kotlinx.android.synthetic.main.connection_fhemweb.view.*
import li.klass.fhem.R
import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.service.connection.SaveData
import org.apache.commons.lang3.StringUtils.trimToNull

class FhemWebStrategy(context: Context) : ConnectionStrategy(context) {
    override fun saveDataFor(view: View): SaveData? {
        val name = trimToNull(view.connectionName.text.toString())
        val username = trimToNull(view.username.text.toString())
        val url = trimToNull(view.url.text.toString())
        val alternateUrl = trimToNull(view.alternate_url.text.toString())
        val clientCertificatePassword = trimToNull(view.clientCertificatePassword.text.toString())
        val clientCertificatePath = trimToNull(view.clientCertificatePath.text.toString())
        val password = trimToNull(view.password.text.toString())

        if (!enforceNotEmpty(R.string.connectionName, name)
                || !enforceUrlStartsWithHttp(url)) {
            return null
        }
        return SaveData.FhemWebSaveData(
                name = name,
                username = username,
                password = password,
                url = url,
                alternateUrl = alternateUrl,
                clientCertificatePassword = clientCertificatePassword,
                clientCertificatePath = clientCertificatePath
        )
    }

    override fun fillView(view: View, fhemServerSpec: FHEMServerSpec) {
        view.connectionName.setText(fhemServerSpec.name)
        view.url.setText(fhemServerSpec.url)
        view.alternate_url.setText(fhemServerSpec.alternateUrl)
        view.username.setText(fhemServerSpec.username)
        view.password.setText(fhemServerSpec.password)
        view.clientCertificatePath.setText(fhemServerSpec.clientCertificatePath)
        view.clientCertificatePassword.setText(fhemServerSpec.clientCertificatePassword)
    }

    private fun enforceUrlStartsWithHttp(url: String?): Boolean {
        val field = R.string.connectionURL
        if (!enforceNotEmpty(field, url)) {
            return false
        }
        if ((url ?: "").startsWith("http")) {
            return true
        }
        showError(context.getString(R.string.connectionUrlHttp))
        return false
    }
}