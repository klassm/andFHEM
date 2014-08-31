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

package li.klass.fhem.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.google.common.io.Resources;

import java.net.URL;
import java.nio.charset.Charset;

import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.util.BuildVersion;

public class FloorplanFragment extends AbstractWebViewFragment {

    public static final String TAG = FloorplanFragment.class.getName();
    private String deviceName;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        deviceName = args.getString(BundleExtraKeys.DEVICE_NAME);
    }


    @Override
    protected void onPageLoadFinishedCallback(final WebView view, String url) {
        if (url.contains("&XHR=1")) {
            view.loadUrl(getLoadUrl());
            return;
        }

        URL modifyJsUrl = FloorplanFragment.class.getResource("floorplan-modify.js");
        try {
            final String modifyJs = Resources.toString(modifyJsUrl, Charset.forName("UTF-8"));
            BuildVersion.execute(new BuildVersion.VersionDependent() {
                @Override
                public void ifBelow() {
                    view.loadUrl("javascript:" + modifyJs);
                }

                @Override
                @SuppressLint("NewApi")
                public void ifAboveOrEqual() {
                    view.evaluateJavascript(modifyJs, null);
                }
            }, 19);
            view.loadUrl("javascript:" + modifyJs);
        } catch (Exception e) {
            Log.e(TAG, "cannot load floorplan-modify.js", e);
        }
    }

    protected String getLoadUrl() {
        String url = connectionService.getCurrentServer().getUrl();
        return url + "/floorplan/" + deviceName;
    }
}
