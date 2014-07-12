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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.AndFHEMBase;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApplicationProperties {
    public static final ApplicationProperties INSTANCE = new ApplicationProperties();
    public static final String TAG = ApplicationProperties.class.getName();

    private final Properties properties = new Properties();

    private ApplicationProperties() {
        load("application.properties");
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
            Log.e(TAG, "error while loading file", e);
        }
    }

    public String getStringApplicationProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean getBooleanApplicationProperty(String key) {
        String value = getStringApplicationProperty(key);
        return value != null ? Boolean.valueOf(value) : false;
    }


    public boolean getBooleanSharedPreference(String key, boolean defaultValue) {
        SharedPreferences preferences = getPreferences();
        return preferences.getBoolean(key, defaultValue);
    }

    public int getIntegerSharedPreference(String key, int defaultValue) {
        SharedPreferences preferences = getPreferences();
        return preferences.getInt(key, defaultValue);
    }

    public String getStringSharedPreference(String key, String defaultValue) {
        SharedPreferences preferences = getPreferences();
        String value = preferences.getString(key, defaultValue);
        if (isNullOrEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public void setSharedPreference(String key, String value) {
        SharedPreferences preferences = getPreferences();
        preferences.edit().putString(key, value).apply();
    }

    public void deleteSharedPreference(String key) {
        SharedPreferences preferences = getPreferences();
        preferences.edit().remove(key).apply();
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
    }
}
