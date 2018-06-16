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

package li.klass.fhem.activities

import android.os.Build
import android.support.v7.app.AppCompatDelegate
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties
import javax.inject.Inject

class ThemeInitializer @Inject constructor(val applicationProperties: ApplicationProperties) {
    fun init() {
        AppCompatDelegate.setDefaultNightMode(getMode())
    }

    private fun getMode(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        return applicationProperties.getStringSharedPreference(SettingsKeys.THEME)
                ?.let {
                    when (it) {
                        "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
                        "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
                        else -> AppCompatDelegate.MODE_NIGHT_AUTO
                    }
                } ?: AppCompatDelegate.MODE_NIGHT_AUTO
    }
}