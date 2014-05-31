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

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.connection.ConnectionService;

public class FloorplanFragment extends AbstractWebViewFragment {

    public static final String TAG = FloorplanFragment.class.getName();
    private String deviceName;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        deviceName = args.getString(BundleExtraKeys.DEVICE_NAME);
    }

    @Override
    protected void onPageLoadFinishedCallback(WebView view, String url) {
        if (url.contains("&XHR=1")) {
            view.loadUrl(getLoadUrl());
            return;
        }

        InputStream modifyJsStream = FloorplanFragment.class.getResourceAsStream("floorplan-modify.js");
        try {
            String modifyJs = IOUtils.toString(modifyJsStream);
            view.loadUrl("javascript:" + modifyJs);
        } catch (IOException e) {
            Log.e(TAG, "cannot load floorplan-modify.js", e);
        }
    }

    protected String getLoadUrl() {
        String url = ConnectionService.INSTANCE.getCurrentServer().getUrl();
        return url + "/floorplan/" + deviceName;
    }
}
