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

package li.klass.fhem.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationProperties @Inject
constructor(private val application: Application) {

    private val preferences: SharedPreferences?
        get() = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)

    fun getBooleanSharedPreference(key: String, defaultValue: Boolean): Boolean {
        val preferences = preferences
        return preferences?.getBoolean(key, defaultValue) ?: defaultValue
    }

    fun getIntegerSharedPreference(key: String, defaultValue: Int): Int {
        val preferences = preferences
        return preferences?.getInt(key, defaultValue) ?: defaultValue
    }

    fun getApplicationSharedPreferencesName(context: Context): String =
            context.packageName + "_preferences"

    @JvmOverloads
    fun getStringSharedPreference(key: String, defaultValue: String? = null): String? {
        val preferences = preferences
        val value = if (preferences == null) defaultValue else preferences.getString(key, defaultValue)
        return if (value.isNullOrEmpty()) {
            defaultValue
        } else {
            value
        }
    }

    fun setSharedPreference(key: String, value: String) {
        val preferences = preferences ?: return
        preferences.edit().putString(key, value).apply()
    }
}
