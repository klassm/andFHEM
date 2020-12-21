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

package li.klass.fhem.connection.backend

sealed class SaveData(val name: String,
                      val password: String?,
                      val serverType: ServerType) {

    open fun fillServer(server: FHEMServerSpec) {
        server.name = name
        server.password = password
    }

    class FhemWebSaveData(name: String,
                          val url: String,
                          val alternateUrl: String?,
                          val clientCertificatePath: String?,
                          val clientCertificatePassword: String?,
                          val username: String?,
                          val csrfToken: String?,
                          password: String?) : SaveData(name, password, ServerType.FHEMWEB) {

        override fun fillServer(server: FHEMServerSpec) {
            super.fillServer(server)
            server.url = url
            server.alternateUrl = alternateUrl
            server.clientCertificatePath = clientCertificatePath
            server.clientCertificatePassword = clientCertificatePassword
            server.csrfToken = csrfToken
            server.username = username
        }
    }

    class TelnetSaveData(name: String,
                         val ip: String,
                         val port: Int,
                         password: String?) : SaveData(name, password, ServerType.TELNET) {
        override fun fillServer(server: FHEMServerSpec) {
            super.fillServer(server)
            server.ip = ip
            server.port = port
        }
    }
}