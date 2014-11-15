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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.io.Closer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.dagger.ForApplication;

import static com.google.common.base.Strings.isNullOrEmpty;

@Singleton
public class ApplicationProperties {
    public static final String TAG = ApplicationProperties.class.getName();

    private final Properties properties = new Properties();

    @Inject
    @ForApplication
    Context applicationContext;

    private boolean isLoaded = false;

    public void load() {
        if (!isLoaded) {
            load("/application.properties");
            isLoaded = true;
        }
    }

    private void load(String fileName) {
        URL resource = AndFHEMApplication.class.getResource(fileName);
        if (resource == null) {
            Log.i(TAG, "cannot load " + fileName + " (not found)");
        } else {
            Log.i(TAG, "loading " + resource.getPath());
            try {
                load(resource);
            } catch (Exception e) {
                Log.e(TAG, "error while loading file", e);
            }
            Log.i(TAG, "load completed, now contains " + properties.size() + " properties");
        }
    }

    void load(URL url) throws Exception {
        Closer closer = Closer.create();
        InputStream stream = url.openStream();
        closer.register(stream);
        try {
            properties.load(stream);
        } catch (IOException e) {
            Log.e(TAG, "error while loading url", e);
        } finally {
            closer.close();
        }
    }

    public boolean getBooleanApplicationProperty(String key) {
        load();
        String value = getStringApplicationProperty(key);
        return value != null ? Boolean.valueOf(value) : false;
    }

    public String getStringApplicationProperty(String key) {
        load();
        return properties.getProperty(key);
    }

    public boolean getBooleanSharedPreference(String key, boolean defaultValue) {
        SharedPreferences preferences = getPreferences();
        return preferences.getBoolean(key, defaultValue);
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext);
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
}
