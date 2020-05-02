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
package li.klass.fhem.domain

import android.util.Log
import java.util.*

enum class CulHmHeatingMode {
    MANUAL, AUTO, CENTRAL, BOOST, UNKNOWN;

    companion object {
        fun heatingModeFor(value: String?): CulHmHeatingMode? {
            // If the command is not confirmed yet FHEM sets the state to the target state with the "SET_" prefix.
            // We assume that the command goes well and remove the prefix ...
            var replaced = value?.toUpperCase(Locale.getDefault())?.replace("SET_", "")
                    ?: return null
            if (replaced.equals("MANU", ignoreCase = true)) {
                replaced = MANUAL.name
            }
            return try {
                valueOf(replaced)
            } catch (e: Exception) {
                Log.e(CulHmHeatingMode::class.java.name, "cannot set heating mode from value $replaced", e)
                null
            }
        }
    }
}