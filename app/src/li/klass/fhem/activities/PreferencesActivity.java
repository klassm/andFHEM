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

import android.os.Bundle;
import android.preference.*;
import android.text.method.PasswordTransformationMethod;
import com.hlidskialf.android.preference.SeekBarPreference;
import li.klass.fhem.R;
import li.klass.fhem.adapter.rooms.DeviceGridAdapter;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.fhem.FHEMWebConnection.*;
import static li.klass.fhem.fhem.TelnetConnection.*;

public class PreferencesActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.preferences);

        ListPreference dataOriginPreference = (ListPreference) findPreference(DataConnectionSwitch.CONNECTION_TYPE);
        setDataOriginOptionsForValue(dataOriginPreference.getValue());
        dataOriginPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                setDataOriginOptionsForValue((String) o);
                return true;
            }
        });

        SeekBarPreference deviceColumnWidthPreference = (SeekBarPreference) findPreference(DeviceGridAdapter.COLUMN_WIDTH_PREFERENCE);
        deviceColumnWidthPreference.setMin(350);

    }

    private void setDataOriginOptionsForValue(String value) {
        removeAllDateOriginOptions();
        if (value.equals("TELNET")) {
            addTelnetPreferences();
        } else if (value.equals("FHEMWEB")) {
            addFHEMWEBPreferences();
        }
    }

    private void removeAllDateOriginOptions() {
        removePreferenceIfNotNull(TELNET_URL);
        removePreferenceIfNotNull(TELNET_PORT);
        removePreferenceIfNotNull(TELNET_PASSWORD);

        removePreferenceIfNotNull(FHEMWEB_URL);
        removePreferenceIfNotNull(FHEMWEB_USERNAME);
        removePreferenceIfNotNull(FHEMWEB_PASSWORD);

    }

    private void removePreferenceIfNotNull(String preferenceKey) {
        Preference preference = findPreference(preferenceKey);
        if (preference != null) {
            getDataOriginCategory().removePreference(preference);
        }
    }

    private void addTelnetPreferences() {
        EditTextPreference urlPreference = new EditTextPreference(this);
        urlPreference.setTitle(R.string.prefTelnetUrl);
        urlPreference.setSummary(R.string.prefTelnetUrlSummary);
        urlPreference.setKey(TELNET_URL);
        getDataOriginCategory().addPreference(urlPreference);

        EditTextPreference portPreference = new EditTextPreference(this);
        portPreference.setTitle(R.string.prefTelnetPort);
        portPreference.setSummary(R.string.prefTelnetPortSummary);
        portPreference.setKey(TELNET_PORT);
        getDataOriginCategory().addPreference(portPreference);

        EditTextPreference passwordPreference = new EditTextPreference(this);
        passwordPreference.setTitle(R.string.prefPassword);
        passwordPreference.setSummary(R.string.optional);
        passwordPreference.setKey(TELNET_PASSWORD);
        passwordPreference.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());

        getDataOriginCategory().addPreference(passwordPreference);
    }

    private void addFHEMWEBPreferences() {
        EditTextPreference urlPreference = new EditTextPreference(this);
        urlPreference.setTitle(R.string.prefFHEMWEBUrl);
        urlPreference.setSummary(R.string.prefFHEMWEBUrlSummary);
        urlPreference.setKey(FHEMWEB_URL);
        getDataOriginCategory().addPreference(urlPreference);

        urlPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String newValue = (String) o;
                if (! newValue.matches("http[s]?://.*")) {
                    DialogUtil.showAlertDialog(preference.getContext(), R.string.error ,R.string.prefUrlBeginningError);
                }
                return true;
            }
        });

        EditTextPreference usernamePreference = new EditTextPreference(this);
        usernamePreference.setTitle(R.string.prefUsername);
        usernamePreference.setKey(FHEMWEB_USERNAME);
        usernamePreference.setSummary(R.string.optional);
        getDataOriginCategory().addPreference(usernamePreference);

        EditTextPreference passwordPreference = new EditTextPreference(this);
        passwordPreference.setTitle(R.string.prefPassword);
        passwordPreference.setKey(FHEMWEB_PASSWORD);
        passwordPreference.setSummary(R.string.optional);
        passwordPreference.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
        getDataOriginCategory().addPreference(passwordPreference);
    }

    private PreferenceCategory getDataOriginCategory() {
        return (PreferenceCategory) findPreference("DATAORIGINCATEGORY");
    }
}