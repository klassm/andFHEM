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
import li.klass.fhem.databinding.ConnectionTelnetBinding
import org.apache.commons.lang3.StringUtils.trimToNull

class TelnetStrategy(context: Context) : ConnectionStrategy(context) {
    override fun saveDataFor(view: View): SaveData? {
        val detailBinding = view.connectionDetailBinding
        val telnetBinding = view.telnetBinding
        val name = trimToNull(detailBinding.connectionName.text.toString())
        val ip = trimToNull(telnetBinding.ip.text.toString())
        val port = trimToNull(telnetBinding.port.text.toString())
        val password = trimToNull(telnetBinding.password.text.toString())

        if (!enforceNotEmpty(R.string.connectionName, name)
            || !enforceNotEmpty(R.string.ip, ip)
            || !enforceNotEmpty(R.string.connectionPort, port)
        ) {
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
        val detailBinding = view.connectionDetailBinding
        val telnetBinding = view.telnetBinding
        detailBinding.connectionName.setText(fhemServerSpec.name)
        telnetBinding.ip.setText(fhemServerSpec.ip)
        telnetBinding.port.setText(fhemServerSpec.port.toString())
        telnetBinding.password.setText(fhemServerSpec.password)
    }


    private val View.telnetBinding
        get() = ConnectionTelnetBinding.bind(connectionContentView)
}