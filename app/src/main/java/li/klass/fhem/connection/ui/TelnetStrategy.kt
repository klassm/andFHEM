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
import kotlinx.android.synthetic.main.connection_telnet.view.*
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.SaveData
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
        view.port.setText(fhemServerSpec.port.toString())
        view.password.setText(fhemServerSpec.password)
    }
}