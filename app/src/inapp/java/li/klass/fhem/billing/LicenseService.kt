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

package li.klass.fhem.billing

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.security.cert.X509Certificate

@Singleton
class LicenseService @Inject constructor(
        private val application: Application
) {

    fun isDebug(): Boolean {
        try {
            val pkgInfo = applicationContext.packageManager
                    .getPackageInfo(applicationContext.packageName, PackageManager.GET_SIGNATURES)

            pkgInfo.signatures
                .map { X509Certificate.getInstance(it.toByteArray()) }
                .any { it.subjectDN.name.contains("Android Debug") }
        } catch (e: Exception) {
            LOGGER.error("isDebug() : some exception occurred while reading app signatures", e)
        }

        return false
    }

    private val applicationContext: Context get() = application.applicationContext

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LicenseService::class.java)
    }
}
