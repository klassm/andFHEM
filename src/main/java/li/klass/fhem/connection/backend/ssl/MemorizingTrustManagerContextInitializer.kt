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

package li.klass.fhem.connection.backend.ssl

import android.content.Context
import com.google.common.base.MoreObjects
import de.duenndns.ssl.MemorizingTrustManager
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.util.CloseableUtil
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.*

class MemorizingTrustManagerContextInitializer {
    fun init(context: Context, serverSpec: FHEMServerSpec? = null): Initialized? {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            var clientKeys: Array<KeyManager>? = null
            if (serverSpec?.clientCertificatePath != null) {
                val clientCertificate = File(serverSpec.clientCertificatePath)
                val clientCertificatePassword = serverSpec.clientCertificatePassword
                if (clientCertificate.exists()) {
                    val keyStore = loadPKCS12KeyStore(clientCertificate, clientCertificatePassword)
                    val keyManagerFactory = KeyManagerFactory.getInstance("X509")
                    keyManagerFactory.init(keyStore, MoreObjects.firstNonNull(clientCertificatePassword, "").toCharArray())
                    clientKeys = keyManagerFactory.keyManagers
                }
            }

            val memorizingTrustManager = MemorizingTrustManager(context)
            val hostnameVerifier = memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier())
            sslContext.init(clientKeys, arrayOf<X509TrustManager>(memorizingTrustManager), java.security.SecureRandom())
            return Initialized(sslContext.socketFactory, hostnameVerifier, memorizingTrustManager)
        } catch (e: Exception) {
            logger.error("init", e)
            return null
        }
    }


    @Throws(Exception::class)
    private fun loadPKCS12KeyStore(certificateFile: File, clientCertPassword: String?): KeyStore? {
        val keyStore: KeyStore?
        var fileInputStream: FileInputStream? = null
        try {
            keyStore = KeyStore.getInstance("PKCS12")
            fileInputStream = FileInputStream(certificateFile)
            keyStore!!.load(fileInputStream, clientCertPassword!!.toCharArray())
        } finally {
            CloseableUtil.close(fileInputStream)
        }
        return keyStore
    }

    data class Initialized(val socketFactory: SSLSocketFactory, val hostnameVerifier: HostnameVerifier, val trustManager: X509TrustManager)

    companion object {
        private val logger = LoggerFactory.getLogger(MemorizingTrustManagerContextInitializer::class.java)
    }
}