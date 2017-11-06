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

package li.klass.fhem.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import android.widget.Toast
import com.google.common.base.MoreObjects.firstNonNull
import de.duenndns.ssl.MemorizingTrustManager
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.AndFHEMApplication.Companion.application
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.constants.PreferenceKeys.*
import li.klass.fhem.error.ErrorHolder
import li.klass.fhem.fcm.GCMSendDeviceService
import li.klass.fhem.fhem.FHEMConnection.CONNECTION_TIMEOUT_DEFAULT_SECONDS
import li.klass.fhem.fragments.core.DeviceListFragment
import li.klass.fhem.service.CommandExecutionService
import li.klass.fhem.ui.service.importExport.ImportExportUIService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.widget.preference.SeekBarPreference
import org.apache.commons.lang3.ArrayUtils
import java.security.KeyStoreException
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var gcmSendDeviceService: GCMSendDeviceService

    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var importExportUIService: ImportExportUIService

    private var delegate: AppCompatDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AndFHEMApplication).daggerComponent.inject(this)

        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        preferences.registerOnSharedPreferenceChangeListener(this)

        addPreferencesFromResource(R.xml.preferences)

        attachListSummaryListenerTo(PreferenceKeys.STARTUP_VIEW, R.array.startupViewsValues, R.array.startupViews, R.string.prefStartupViewSummary)
        attachIntSummaryListenerTo(PreferenceKeys.DEVICE_COLUMN_WIDTH, R.string.prefDeviceColumnWidthSummary)
        attachIntSummaryListenerTo(PreferenceKeys.DEVICE_LIST_RIGHT_PADDING, R.string.prefDeviceListPaddingRightSummary)
        attachListSummaryListenerTo(PreferenceKeys.GRAPH_DEFAULT_TIMESPAN, R.array.graphDefaultTimespanValues, R.array.graphDefaultTimespanEntries, R.string.prefDefaultTimespanSummary)
        attachListSummaryListenerTo(PreferenceKeys.WIDGET_UPDATE_INTERVAL_WLAN, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries, R.string.prefWidgetUpdateTimeWLANSummary)
        attachListSummaryListenerTo(PreferenceKeys.WIDGET_UPDATE_INTERVAL_MOBILE, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries, R.string.prefWidgetUpdateTimeMobileSummary)
        attachListSummaryListenerTo(PreferenceKeys.AUTO_UPDATE_TIME, R.array.updateRoomListTimeValues, R.array.updateRoomListTimeEntries, R.string.prefAutoUpdateSummary)
        attachIntSummaryListenerTo(PreferenceKeys.CONNECTION_TIMEOUT, R.string.prefConnectionTimeoutSummary)
        attachIntSummaryListenerTo(PreferenceKeys.COMMAND_EXECUTION_RETRIES, R.string.prefCommandExecutionRetriesSummary)
        attachStringSummaryListenerTo(PreferenceKeys.FHEMWEB_DEVICE_NAME, R.string.prefFHEMWEBDeviceNameSummary)

        val deviceColumnWidthPreference = findPreference(DEVICE_COLUMN_WIDTH) as SeekBarPreference
        deviceColumnWidthPreference.setMinimumValue(200)
        deviceColumnWidthPreference.setDefaultValue(DeviceListFragment.DEFAULT_COLUMN_WIDTH)
        deviceColumnWidthPreference.setMaximumValue(800)

        findPreference(SEND_LAST_ERROR).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            ErrorHolder.sendLastErrorAsMail(activity)
            true
        }

        findPreference(SEND_APP_LOG).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            ErrorHolder.sendApplicationLogAsMail(activity)
            true
        }

        findPreference(CLEAR_TRUSTED_CERTIFICATES).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val mtm = MemorizingTrustManager(activity)
            val aliases = mtm.certificates
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                try {
                    mtm.deleteCertificate(alias)
                    LOGGER.log(Level.INFO, "Deleting certificate for {} ", alias)
                } catch (e: KeyStoreException) {
                    LOGGER.log(Level.SEVERE, "Could not delete certificate", e)
                }

            }
            Toast.makeText(activity, getString(R.string.prefClearTrustedCertificatesFinished), Toast.LENGTH_SHORT).show()
            true
        }

        val exportPreference = findPreference(EXPORT_SETTINGS)
        exportPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            importExportUIService.handleExport(activity)
            true
        }
        if (AndFHEMApplication.androidSDKLevel <= Build.VERSION_CODES.KITKAT) {
            preferenceScreen.removePreference(exportPreference)
        }

        val importPreference = findPreference(IMPORT_SETTINGS)
        importPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            importExportUIService.handleImport(activity)
            true
        }
        if (AndFHEMApplication.androidSDKLevel <= Build.VERSION_CODES.KITKAT) {
            preferenceScreen.removePreference(importPreference)
        }

        val connectionTimeoutPreference = findPreference(CONNECTION_TIMEOUT) as SeekBarPreference
        connectionTimeoutPreference.setMinimumValue(1)
        connectionTimeoutPreference.setDefaultValue(CONNECTION_TIMEOUT_DEFAULT_SECONDS)

        val commandExecutionRetriesPreference = findPreference(COMMAND_EXECUTION_RETRIES) as SeekBarPreference
        commandExecutionRetriesPreference.setDefaultValue(CommandExecutionService.Companion.DEFAULT_NUMBER_OF_RETRIES)

        val voiceCommands = findPreference(PreferenceKeys.VOICE_COMMANDS)
        voiceCommands.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            true
        }
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(activity).unregisterOnSharedPreferenceChangeListener(this)

        activity.sendBroadcast(Intent(Actions.REDRAW))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {}

    private fun attachIntSummaryListenerTo(preferenceKey: String, summaryTemplate: Int) {
        val preference = findPreference(preferenceKey)
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
            pref.summary = String.format(getString(summaryTemplate), newValue)
            true
        }
        preference.summary = String.format(getString(summaryTemplate), applicationProperties.getIntegerSharedPreference(preferenceKey, 0, activity))
    }

    private fun attachStringSummaryListenerTo(preferenceKey: String, summaryTemplate: Int, listener: Preference.OnPreferenceChangeListener? = null) {
        val preference = findPreference(preferenceKey)
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
            pref.summary = String.format(getString(summaryTemplate), newValue)

            listener == null || listener.onPreferenceChange(pref, newValue)
        }
        preference.summary = String.format(getString(summaryTemplate), firstNonNull(applicationProperties.getStringSharedPreference(preferenceKey, null, activity), ""))
    }

    private fun attachListSummaryListenerTo(preferenceKey: String, valuesArrayResource: Int, textArrayResource: Int, summaryTemplate: Int) {
        val preference = findPreference(preferenceKey)
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
            pref.summary = nameForArrayValueFormatted(valuesArrayResource, textArrayResource, newValue.toString(), summaryTemplate)
            true
        }
        preference.summary = nameForArrayValueFormatted(valuesArrayResource, textArrayResource,
                applicationProperties.getStringSharedPreference(preferenceKey, activity), summaryTemplate)
    }

    private fun nameForArrayValueFormatted(valuesArrayResource: Int, textArrayResource: Int, value: String, summaryTemplate: Int): String =
            String.format(getString(summaryTemplate), nameForArrayValue(valuesArrayResource, textArrayResource, value))

    private fun nameForArrayValue(valuesArrayResource: Int, textArrayResource: Int, value: String): String? {
        val index = ArrayUtils.indexOf(resources.getStringArray(valuesArrayResource), value)
        if (index == -1) {
            return null
        }

        return resources.getStringArray(textArrayResource)[index]
    }

    companion object {
        private val LOGGER = Logger.getLogger(SettingsFragment::class.java.name)
    }
}