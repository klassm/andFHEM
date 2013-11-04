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

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.MalformedURLException;
import java.net.URL;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fhem.FHEMWebConnection;
import li.klass.fhem.fragments.core.BaseFragment;

public class FloorplanFragment extends BaseFragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) return view;

        view = inflater.inflate(R.layout.floorplan_webview, null);
        assert view != null;

        WebView webView = (WebView) view.findViewById(R.id.webView);


        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.loading));

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    progressDialog.setProgress(newProgress);
                    progressDialog.show();
                } else {
                    progressDialog.hide();
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                String url = FHEMWebConnection.getURL();
                try {
                    String fhemHost = new URL(url).getHost();
                    String username = FHEMWebConnection.getUsername();
                    String password = FHEMWebConnection.getPassword();

                    if (host.startsWith(fhemHost)) {
                        handler.proceed(username, password);
                    } else {
                        handler.cancel();

                        Intent intent = new Intent(Actions.SHOW_TOAST);
                        intent.putExtra(BundleExtraKeys.TOAST_STRING_ID, R.string.authenticationError);
                        getActivity().sendBroadcast(intent);
                    }

                } catch (MalformedURLException e) {
                    Intent intent = new Intent(Actions.SHOW_TOAST);
                    intent.putExtra(BundleExtraKeys.TOAST_STRING_ID, R.string.updateErrorHostConnection);
                    getActivity().sendBroadcast(intent);
                    Log.e(FloorplanFragment.class.getName(), "malformed URL: " + url, e);

                    handler.cancel();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("&XHR=1")) {
                    view.loadUrl(getLoadUrl());
                    return;
                }

                String script = "javascript:" +

                        // hide floorplan navigation elements
                        "document.getElementById(\"floorplans\").style.display=\"none\";" +
                        "document.getElementById(\"fpmenu\").style.display=\"none\";" +
                        "document.getElementById(\"logo\").style.display=\"none\";" +

                        // shift the background image to left, compute the left offset
                        "var backImg = document.getElementById(\"backimg\"); " +
                        "var backImgOffset = window.getComputedStyle(backImg, null).getPropertyValue(\"left\").replace(\"px\", \"\");" +
                        "document.getElementById(\"backimg\").style.left=\"0\";" +

                        // move each child element to left by using the computed background image offset
                        "var elements = document.getElementById(\"floorplan\").getElementsByTagName(\"div\"); " +
                        "for (var i = 0; i < elements.length; i++) { " +
                        "var left = elements[i].style.left.replace(\"px\", \"\"); " +
                        "elements[i].style.left = (left - backImgOffset) + \"px\" " +
                        "};" +

                        // override the implemented FW_cmd function to allow page
                        // reloading when the XMLHttpRequest is finished
                        "function FW_cmd(arg) { " +
                        "var req = new XMLHttpRequest(); " +
                        "req.onreadystatechange=function() { " +
                        "if (req.readyState == 4 && req.status == 200) {" +
                        "window.location.reload();" +
                        "}" +
                        "};" +
                        "req.open(\"GET\", arg, true); " +
                        "req.send(null);}";
                view.loadUrl(script);
            }
        });

        return view;
    }

    @Override
    public void update(boolean doUpdate) {
        WebView webView = (WebView) getView().findViewById(R.id.webView);

        String url = FHEMWebConnection.getURL();
        try {
            String host = new URL(url).getHost();
            String username = FHEMWebConnection.getUsername();
            String password = FHEMWebConnection.getPassword();

            webView.setHttpAuthUsernamePassword(host, "", username, password);

            webView.loadUrl(getLoadUrl());
//            webView.loadUrl("http://www.google.de");
        } catch (MalformedURLException e) {
            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.TOAST_STRING_ID, R.string.updateErrorHostConnection);
            getActivity().sendBroadcast(intent);
            Log.e(FloorplanFragment.class.getName(), "malformed URL: " + url, e);
        }
    }

    private String getLoadUrl() {
        String url = FHEMWebConnection.getURL();
        return url + "/floorplan/" + deviceName;
    }
}
