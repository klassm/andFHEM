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
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.security.cert.X509Certificate

@Singleton
class LicenseService @Inject constructor(
        private val billingService: BillingService,
        private val applicationProperties: ApplicationProperties,
        private val application: Application
) {

    fun isPremium(listener: IsPremiumListener) {
        billingService.loadInventory({ success ->
            val isPremium = isPremiumInternal(success)
            listener.isPremium(isPremium)
        }, applicationContext)
    }

    private fun isPremiumInternal(loadSuccessful: Boolean): Boolean {
        var isPremium = false

        // careful: We need an application context here, as LicenseIntentService (this?!) is null
        // when invoking getPackageName!
        if (applicationProperties.getBooleanApplicationProperty("IS_PREMIUM")) {
            LOGGER.info("found IS_PREMIUM application property to be true => premium")
            isPremium = true
        } else if (applicationContext.packageName == AndFHEMApplication.PREMIUM_PACKAGE) {
            LOGGER.info("found package name to be " + AndFHEMApplication.PREMIUM_PACKAGE + " => premium")
            isPremium = true
        } else if (isDebug()) {
            LOGGER.info("running in debug => premium")
            isPremium = true
        } else if (loadSuccessful && (billingService.contains(AndFHEMApplication.INAPP_PREMIUM_ID) || billingService.contains(AndFHEMApplication.INAPP_PREMIUM_DONATOR_ID))) {
            LOGGER.info("found inapp premium purchase => premium")
            isPremium = true
        } else {
            LOGGER.info("seems that I am not Premium...")
        }
        return isPremium
    }

    fun isDebug(): Boolean {
        try {
            val pkgInfo = applicationContext.packageManager
                    .getPackageInfo(applicationContext.packageName, PackageManager.GET_SIGNATURES)

            pkgInfo.signatures
                    .map { X509Certificate.getInstance(it.toByteArray()) }
                    .filter { it.subjectDN.name.contains("Android Debug") }
                    .forEach { return true }
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
