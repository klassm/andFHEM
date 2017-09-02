package li.klass.fhem.service.connection

import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.fhem.connection.ServerType

sealed class SaveData(val name: String,
                      val password: String?) {

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
                          password: String?) : SaveData(name, password) {

        override fun fillServer(server: FHEMServerSpec) {
            super.fillServer(server)
            server.url = url
            server.alternateUrl = alternateUrl
            server.clientCertificatePath = clientCertificatePath
            server.clientCertificatePassword = clientCertificatePassword
            server.username = username
            server.serverType = ServerType.FHEMWEB
        }
    }

    class TelnetSaveData(name: String,
                         val ip: String,
                         val port: Int,
                         password: String?) : SaveData(name, password) {
        override fun fillServer(server: FHEMServerSpec) {
            super.fillServer(server)
            server.ip = ip
            server.port = port
            server.serverType = ServerType.TELNET
        }
    }
}