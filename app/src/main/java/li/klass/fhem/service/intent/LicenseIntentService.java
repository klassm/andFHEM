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

package li.klass.fhem.service.intent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.ResultReceiver;
import android.util.Log;

import javax.inject.Inject;
import javax.security.cert.X509Certificate;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.AndFHEMApplication.PREMIUM_PACKAGE;
import static li.klass.fhem.constants.BundleExtraKeys.IS_PREMIUM;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class LicenseIntentService extends ConvenientIntentService {

    private static final String TAG = LicenseIntentService.class.getName();

    @Inject
    @ForApplication
    Context applicationContext;

    @Inject
    BillingService billingService;

    @Inject
    ApplicationProperties applicationProperties;

    public LicenseIntentService() {
        super(LicenseIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        if (Actions.IS_PREMIUM.equals(action)) {
            handlePremiumRequest(resultReceiver);
            return STATE.DONE;
        } else {
            return STATE.DONE;
        }
    }

    private void handlePremiumRequest(final ResultReceiver resultReceiver) {

        isPremium(new IsPremiumListener() {
            @Override
            public void isPremium(boolean isPremium) {
                sendSingleExtraResult(resultReceiver, SUCCESS, IS_PREMIUM, isPremium);
            }
        });
    }

    public void isPremium(final IsPremiumListener listener) {
        billingService.loadInventory(new BillingService.OnLoadInventoryFinishedListener() {
            @Override
            public void onInventoryLoadFinished(boolean success) {

                boolean isPremium = isPremiumInternal();
                listener.isPremium(isPremium);
            }
        });
    }

    private boolean isPremiumInternal() {
        boolean isPremium = false;

        if (applicationProperties.getBooleanApplicationProperty("IS_PREMIUM")) {
            Log.i(TAG, "found IS_PREMIUM application property to be true => premium");
            isPremium = true;
        } else if (applicationContext.getPackageName().equals(PREMIUM_PACKAGE)) {
            Log.i(TAG, "found package name to be " + PREMIUM_PACKAGE + " => premium");
            isPremium = true;
        } else if (isDebug(applicationContext)) {
            Log.i(TAG, "running in debug => premium");
            isPremium = true;
        } else if (billingService.contains(AndFHEMApplication.INAPP_PREMIUM_ID) ||
                billingService.contains(AndFHEMApplication.INAPP_PREMIUM_DONATOR_ID)) {
            Log.i(TAG, "found inapp premium purchase => premium");
            isPremium = true;
        } else {
            Log.i(TAG, "seems that I am not Premium...");
        }
        return isPremium;
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
            Log.e(LicenseIntentService.class.getName(), "isDebug() : some exception occurred while reading app signatures", e);
        }
        return false;
    }

    public interface IsPremiumListener {
        void isPremium(boolean isPremium);
    }
}
