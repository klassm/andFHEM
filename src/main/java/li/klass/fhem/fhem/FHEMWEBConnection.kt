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

package li.klass.fhem.fhem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.TrafficStats
import com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpResponseException
import com.google.common.base.Charsets
import com.google.common.base.MoreObjects.firstNonNull
import com.google.common.base.Optional
import com.google.common.collect.ImmutableMap
import com.google.common.io.CharStreams
import de.duenndns.ssl.MemorizingTrustManager
import li.klass.fhem.error.ErrorHolder
import li.klass.fhem.fhem.RequestResultError.*
import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.CloseableUtil.close
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URLEncoder
import java.security.KeyStore
import javax.net.ssl.*

class FHEMWEBConnection(fhemServerSpec: FHEMServerSpec, applicationProperties: ApplicationProperties) : FHEMConnection(fhemServerSpec, applicationProperties) {

    val basicAuthHeaders: HttpHeaders
        get() {
            val password = Optional.fromNullable(serverSpec.password).or("")
            val username = Optional.fromNullable(serverSpec.username).or("")
            return HttpHeaders().setBasicAuthentication(username, password)
        }

    val password: String
        get() = serverSpec.password

    override fun executeCommand(command: String, context: Context): RequestResult<String> {
        LOG.info("executeTask command " + command)

        val urlSuffix = generateUrlSuffix(command)

        var reader: InputStreamReader? = null
        try {
            val response = executeRequest(urlSuffix, context)
            if (response.error != null) {
                return RequestResult(response.error)
            }

            reader = InputStreamReader(response.content, Charsets.UTF_8)
            val content = CharStreams.toString(reader)
            if (content.contains("<title>") || content.contains("<div id=")) {
                LOG.error("found strange content: " + content)
                ErrorHolder.setError("found strange content in URL $urlSuffix: \r\n\r\n$content")
                return RequestResult(INVALID_CONTENT)
            }

            return RequestResult(content)
        } catch (e: Exception) {
            LOG.error("cannot handle result", e)
            return RequestResult(INTERNAL_ERROR)
        } finally {
            close(reader)
        }
    }

    protected fun generateUrlSuffix(command: String): String? {
        var urlSuffix: String? = null
        try {
            urlSuffix = "?XHR=1&cmd=" + URLEncoder.encode(command, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            LOG.error("unsupported encoding", e)
        }

        return urlSuffix
    }

    fun executeRequest(urlSuffix: String?, context: Context): RequestResult<InputStream> =
            executeRequest(serverSpec.url, urlSuffix, false, context)

    private fun executeRequest(serverUrl: String, urlSuffix: String?, isRetry: Boolean, context: Context): RequestResult<InputStream> {
        var url: String? = null
        TrafficStats.setThreadStatsTag(1)
        try {
            initSslContext(context)

            url = serverUrl + if (urlSuffix?.contains("cmd=") ?: false) {
                val csrfToken = findCsrfToken(serverUrl) ?: ""
                urlSuffix + "&fwcsrf=" + csrfToken
            } else urlSuffix
            LOG.info("accessing URL {}", url)

            val response = newCompatibleTransport()
                    .createRequestFactory().buildGetRequest(GenericUrl(url))
                    .setConnectTimeout(SOCKET_TIMEOUT)
                    .setReadTimeout(SOCKET_TIMEOUT)
                    .setLoggingEnabled(false)
                    .setHeaders(basicAuthHeaders)
                    .execute()

            LOG.debug("response status code is " + response.statusCode)


            return RequestResult(BufferedInputStream(response.content) as InputStream)
        } catch (e: HttpResponseException) {
            val errorResult = handleHttpStatusCode(e.statusCode)
            val msg = "found error " + errorResult!!.error.declaringClass.getSimpleName() + " for " +
                    "status code " + e.statusCode
            LOG.debug(msg)
            ErrorHolder.setError(null, msg)
            return errorResult

        } catch (e: Exception) {
            LOG.info("error while loading data", e)
            return handleError(urlSuffix ?: "", isRetry, url, e, context)
        }

    }

    private fun findCsrfToken(serverUrl: String): String? {
        try {
            val response = newCompatibleTransport()
                    .createRequestFactory().buildGetRequest(GenericUrl(serverUrl + "?room=notExistingJustToLoadCsrfToken"))
                    .setConnectTimeout(SOCKET_TIMEOUT)
                    .setReadTimeout(SOCKET_TIMEOUT)
                    .setLoggingEnabled(false)
                    .setHeaders(basicAuthHeaders)
                    .execute()

            val value = response.headers.getFirstHeaderStringValue("X-FHEM-csrfToken")

            response.content.close()
            return value
        } catch (e: IOException) {
            LOG.info("cannot load csrf token", e)
            return null
        }

    }

    private fun handleError(urlSuffix: String, isRetry: Boolean, url: String?, e: Exception, context: Context): RequestResult<InputStream> {
        setErrorInErrorHolderFor(e, url, urlSuffix)
        return handleRetryIfRequired(isRetry, RequestResult(HOST_CONNECTION_ERROR), urlSuffix, context)
    }

    private fun handleRetryIfRequired(isCurrentRequestRetry: Boolean, previousResult: RequestResult<InputStream>,
                                      urlSuffix: String, context: Context): RequestResult<InputStream> {
        return if (!serverSpec.canRetry() || isCurrentRequestRetry) {
            previousResult
        } else retryRequest(urlSuffix, context)
    }

    private fun retryRequest(urlSuffix: String, context: Context): RequestResult<InputStream> {
        LOG.info("retrying request for alternate URL")
        return executeRequest(serverSpec.alternateUrl, urlSuffix, true, context)
    }

    override fun requestBitmap(relativePath: String, context: Context): RequestResult<Bitmap> {
        val response = executeRequest(relativePath, context)
        if (response.error != null) {
            return RequestResult(response.error)
        }
        try {
            return RequestResult(BitmapFactory.decodeStream(response.content))
        } finally {
            close(response.content)
        }
    }

    private fun initSslContext(context: Context) {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            val memorizingTrustManager = MemorizingTrustManager(context)
            var clientKeys: Array<KeyManager>? = null
            if (serverSpec.clientCertificatePath != null) {
                val clientCertificate = File(serverSpec.clientCertificatePath)
                val clientCertificatePassword = serverSpec.clientCertificatePassword
                if (clientCertificate.exists()) {
                    val keyStore = loadPKCS12KeyStore(clientCertificate, clientCertificatePassword)
                    val keyManagerFactory = KeyManagerFactory.getInstance("X509")
                    keyManagerFactory.init(keyStore, firstNonNull(clientCertificatePassword, "").toCharArray())
                    clientKeys = keyManagerFactory.keyManagers
                }
            }
            sslContext.init(clientKeys, arrayOf<X509TrustManager>(memorizingTrustManager), java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier(
                    memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()))
        } catch (e: Exception) {
            LOG.error("Error initializing HttpsUrlConnection", e)
        }

    }

    @Throws(Exception::class)
    private fun loadPKCS12KeyStore(certificateFile: File, clientCertPassword: String): KeyStore? {
        val keyStore: KeyStore?
        var fileInputStream: FileInputStream? = null
        try {
            keyStore = KeyStore.getInstance("PKCS12")
            fileInputStream = FileInputStream(certificateFile)
            keyStore!!.load(fileInputStream, clientCertPassword.toCharArray())
        } finally {
            close(fileInputStream)
        }
        return keyStore
    }

    companion object {

        val SOCKET_TIMEOUT = 20000
        private val LOG = LoggerFactory.getLogger(FHEMWEBConnection::class.java)
        val STATUS_CODE_MAP: Map<Int, RequestResultError> = ImmutableMap.builder<Int, RequestResultError>()
                .put(400, BAD_REQUEST)
                .put(401, AUTHENTICATION_ERROR)
                .put(403, AUTHENTICATION_ERROR)
                .put(404, NOT_FOUND)
                .put(500, INTERNAL_SERVER_ERROR)
                .build()

        init {
            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory())
        }

        private fun handleHttpStatusCode(statusCode: Int): RequestResult<InputStream>? {

            val error = STATUS_CODE_MAP[statusCode] ?: return null

            LOG.info("handleHttpStatusCode() : encountered http status code {}", statusCode)
            return RequestResult(error)
        }
    }
}
