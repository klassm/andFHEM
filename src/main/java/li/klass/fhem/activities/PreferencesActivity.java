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
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.apache.commons.lang3.ArrayUtils;

import java.security.KeyStoreException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import de.duenndns.ssl.MemorizingTrustManager;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.service.device.GCMSendDeviceService;
import li.klass.fhem.ui.service.importExport.ImportExportUIService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DisplayUtil;
import li.klass.fhem.widget.preference.SeekBarPreference;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static li.klass.fhem.adapter.rooms.DeviceGridAdapter.DEFAULT_COLUMN_WIDTH;
import static li.klass.fhem.constants.PreferenceKeys.CLEAR_TRUSTED_CERTIFICATES;
import static li.klass.fhem.constants.PreferenceKeys.COMMAND_EXECUTION_RETRIES;
import static li.klass.fhem.constants.PreferenceKeys.CONNECTION_TIMEOUT;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_COLUMN_WIDTH;
import static li.klass.fhem.constants.PreferenceKeys.EXPORT_SETTINGS;
import static li.klass.fhem.constants.PreferenceKeys.GCM_PROJECT_ID;
import static li.klass.fhem.constants.PreferenceKeys.IMPORT_SETTINGS;
import static li.klass.fhem.constants.PreferenceKeys.SEND_APP_LOG;
import static li.klass.fhem.constants.PreferenceKeys.SEND_LAST_ERROR;
import static li.klass.fhem.fhem.FHEMConnection.CONNECTION_TIMEOUT_DEFAULT_SECONDS;
import static li.klass.fhem.service.CommandExecutionService.DEFAULT_NUMBER_OF_RETRIES;

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static Logger LOGGER = Logger.getLogger(PreferencesActivity.class.getName());

    @Inject
    GCMSendDeviceService gcmSendDeviceService;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    ImportExportUIService importExportUIService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AndFHEMApplication) getApplication()).inject(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.layout.preferences);

        attachListSummaryListenerTo(PreferenceKeys.STARTUP_VIEW, R.array.startupViewsValues, R.array.startupViews, R.string.prefStartupViewSummary);
        attachIntSummaryListenerTo(PreferenceKeys.DEVICE_COLUMN_WIDTH, R.string.prefDeviceColumnWidthSummary);
        attachIntSummaryListenerTo(PreferenceKeys.DEVICE_LIST_RIGHT_PADDING, R.string.prefDeviceListPaddingRightSummary);
        attachListSummaryListenerTo(PreferenceKeys.GRAPH_DEFAULT_TIMESPAN, R.array.graphDefaultTimespanValues, R.array.graphDefaultTimespanEntries, R.string.prefDefaultTimespanSummary);
        attachListSummaryListenerTo(PreferenceKeys.WIDGET_UPDATE_INTERVAL_WLAN, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries, R.string.prefWidgetUpdateTimeWLANSummary);
        attachListSummaryListenerTo(PreferenceKeys.WIDGET_UPDATE_INTERVAL_MOBILE, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries, R.string.prefWidgetUpdateTimeMobileSummary);
        attachStringSummaryListenerTo(PreferenceKeys.GCM_PROJECT_ID, R.string.prefGCMProjectIdSummary);
        attachListSummaryListenerTo(PreferenceKeys.AUTO_UPDATE_TIME, R.array.updateRoomListTimeValues, R.array.updateRoomListTimeEntries, R.string.prefAutoUpdateSummary);
        attachIntSummaryListenerTo(PreferenceKeys.CONNECTION_TIMEOUT, R.string.prefConnectionTimeoutSummary);
        attachIntSummaryListenerTo(PreferenceKeys.COMMAND_EXECUTION_RETRIES, R.string.prefCommandExecutionRetriesSummary);
        attachStringSummaryListenerTo(PreferenceKeys.DEVICE_NAME, R.string.prefDeviceNameSummary);

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

        findPreference(CLEAR_TRUSTED_CERTIFICATES).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MemorizingTrustManager mtm = new MemorizingTrustManager(AndFHEMApplication.getContext());
                Enumeration<String> aliases = mtm.getCertificates();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    try {
                        mtm.deleteCertificate(alias);
                        LOGGER.log(Level.INFO, "Deleting certificate for {} ", alias);
                    } catch (KeyStoreException e) {
                        LOGGER.log(Level.SEVERE, "Could not delete certificate", e);
                    }
                }
                Toast.makeText(getApplicationContext(), getString(R.string.prefClearTrustedCertificatesFinished), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference exportPreference = findPreference(EXPORT_SETTINGS);
        exportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                importExportUIService.handleExport(PreferencesActivity.this);
                return true;
            }
        });
        if (AndFHEMApplication.getAndroidSDKLevel() <= Build.VERSION_CODES.KITKAT) {
            getPreferenceScreen().removePreference(exportPreference);
        }

        Preference importPreference = findPreference(IMPORT_SETTINGS);
        importPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                importExportUIService.handleImport(PreferencesActivity.this);
                return true;
            }
        });
        if (AndFHEMApplication.getAndroidSDKLevel() <= Build.VERSION_CODES.KITKAT) {
            getPreferenceScreen().removePreference(importPreference);
        }

        SeekBarPreference connectionTimeoutPreference = (SeekBarPreference) findPreference(CONNECTION_TIMEOUT);
        connectionTimeoutPreference.setMinimumValue(1);
        connectionTimeoutPreference.setDefaultValue(CONNECTION_TIMEOUT_DEFAULT_SECONDS);

        SeekBarPreference commandExecutionRetriesPreference = (SeekBarPreference) findPreference(COMMAND_EXECUTION_RETRIES);
        commandExecutionRetriesPreference.setDefaultValue(DEFAULT_NUMBER_OF_RETRIES);

        Preference voiceCommands = findPreference(PreferenceKeys.VOICE_COMMANDS);
        voiceCommands.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                return true;
            }
        });
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

    private void attachIntSummaryListenerTo(String preferenceKey, final int summaryTemplate) {
        Preference preference = findPreference(preferenceKey);
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(String.format(getString(summaryTemplate), newValue));
                return true;
            }
        });
        preference.setSummary(String.format(getString(summaryTemplate), applicationProperties.getIntegerSharedPreference(preferenceKey, 0)));
    }

    private void attachStringSummaryListenerTo(String preferenceKey, final int summaryTemplate) {
        Preference preference = findPreference(preferenceKey);
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(String.format(getString(summaryTemplate), newValue));
                return true;
            }
        });
        preference.setSummary(String.format(getString(summaryTemplate), firstNonNull(applicationProperties.getStringSharedPreference(preferenceKey, null), "")));
    }

    private void attachListSummaryListenerTo(String preferenceKey, final int valuesArrayResource, final int textArrayResource, final int summaryTemplate) {
        Preference preference = findPreference(preferenceKey);
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(nameForArrayValueFormatted(valuesArrayResource, textArrayResource, newValue.toString(), summaryTemplate));
                return true;
            }
        });
        preference.setSummary(nameForArrayValueFormatted(valuesArrayResource, textArrayResource,
                applicationProperties.getStringSharedPreference(preferenceKey), summaryTemplate));
    }

    private String nameForArrayValueFormatted(int valuesArrayResource, int textArrayResource, String value, int summaryTemplate) {
        return String.format(getString(summaryTemplate), nameForArrayValue(valuesArrayResource, textArrayResource, value));
    }

    private String nameForArrayValue(int valuesArrayResource, int textArrayResource, String value) {
        int index = ArrayUtils.indexOf(getResources().getStringArray(valuesArrayResource), value);
        checkArgument(index >= 0);

        return getResources().getStringArray(textArrayResource)[index];
    }
}