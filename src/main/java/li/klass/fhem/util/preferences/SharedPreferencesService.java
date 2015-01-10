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

package li.klass.fhem.util.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.dagger.ForApplication;

@Singleton
public class SharedPreferencesService {
    @Inject
    @ForApplication
    Context context;

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedPreferencesService.class);

    public Map<String, ?> listAllFrom(String fileName) {
        return getPreferences(fileName).getAll();
    }

    public void writeAllIn(String preferenceName, Map<String, ?> toWrite) {
        LOGGER.info("writeAllIn({}) - containing {} entries", preferenceName, toWrite.size());
        SharedPreferences preferences = getPreferences(preferenceName);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        for (Map.Entry<String, ?> entry : toWrite.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof Float) {
                editor.putFloat(entry.getKey(), (Float) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(entry.getKey(), (Boolean) value);
            } else if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else {
                throw new IllegalArgumentException("don't know how to handle " + value);
            }

            LOGGER.debug("writeAllIn({}) - imported key={}, value={}", preferenceName, entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    private SharedPreferences getPreferences(String preferenceName) {
        return context.getSharedPreferences(preferenceName, Activity.MODE_PRIVATE);
    }
}
