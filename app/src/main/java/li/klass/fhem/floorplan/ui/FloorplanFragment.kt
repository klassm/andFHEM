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

package li.klass.fhem.floorplan.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.navigation.fragment.navArgs
import com.google.common.io.Resources
import li.klass.fhem.R
import li.klass.fhem.ui.AbstractWebViewFragment
import li.klass.fhem.util.BuildVersion
import java.nio.charset.Charset
import javax.inject.Inject

class FloorplanFragment @Inject constructor() : AbstractWebViewFragment() {

    private val args: FloorplanFragmentArgs by navArgs()

    override fun onPageLoadFinishedCallback(view: WebView, url: String) {
        if (url.contains("&XHR=1")) {
            view.loadUrl(getLoadUrl())
            return
        }

        val modifyJsUrl = FloorplanFragment::class.java.getResource("floorplan-modify.js")
        try {
            val modifyJs = Resources.toString(modifyJsUrl!!, Charset.forName("UTF-8"))
            BuildVersion.execute(object : BuildVersion.VersionDependent {
                override fun ifBelow() {
                    view.loadUrl("javascript:$modifyJs")
                }

                @SuppressLint("NewApi")
                override fun ifAboveOrEqual() {
                    view.evaluateJavascript(modifyJs, null)
                }
            }, 19)
            view.loadUrl("javascript:$modifyJs")
        } catch (e: Exception) {
            Log.e(TAG, "cannot load floorplan-modify.js", e)
        }

    }

    override fun getTitle(context: Context) = context.getString(R.string.functionalityFloorplan)

    override fun canChildScrollUp(): Boolean = true

    override fun getLoadUrl(): String {
        val server = connectionService.getCurrentServer()
        return server!!.url + "/floorplan/" + args.deviceName
    }

    override fun getAlternateLoadUrl(): String? {
        return connectionService.getCurrentServer()?.alternateUrl?.let {
            "$it/floorplan/${args.deviceName}"
        }
    }

    companion object {
        val TAG = FloorplanFragment::class.java.name
    }
}
