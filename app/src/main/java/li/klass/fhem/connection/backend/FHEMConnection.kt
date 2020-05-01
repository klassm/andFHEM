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
package li.klass.fhem.connection.backend

import android.content.Context
import li.klass.fhem.error.ErrorHolder.setError
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties

abstract class FHEMConnection(var server: FHEMServerSpec, protected var applicationProperties: ApplicationProperties) {
    abstract fun executeCommand(command: String, context: Context): RequestResult<String>

    protected fun setErrorInErrorHolderFor(e: Exception?, host: String, suffix: String) {
        val text = """
            Error while accessing '$host' with suffix '$suffix'
            $server

            """.trimIndent()
        setError(e, text)
    }

    val connectionTimeoutMilliSeconds: Int
        get() = 1000 * applicationProperties.getIntegerSharedPreference(
                SettingsKeys.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT_DEFAULT_SECONDS
        )

    companion object {
        const val CONNECTION_TIMEOUT_DEFAULT_SECONDS = 4
    }

}