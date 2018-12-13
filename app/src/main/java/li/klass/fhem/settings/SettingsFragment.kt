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

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.AndFHEMApplication.Companion.application
import li.klass.fhem.settings.type.SettingsTypeHandler
import li.klass.fhem.settings.type.SettingsTypeHandlers
import javax.inject.Inject

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var typeHandlers: SettingsTypeHandlers

    private val typeHandler: SettingsTypeHandler
        get() = typeHandlers.handlerFor(arguments.getString("type")!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AndFHEMApplication).daggerComponent.inject(this)

        super.onCreate(savedInstanceState)

        addPreferencesFromResource(typeHandler.getResource())
        typeHandler.initializeWith(PreferenceManager.getDefaultSharedPreferences(activity),
                { preferenceKey: String -> findPreference(preferenceKey) }, activity)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preference = preferenceManager.findPreference(key)
        preference ?: return
        typeHandler.onPreferenceChange(sharedPreferences, preference)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}