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
import android.webkit.WebView;

import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.connection.ConnectionService;

public class FloorplanFragment extends AbstractWebViewFragment {

    public static final String TAG = FloorplanFragment.class.getName();
    private String deviceName;

    @SuppressWarnings("unused")
    public FloorplanFragment() {
    }

    @SuppressWarnings("unused")
    public FloorplanFragment(Bundle bundle) {
        super(bundle);

        deviceName = bundle.getString(BundleExtraKeys.DEVICE_NAME);
    }

    @Override
    protected void onPageLoadFinishedCallback(WebView view, String url) {
        if (url.contains("&XHR=1")) {
            view.loadUrl(getLoadUrl());
            return;
        }

        String script = "javascript:" +

                // hide floorplan navigation elements
                "var floorplans = document.getElementById(\"floorplans\");" +
                "if (!! floorplans) floorplans.style.display=\"none\";" +

                "var fpmenu = document.getElementById(\"fpmenu\");" +
                "if (!! fpmenu) fpmenu.style.display=\"none\";" +

                "var logo = document.getElementById(\"logo\");" +
                "if (!! logo) logo.style.display=\"none\";" +

                // shift the background image to left, compute the left offset
                "var backImg = document.getElementById(\"backimg\"); " +
                "if (!! backImg) {" +
                "  var backImgOffset = window.getComputedStyle(backImg, null).getPropertyValue(\"left\").replace(\"px\", \"\");" +
                "  document.getElementById(\"backimg\").style.left=\"0\";" +
                "}" +

                // move each child element to left by using the computed background image offset
                "var elements = document.getElementById(\"floorplan\").getElementsByTagName(\"div\"); " +
                "if (!! elements) {" +
                "  for (var i = 0; i < elements.length; i++) { " +
                "    var left = elements[i].style.left.replace(\"px\", \"\"); " +
                "    elements[i].style.left = (left - backImgOffset) + \"px\" " +
                "  }" +
                "};" +

                // override the implemented FW_cmd function to allow page
                // reloading when the XMLHttpRequest is finished
                "function FW_cmd(arg) { " +
                "  var req = new XMLHttpRequest(); " +
                "  req.onreadystatechange=function() { " +
                "  if (req.readyState == 4 && req.status == 200) {" +
                "    window.location.reload();" +
                "  }" +
                "};" +
                "req.open(\"GET\", arg, true); " +
                "req.send(null);}";
        view.loadUrl(script);
    }

    protected String getLoadUrl() {
        String url = ConnectionService.INSTANCE.getCurrentServer().getUrl();
        return url + "/floorplan/" + deviceName;
    }
}
