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

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.service.connection.ConnectionService;

public abstract class AbstractWebViewFragment extends BaseFragment {
    public static final String TAG = AbstractWebViewFragment.class.getName();

    @Inject
    ConnectionService connectionService;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) return view;

        view = inflater.inflate(R.layout.webview, container, false);
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
            public void onReceivedSslError(WebView view, @NotNull SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, @NotNull HttpAuthHandler handler, String host, String realm) {
                FHEMServerSpec currentServer = connectionService.getCurrentServer();
                String url = currentServer.getUrl();
                try {
                    String fhemHost = new URL(url).getHost();
                    String username = currentServer.getUsername();
                    String password = currentServer.getPassword();

                    if (host.startsWith(fhemHost)) {
                        handler.proceed(username, password);
                    } else {
                        handler.cancel();

                        Intent intent = new Intent(Actions.SHOW_TOAST);
                        intent.putExtra(BundleExtraKeys.STRING_ID, R.string.error_authentication);
                        getActivity().sendBroadcast(intent);
                    }

                } catch (MalformedURLException e) {
                    Intent intent = new Intent(Actions.SHOW_TOAST);
                    intent.putExtra(BundleExtraKeys.STRING_ID, R.string.error_host_connection);
                    getActivity().sendBroadcast(intent);
                    Log.e(TAG, "malformed URL: " + url, e);

                    handler.cancel();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                onPageLoadFinishedCallback(view, url);
            }
        });

        return view;
    }

    protected void onPageLoadFinishedCallback(WebView view, String url) {
    }

    @Override
    public void update(boolean doUpdate) {
        if (getView() == null) return;

        WebView webView = (WebView) getView().findViewById(R.id.webView);

        FHEMServerSpec currentServer = connectionService.getCurrentServer();
        String url = currentServer.getUrl();
        try {
            if (url != null) {
                String host = new URL(currentServer.getUrl()).getHost();
                String username = currentServer.getUsername();
                String password = currentServer.getPassword();

                if (username != null && password != null) {
                    webView.setHttpAuthUsernamePassword(host, "", username, password);
                }
            }

            webView.loadUrl(getLoadUrl());
        } catch (MalformedURLException e) {
            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.STRING_ID, R.string.error_host_connection);
            getActivity().sendBroadcast(intent);
            Log.e(TAG, "malformed URL: " + url, e);
        }
    }

    protected abstract String getLoadUrl();
}
