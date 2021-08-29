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
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.SaveData
import li.klass.fhem.databinding.ConnectionDetailBinding
import li.klass.fhem.databinding.ConnectionFhemwebBinding
import org.apache.commons.lang3.StringUtils.trimToNull

class FhemWebStrategy(context: Context) : ConnectionStrategy(context) {
    override fun saveDataFor(view: View): SaveData? {
        val fhemwebBinding = ConnectionFhemwebBinding.bind(view)
        val detailBinding = ConnectionDetailBinding.bind(view)
        val name = trimToNull(detailBinding.connectionName.text.toString())
        val username = trimToNull(fhemwebBinding.username.text.toString())
        val url = trimToNull(fhemwebBinding.url.text.toString())
        val alternateUrl = trimToNull(fhemwebBinding.alternateUrl.text.toString())
        val clientCertificatePath = trimToNull(fhemwebBinding.clientCertificatePath.text.toString())
        val clientCertificatePassword =
            trimToNull(fhemwebBinding.clientCertificatePassword.text.toString())
        val password = trimToNull(fhemwebBinding.password.text.toString())
        val csrfToken = trimToNull(fhemwebBinding.csrfToken.text.toString())

        if (!enforceNotEmpty(R.string.connectionName, name)
            || !enforceUrlStartsWithHttp(url)
        ) {
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
        val fhemwebBinding = ConnectionFhemwebBinding.bind(view)
        val detailBinding = ConnectionDetailBinding.bind(view)
        detailBinding.connectionName.setText(fhemServerSpec.name)
        fhemwebBinding.url.setText(fhemServerSpec.url)
        fhemwebBinding.alternateUrl.setText(fhemServerSpec.alternateUrl)
        fhemwebBinding.username.setText(fhemServerSpec.username)
        fhemwebBinding.password.setText(fhemServerSpec.password)
        fhemwebBinding.clientCertificatePassword.setText(fhemServerSpec.clientCertificatePassword)
        fhemwebBinding.clientCertificatePath.text = fhemServerSpec.clientCertificatePath
        fhemwebBinding.csrfToken.setText(fhemServerSpec.csrfToken)
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