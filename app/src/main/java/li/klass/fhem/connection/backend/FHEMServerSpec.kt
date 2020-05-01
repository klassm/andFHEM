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

import com.google.common.base.Strings.isNullOrEmpty
import java.io.Serializable

open class FHEMServerSpec(val id: String, val serverType: ServerType, var name: String) : Comparable<FHEMServerSpec>, Serializable {
    var password: String? = null
    var ip: String? = null
    var port: Int = 0
    var url: String? = null
    var alternateUrl: String? = null
    var username: String? = null
    var clientCertificatePath: String? = null
    var clientCertificatePassword: String? = null
    var csrfToken: String? = null

    override fun compareTo(other: FHEMServerSpec): Int = name.compareTo(other.name)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as FHEMServerSpec?

        if (port != that!!.port) return false
        if (if (alternateUrl != null) alternateUrl != that.alternateUrl else that.alternateUrl != null)
            return false
        if (if (clientCertificatePassword != null) clientCertificatePassword != that.clientCertificatePassword else that.clientCertificatePassword != null)
            return false
        if (if (clientCertificatePath != null) clientCertificatePath != that.clientCertificatePath else that.clientCertificatePath != null)
            return false
        if (if (ip != null) ip != that.ip else that.ip != null) return false
        if (name != that.name) return false
        if (if (password != null) password != that.password else that.password != null)
            return false
        if (serverType != that.serverType) return false
        if (if (url != null) url != that.url else that.url != null) return false
        if (if (csrfToken != null) csrfToken != that.csrfToken else that.csrfToken != null) return false
        return !if (username != null) username != that.username else that.username != null

    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + if (password != null) password!!.hashCode() else 0
        result = 31 * result + if (ip != null) ip!!.hashCode() else 0
        result = 31 * result + port
        result = 31 * result + if (url != null) url!!.hashCode() else 0
        result = 31 * result + if (alternateUrl != null) alternateUrl!!.hashCode() else 0
        result = 31 * result + if (username != null) username!!.hashCode() else 0
        result = 31 * result + if (clientCertificatePath != null) clientCertificatePath!!.hashCode() else 0
        result = 31 * result + if (clientCertificatePassword != null) clientCertificatePassword!!.hashCode() else 0
        result = 31 * result + if (csrfToken != null) csrfToken!!.hashCode() else 0
        result = 31 * result + serverType.hashCode()
        return result
    }

    override fun toString(): String {
        return "FHEMServerSpec{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", password='" + (if (isNullOrEmpty(password)) "empty" else "*****") + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", url='" + url + '\'' +
                ", alternateUrl='" + alternateUrl + '\'' +
                ", username='" + username + '\'' +
                ", clientCertificatePath='" + clientCertificatePath + '\'' +
                ", clientCertificatePassword='" + clientCertificatePassword + '\'' +
                ", serverType=" + serverType +
                ", csrfToken=" + csrfToken +
                '}'
    }

    fun canRetry(): Boolean = !isNullOrEmpty(alternateUrl)
}
