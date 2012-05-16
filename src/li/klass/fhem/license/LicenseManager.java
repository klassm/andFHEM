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
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.billing.PurchaseDatabase;

import javax.security.cert.X509Certificate;
import java.util.Set;

public class LicenseManager {
    public static final LicenseManager INSTANCE = new LicenseManager();
    private PurchaseDatabase purchaseDatabase;

    private LicenseManager() {
        purchaseDatabase = new PurchaseDatabase(AndFHEMApplication.getContext());
    }
    public boolean isPro() {
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
            Log.e(LicenseManager.class.getName(), "some exception occurred during reading of app signatures", e);
        }
        Set<String> ownedItems = purchaseDatabase.getOwnedItems();
        return ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_ID) || ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_DONATOR_ID);
    }
}
