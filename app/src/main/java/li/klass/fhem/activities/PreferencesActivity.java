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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.service.device.GCMSendDeviceService;
import li.klass.fhem.util.DisplayUtil;
import li.klass.fhem.widget.preference.SeekBarPreference;

import static li.klass.fhem.adapter.rooms.DeviceGridAdapter.DEFAULT_COLUMN_WIDTH;
import static li.klass.fhem.constants.PreferenceKeys.COMMAND_EXECUTION_RETRIES;
import static li.klass.fhem.constants.PreferenceKeys.CONNECTION_TIMEOUT;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_COLUMN_WIDTH;
import static li.klass.fhem.constants.PreferenceKeys.GCM_PROJECT_ID;
import static li.klass.fhem.constants.PreferenceKeys.SEND_APP_LOG;
import static li.klass.fhem.constants.PreferenceKeys.SEND_LAST_ERROR;
import static li.klass.fhem.fhem.FHEMConnection.CONNECTION_TIMEOUT_DEFAULT_SECONDS;
import static li.klass.fhem.service.CommandExecutionService.DEFAULT_NUMBER_OF_RETRIES;

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    GCMSendDeviceService gcmSendDeviceService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AndFHEMApplication) getApplication()).inject(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.layout.preferences);

        SeekBarPreference deviceColumnWidthPreference = (SeekBarPreference) findPreference(DEVICE_COLUMN_WIDTH);
        deviceColumnWidthPreference.setMinimumValue(200);
        deviceColumnWidthPreference.setDefaultValue(DEFAULT_COLUMN_WIDTH);
        deviceColumnWidthPreference.setMaximumValue(DisplayUtil.getLargestDimensionInDP());

        findPreference(GCM_PROJECT_ID).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String projectId = (String) o;
                gcmSendDeviceService.registerWithGCM(PreferencesActivity.this, projectId);
                return true;
            }
        });

        findPreference(SEND_LAST_ERROR).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ErrorHolder.sendLastErrorAsMail(PreferencesActivity.this);
                return true;
            }
        });

        findPreference(SEND_APP_LOG).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ErrorHolder.sendApplicationLogAsMail(PreferencesActivity.this);
                return true;
            }
        });

        SeekBarPreference connectionTimeoutPreference = (SeekBarPreference) findPreference(CONNECTION_TIMEOUT);
        connectionTimeoutPreference.setMinimumValue(1);
        connectionTimeoutPreference.setDefaultValue(CONNECTION_TIMEOUT_DEFAULT_SECONDS);

        SeekBarPreference commandExecutionRetriesPreference = (SeekBarPreference) findPreference(COMMAND_EXECUTION_RETRIES);
        commandExecutionRetriesPreference.setDefaultValue(DEFAULT_NUMBER_OF_RETRIES);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        sendBroadcast(new Intent(Actions.REDRAW));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    }
}