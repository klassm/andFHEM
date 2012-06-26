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

package li.klass.fhem.billing.amazon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.amazon.inapp.purchasing.PurchasingManager;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.billing.BillingProvider;

import java.util.Map;

public class AmazonBillingProvider implements BillingProvider {

    private static final String REQUEST_PREFERENCES = AmazonBillingProvider.class.getName() + "-request";

    public static final AmazonBillingProvider INSTANCE = new AmazonBillingProvider();

    private AmazonBillingProvider() {
        PurchasingManager.registerObserver(AmazonPurchasingObserver.INSTANCE);
    }

    void removePurchaseRequest(String requestId) {
        getRequestPreferences().edit().remove(requestId).commit();
    }

    @Override
    public void requestPurchase(String productId, String payload) {
        String requestId = PurchasingManager.initiatePurchaseRequest(productId);
        getRequestPreferences().edit().putString(requestId, productId).commit();
    }

    @Override
    public void bindActivity(Activity activity) {
    }

    @Override
    public void unbindActivity(Activity activity) {
    }

    @Override
    public boolean isBillingSupported() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasPendingRequestFor(String productId) {
        Map<String, String> entries = (Map<String, String>) getRequestPreferences().getAll();
        for (String value : entries.values()) {
            if (value.equals(productId)) return true;
        }
        return false;
    }

    private SharedPreferences getRequestPreferences() {
        return AndFHEMApplication.getContext().getSharedPreferences(REQUEST_PREFERENCES, Context.MODE_PRIVATE);
    }
}
