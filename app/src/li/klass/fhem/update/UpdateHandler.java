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

package li.klass.fhem.update;

import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.util.ApplicationProperties;

public class UpdateHandler {
    public static final UpdateHandler INSTANCE = new UpdateHandler();

    private UpdateHandler() {
    }

    private static final String TAG = UpdateHandler.class.getName();

    public void onUpdate() {
        AndFHEMApplication application = AndFHEMApplication.INSTANCE;
        if (!application.isUpdate()) return;

        fixInvalidPurchases();
    }

    private void fixInvalidPurchases() {
        if (!ApplicationProperties.INSTANCE.getBooleanSharedPreference(PreferenceKeys.FIX_INVALID_PURCHASES, false)) {

            Log.e(TAG, "execute fix invalid purchases");

            BillingService.INSTANCE.registerBeforeProductPurchasedListener(new BillingService.BeforeProductPurchasedListener() {
                @Override
                public void productPurchased(String orderId, String productId) {
                    BillingService.INSTANCE.clearDatabase();
                    BillingService.INSTANCE.removeBeforeProductPurchasedListener(this);
                    ApplicationProperties.INSTANCE.setSharedPreference(PreferenceKeys.FIX_INVALID_PURCHASES, true);

                    Log.e(TAG, "fix invalid purchases fix executed!");
                }
            });
            ApplicationProperties.INSTANCE.setSharedPreference(PreferenceKeys.BILLING_DATABASE_INITIALISED, false);
            BillingService.INSTANCE.rebuildDatabaseFromRemote();
        }
    }

}
