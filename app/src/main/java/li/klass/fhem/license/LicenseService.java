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

package li.klass.fhem.license;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.cert.X509Certificate;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.util.ApplicationProperties;

@Singleton
public class LicenseService {
    private static final String TAG = LicenseService.class.getName();

    @Inject
    @ForApplication
    Context applicationContext;

    @Inject
    BillingService billingService;

    @Inject
    ApplicationProperties applicationProperties;

    public void isPremium(final IsPremiumListener isPremiumListener) {

        billingService.getOwnedItems(new BillingService.OwnedItemsLoadedListener() {
            @Override
            public void onItemsLoaded(Set<String> ownedItems, boolean isInitialized) {
                boolean isPremium = !isInitialized;

                if (applicationProperties.getBooleanApplicationProperty("IS_PREMIUM")) {
                    Log.i(TAG, "found IS_PREMIUM application property to be true => premium");
                    isPremium = true;
                } else if (isPremiumApk()) {
                    Log.i(TAG, "found package name to be li.klass.fhempremium => premium");
                    isPremium = true;
                } else if (isDebug()) {
                    Log.i(TAG, "running in debug => premium");
                    isPremium = true;
                } else if (ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_ID) ||
                        ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_DONATOR_ID)) {
                    Log.i(TAG, "found inapp premium purchase => premium");
                    isPremium = true;
                } else {
                    Log.i(TAG, "seems that I am not Premium...");
                }

                isPremiumListener.onIsPremiumDetermined(isPremium);
            }
        });
    }

    public boolean isPremiumApk() {
        return applicationContext.getPackageName().equals("li.klass.fhempremium");
    }

    public boolean isDebug() {
        try {
            PackageInfo pkgInfo = applicationContext.getPackageManager()
                    .getPackageInfo(applicationContext.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature appSignature : pkgInfo.signatures) {
                X509Certificate appCertificate = X509Certificate.getInstance(appSignature.toByteArray());
                if (appCertificate.getSubjectDN().getName().contains("Android Debug")) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(LicenseService.class.getName(), "some exception occurred while reading app signatures", e);
        }
        return false;
    }

    public interface IsPremiumListener {
        void onIsPremiumDetermined(boolean isPremium);
    }
}
