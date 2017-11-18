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

package li.klass.fhem.util.preferences

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences

import org.slf4j.LoggerFactory

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesService @Inject
constructor(val application: Application) {

    fun listAllFrom(fileName: String): Map<String, *> =
            getPreferences(fileName).all

    fun writeAllIn(preferenceName: String, toWrite: Map<String, *>) {
        LOGGER.info("writeAllIn({}) - containing {} entries", preferenceName, toWrite.size)
        val preferences = getPreferences(preferenceName)
        val editor = preferences.edit()
        editor.clear()
        for ((key, value) in toWrite) {
            when (value) {
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is String -> editor.putString(key, value)
                else -> throw IllegalArgumentException("don't know how to handle " + value)
            }

            LOGGER.debug("writeAllIn({}) - imported key={}, value={}", preferenceName, key, value)
        }
        editor.apply()
    }

    fun getSharedPreferencesEditor(preferencesName: String): SharedPreferences.Editor {
        val sharedPreferences = getPreferences(preferencesName)
        return sharedPreferences.edit()
    }

    fun getPreferences(preferenceName: String): SharedPreferences =
            applicationContext.getSharedPreferences(preferenceName, Activity.MODE_PRIVATE)

    private val applicationContext: Context get() = application.applicationContext

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SharedPreferencesService::class.java)
    }
}
