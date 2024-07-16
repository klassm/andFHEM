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

package li.klass.fhem.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import li.klass.fhem.R
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.ssl.AndFHEMMemorizingTrustManager
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.util.ReflectionUtil
import org.apache.commons.lang3.StringUtils.trimToNull
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.net.URL
import java.security.cert.X509Certificate
import javax.inject.Inject

abstract class AbstractWebViewFragment : BaseFragment() {

    @Inject
    lateinit var connectionService: ConnectionService

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) return view

        val newView = inflater.inflate(R.layout.webview, container, false)

        val webView = newView.findViewById<View>(R.id.webView) as WebView
        webView.settings.apply {
            useWideViewPort = true
            loadWithOverviewMode = true
            javaScriptEnabled = true
            builtInZoomControls = true
        }

        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage(resources.getString(R.string.loading))

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (showProgressDialog()) {
                    if (newProgress < 100) {
                        progressDialog.progress = newProgress
                        progressDialog.show()
                    } else {
                        progressDialog.hide()
                    }
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                val certificate = error.certificate
                try {
                    val trustManager = AndFHEMMemorizingTrustManager(context)
                    trustManager.bindDisplayActivity(activity)
                    val x509Certificate = ReflectionUtil.getFieldValue(certificate, certificate.javaClass.getDeclaredField("mX509Certificate")) as X509Certificate
                    val hostname = URL(error.url).host

                    Thread(Runnable {
                        if (trustManager.checkCertificate(x509Certificate, hostname)) {
                            handler.proceed()
                        } else {
                            handler.cancel()
                        }
                        trustManager.unbindDisplayActivity(activity)
                    }).start()


                } catch (e: Exception) {
                    LOG.error("cannot handle error", e)
                    handler.cancel()
                }

            }

            override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
                val currentServer = connectionService.getCurrentServer()
                val url = currentServer!!.url
                val alternativeUrl = trimToNull(currentServer.alternateUrl)
                try {

                    val fhemUrlHost = URL(url).host
                    val alternativeUrlHost = if (alternativeUrl == null) null else URL(alternativeUrl).host
                    val username = currentServer.username
                    val password = currentServer.password

                    if (host.startsWith(fhemUrlHost) || alternativeUrlHost != null && host.startsWith(alternativeUrlHost)) {
                        handler.proceed(username, password)
                    } else {
                        handler.cancel()

                        val intent = Intent(Actions.SHOW_TOAST)
                        intent.putExtra(BundleExtraKeys.STRING_ID, R.string.error_authentication)
                            .apply { setPackage(activity?.packageName) }
                        activity!!.sendBroadcast(intent)
                    }

                } catch (e: MalformedURLException) {
                    val intent = Intent(Actions.SHOW_TOAST)
                    intent.putExtra(BundleExtraKeys.STRING_ID, R.string.error_host_connection)
                        .apply { setPackage(activity?.packageName) }
                    activity!!.sendBroadcast(intent)
                    LOG.error("malformed URL: " + url!!, e)

                    handler.cancel()
                }

            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if ("about:blank".equals(url, ignoreCase = true)) {
                    val alternativeUrl = getAlternateLoadUrl()
                    if (alternativeUrl != null) {
                        webView.loadUrl(handleUrl(connectionService.getCurrentServer()!!.alternateUrl, alternativeUrl))
                    }
                } else {
                    onPageLoadFinishedCallback(view, url)
                }
            }
        }

        return newView
    }

    private fun handleUrl(serverUrl: String?, toLoad: String): String {
        if (!toLoad.startsWith("/")) {
            return toLoad
        }
        try {
            val url = URL(serverUrl)
            var newUrl = url.protocol + "://" + url.host
            if (url.port != -1) {
                newUrl += ":" + url.port
            }
            newUrl += toLoad

            return newUrl
        } catch (e: MalformedURLException) {
            LOG.error("cannot parse URL", e)
            return toLoad
        }

    }

    protected open fun onPageLoadFinishedCallback(view: WebView, url: String) {}

    override suspend fun update(refresh: Boolean) {
        if (view == null) return

        val webView = view!!.findViewById<View>(R.id.webView) as WebView

        val currentServer = connectionService.getCurrentServer()
        val url = currentServer!!.url
        try {
            if (url != null) {
                val host = URL(currentServer.url).host
                val username = currentServer.username
                val password = currentServer.password

                if (username != null && password != null) {
                    webView.setHttpAuthUsernamePassword(host, "", username, password)
                }
            }

            webView.loadUrl(handleUrl(connectionService.getCurrentServer()!!.url, getLoadUrl()))
        } catch (e: MalformedURLException) {
            val intent = Intent(Actions.SHOW_TOAST)
            intent.putExtra(BundleExtraKeys.STRING_ID, R.string.error_host_connection)
                .apply { setPackage(activity?.packageName) }
            requireActivity().sendBroadcast(intent)
            LOG.error("malformed URL: " + url!!, e)
        }

    }

    open fun showProgressDialog(): Boolean = true

    abstract fun getLoadUrl(): String

    open fun getAlternateLoadUrl(): String? = null

    companion object {
        private val LOG = LoggerFactory.getLogger(AbstractWebViewFragment::class.java)
    }
}
