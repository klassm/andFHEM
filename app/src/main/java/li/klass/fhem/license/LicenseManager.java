/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.license;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.util.Set;

import javax.security.cert.X509Certificate;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.util.ApplicationProperties;

public class LicenseManager {
    public static final LicenseManager INSTANCE = new LicenseManager();
    private final static boolean isPremium = false;

    private LicenseManager() {
    }

    public boolean isPro() {
        if (isPremium) return true;
        if (ApplicationProperties.INSTANCE.getBooleanApplicationProperty("IS_PREMIUM")) return true;
        if (AndFHEMApplication.getContext().getPackageName().equals("li.klass.fhempremium")) {
            return true;
        }
        if (isDebug()) return true;

        Set<String> ownedItems = BillingService.INSTANCE.getOwnedItems();
        return ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_ID) ||
                ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_DONATOR_ID);
    }

    public boolean isDebug() {
        try {
            Context context = AndFHEMApplication.getContext();
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature appSignature : pkgInfo.signatures) {
                X509Certificate appCertificate = X509Certificate.getInstance(appSignature.toByteArray());
                if (appCertificate.getSubjectDN().getName().contains("Android Debug")) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(LicenseManager.class.getName(), "some exception occurred while reading app signatures", e);
        }
        return false;
    }
}
