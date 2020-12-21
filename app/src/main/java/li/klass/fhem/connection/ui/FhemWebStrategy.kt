/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.connection.ui

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.connection_detail.view.*
import kotlinx.android.synthetic.main.connection_fhemweb.view.*
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.SaveData
import org.apache.commons.lang3.StringUtils.trimToNull

class FhemWebStrategy(context: Context) : ConnectionStrategy(context) {
    override fun saveDataFor(view: View): SaveData? {
        val name = trimToNull(view.connectionName.text.toString())
        val username = trimToNull(view.username.text.toString())
        val url = trimToNull(view.url.text.toString())
        val alternateUrl = trimToNull(view.alternate_url.text.toString())
        val clientCertificatePath = trimToNull(view.clientCertificatePath.text.toString())
        val clientCertificatePassword = trimToNull(view.clientCertificatePassword.text.toString())
        val password = trimToNull(view.password.text.toString())
        val csrfToken = trimToNull(view.csrfToken.text.toString())

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
                clientCertificatePath = clientCertificatePath,
                clientCertificatePassword = clientCertificatePassword,
                csrfToken = csrfToken
        )
    }

    override fun fillView(view: View, fhemServerSpec: FHEMServerSpec) {
        view.connectionName.setText(fhemServerSpec.name)
        view.url.setText(fhemServerSpec.url)
        view.alternate_url.setText(fhemServerSpec.alternateUrl)
        view.username.setText(fhemServerSpec.username)
        view.password.setText(fhemServerSpec.password)
        view.clientCertificatePassword.setText(fhemServerSpec.clientCertificatePassword)
        view.clientCertificatePath.text = fhemServerSpec.clientCertificatePath
        view.csrfToken.setText(fhemServerSpec.csrfToken)
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