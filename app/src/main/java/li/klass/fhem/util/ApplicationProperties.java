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

package li.klass.fhem.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.AndFHEMBase;
import li.klass.fhem.fhem.ConnectionType;
import li.klass.fhem.fhem.DataConnectionSwitch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class ApplicationProperties {
    public static final ApplicationProperties INSTANCE = new ApplicationProperties();
    public static final String TAG = ApplicationProperties.class.getName();

    private final Properties properties = new Properties();

    private ApplicationProperties() {
        loadApplicationProperties();
    }

    private void loadApplicationProperties() {
        load("application.properties");
        load("billing.properties");
    }

    private void load(String fileName) {
        try {
            URL resource = AndFHEMBase.class.getResource(fileName);
            if (resource == null) {
                Log.i(TAG, "cannot load " + fileName + " (not found)");
            } else {
                load(resource.openStream());
            }
        } catch (IOException e) {
            Log.e(TAG, "cannot load " + fileName, e);
        }
    }

    void load(InputStream inputStream) {
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "cannot load application.properties", e);
        }
    }

    public String getStringApplicationProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean getBooleanSharedPreference(String key, boolean defaultValue) {
        SharedPreferences preferences = getPreferences();
        return preferences.getBoolean(key, defaultValue);
    }

    public void setBooleanSharedPreference(String key, boolean value) {
        SharedPreferences preferences = getPreferences();
        preferences.edit().putBoolean(key, value).commit();
    }

    public int getIntegerSharedPreference(String key, int defaultValue) {
        SharedPreferences preferences = getPreferences();
        return preferences.getInt(key, defaultValue);
    }

    public String getStringSharedPreference(String key, String defaultValue) {
        SharedPreferences preferences = getPreferences();
        return preferences.getString(key, defaultValue);
    }

    public void setSharedPreference(String key, boolean value) {
        SharedPreferences preferences = getPreferences();
        preferences.edit().putBoolean(key, value).commit();
    }

    public void setSharedPreference(String key, String value) {
        SharedPreferences preferences = getPreferences();
        preferences.edit().putString(key, value).commit();
    }

    public void deleteSharedPreference(String key) {
        SharedPreferences preferences = getPreferences();
        preferences.edit().remove(key);
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
    }

    public ConnectionType getConnectionType() {
        try {
            String connectionType = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext())
                    .getString(DataConnectionSwitch.CONNECTION_TYPE, "DUMMYDATA");
            if (connectionType.equals("")) connectionType = "DUMMYDATA";
            Log.v(TAG, "returning " + connectionType + " as current connection type");
            return ConnectionType.valueOf(connectionType.toUpperCase());
        } catch (Exception e) {
            Log.e(TAG, "error occurred while loading connection type", e);
            return ConnectionType.DUMMYDATA;
        }
    }
}
