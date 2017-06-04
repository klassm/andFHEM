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

package li.klass.fhem.billing;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.cert.X509Certificate;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.AndFHEMApplication.PREMIUM_PACKAGE;

@Singleton
public class LicenseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseService.class);

    @Inject
    public LicenseService() {
    }

    public void isPremium(final IsPremiumListener listener, Context context) {
        listener.isPremium(true);
    }

    private boolean isPremiumInternal(boolean loadSuccessful, Context context) {
        return true;
    }

    public static boolean isDebug(Context context) {
        try {
            PackageInfo pkgInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature appSignature : pkgInfo.signatures) {
                X509Certificate appCertificate = X509Certificate.getInstance(appSignature.toByteArray());
                if (appCertificate.getSubjectDN().getName().contains("Android Debug")) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("isDebug() : some exception occurred while reading app signatures", e);
        }
        return false;
    }

    public interface IsPremiumListener {
        void isPremium(boolean isPremium);
    }
}
