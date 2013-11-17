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

import com.hlidskialf.android.preference.SeekBarPreference;

import li.klass.fhem.GCMIntentService;
import li.klass.fhem.R;
import li.klass.fhem.adapter.rooms.DeviceGridAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.util.DisplayUtil;

import static li.klass.fhem.constants.PreferenceKeys.DEVICE_COLUMN_WIDTH;

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean preferencesChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesChanged = false;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.layout.preferences);

        SeekBarPreference deviceColumnWidthPreference = (SeekBarPreference) findPreference(DEVICE_COLUMN_WIDTH);
        deviceColumnWidthPreference.setMin(200);
        deviceColumnWidthPreference.setDefaultValue(DeviceGridAdapter.DEFAULT_COLUMN_WIDTH);
        deviceColumnWidthPreference.setMax(DisplayUtil.getLargestDimensionInDP(this));

        findPreference("GCM_PROJECT_ID").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String projectId = (String) o;
                GCMIntentService.registerWithGCM(PreferencesActivity.this, projectId);
                return true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        if (preferencesChanged) {
            sendBroadcast(new Intent(Actions.DO_UPDATE));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        preferencesChanged = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (preferencesChanged) {
            Intent intent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST);
            intent.putExtra(BundleExtraKeys.DO_REFRESH, true);
            startService(intent);
        }
    }
}