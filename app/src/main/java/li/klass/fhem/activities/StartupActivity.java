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

package li.klass.fhem.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.service.intent.FavoritesIntentService;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.util.FhemResultReceiver;

import static com.google.common.base.Strings.isNullOrEmpty;
import static li.klass.fhem.constants.PreferenceKeys.STARTUP_PASSWORD;
import static li.klass.fhem.constants.PreferenceKeys.UPDATE_ON_APPLICATION_START;

public class StartupActivity extends Activity {
    private static final String TAG = StartupActivity.class.getName();

    @Inject
    ApplicationProperties applicationProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        ((AndFHEMApplication) getApplication()).inject(this);

        setContentView(R.layout.startup);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startService(new Intent(Actions.REMOTE_UPDATE_RESET)
                .setClass(this, RoomListIntentService.class));

        if (!isNullOrEmpty(getPassword())) {
            showLoginDialog();
        } else {
            handleLoginStatus();
        }
    }

    private void showLoginDialog() {
        getLoginStatus().setVisibility(View.GONE);
        getLoginLayout().setVisibility(View.VISIBLE);

        Button loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText passwordInput = (EditText) findViewById(R.id.password);
                String password = passwordInput.getText().toString();
                if (password.equals(getPassword())) {
                    handleLoginStatus();
                } else {
                    DialogUtil.showAlertDialog(StartupActivity.this, null,
                            getString(R.string.wrongPassword));
                }
                passwordInput.setText("");
            }
        });
    }

    private View getLoginLayout() {
        return findViewById(R.id.loginForm);
    }

    private void handleLoginStatus() {
        getLoginLayout().setVisibility(View.GONE);
        getLoginStatus().setVisibility(View.VISIBLE);

        initializeGoogleBilling();
    }

    private void initializeGoogleBilling() {
        setCurrentStatus(R.string.currentStatus_billing);

        startService(new Intent(Actions.IS_PREMIUM)
                        .setClass(this, LicenseIntentService.class)
                        .putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode == ResultCodes.ERROR) {
                                    Log.e(TAG, "initializeGoogleBilling() : cannot initialize connection to Google Billing");
                                } else {
                                    Log.i(TAG, "initializeGoogleBilling() : connection was initialized");
                                }

                                // we need to continue anyway.
                                loadDeviceList();
                            }
                        })
        );
    }

    private View getLoginStatus() {
        return findViewById(R.id.loginStatus);
    }

    private void loadDeviceList() {
        setCurrentStatus(R.string.currentStatus_loadingDeviceList);

        Intent intent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST)
                .setClass(this, RoomListIntentService.class)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == ResultCodes.ERROR) {
                            Log.e(TAG, "loadDeviceList() : cannot load device list: " + resultData);
                        } else {
                            Log.d(TAG, "loadDeviceList() : device list was loaded");
                            loadFavorites();
                        }
                    }
                });
        boolean updateOnApplicationStart = applicationProperties.getBooleanSharedPreference(UPDATE_ON_APPLICATION_START, false);
        if (updateOnApplicationStart) {
            intent.putExtra(BundleExtraKeys.UPDATE_PERIOD, RoomListService.ALWAYS_UPDATE_PERIOD);
        }

        startService(intent);
    }

    private void loadFavorites() {
        setCurrentStatus(R.string.currentStatus_loadingFavorites);

        startService(new Intent(Actions.FAVORITES_PRESENT)
                .setClass(this, FavoritesIntentService.class)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == ResultCodes.ERROR) {
                            Log.e(TAG, "loadFavorites : cannot load favorites: " + resultData);
                        } else {
                            boolean favoritesPresent = resultData.getBoolean(BundleExtraKeys.HAS_FAVORITES);
                            Log.d(TAG, "loadFavorites : favorites_present=" + favoritesPresent);
                            gotoMainActivity(favoritesPresent);
                        }
                    }
                }));
    }

    private void setCurrentStatus(int stringId) {
        ((TextView) findViewById(R.id.currentStatus)).setText(stringId);
    }

    private String getPassword() {
        return applicationProperties.getStringSharedPreference(STARTUP_PASSWORD, "");
    }

    private void gotoMainActivity(boolean favoritesPresent) {

        startActivity(new Intent(this, AndFHEMMainActivity.class)
                .putExtra(BundleExtraKeys.HAS_FAVORITES, favoritesPresent));
    }
}
