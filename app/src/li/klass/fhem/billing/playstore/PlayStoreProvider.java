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

package li.klass.fhem.billing.playstore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.billing.BillingConstants;
import li.klass.fhem.billing.BillingProvider;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class PlayStoreProvider extends PlayStorePurchaseObserver implements BillingProvider {
    private PlayStoreBillingService billingService;
    private static final String BILLING_PREFERENCE = "li.klass.fhem.billing";
    private static final String DB_INITIALIZED = "billingDbInitialised";

    public static final String TAG = PlayStoreProvider.class.getName();


    public static final PlayStoreProvider INSTANCE = new PlayStoreProvider();
    private PlayStoreProvider() {
        super(new Handler());
    }

    @Override
    public void requestPurchase(String productId, String payload) {
        billingService.requestPurchase(productId, payload);
    }

    @Override
    public void bindActivity(Activity activity) {
        super.bindActivity(activity);
        PlayStoreResponseHandler.register(this);
        billingService = new PlayStoreBillingService();
        billingService.setContext(activity);
    }

    @Override
    public void unbindActivity(Activity activity) {
        PlayStoreResponseHandler.unregister(this);
        if (billingService != null) billingService.unbind();
    }

    @Override
    public boolean isBillingSupported() {
        return billingService.checkBillingSupported();
    }

    @Override
    public boolean hasPendingRequestFor(String productId) {
        return billingService.hasPendingRequestFor(productId);
    }

    @Override
    public void onBillingSupported(boolean supported) {
        Log.i(TAG, "billing is " + (supported ? "" : "not ") + "supported");
        if (supported) {
            restoreDatabase();
        }
    }

    @Override
    public void onPurchaseStateChange(BillingConstants.PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload) {
        Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
        doUpdate();
    }

    private void doUpdate() {
        Intent intent = new Intent(Actions.DO_UPDATE);
        if (activity != null) {
            activity.sendBroadcast(intent);
        }
    }

    @Override
    public void onRequestPurchaseResponse(PlayStoreBillingService.RequestPurchase request, BillingConstants.ResponseCode responseCode) {
        Log.i(TAG, "request purchase response: " + responseCode.name());
        doUpdate();
    }

    @Override
    public void onRestoreTransactionsResponse(PlayStoreBillingService.RestoreTransactions request, BillingConstants.ResponseCode responseCode) {
        Log.i(TAG, "restore transactions response with result" + responseCode.name());
        if (responseCode != BillingConstants.ResponseCode.RESULT_OK) {
            return;
        }

        SharedPreferences billingPreferences = getBillingPreferences();
        SharedPreferences.Editor edit = billingPreferences.edit();
        edit.putBoolean(DB_INITIALIZED, true);
        edit.commit();

        doUpdate();
    }

    private void restoreDatabase() {
        SharedPreferences preferences = getBillingPreferences();
        boolean initialized = preferences.getBoolean(DB_INITIALIZED, false);
        if (!initialized) {
            billingService.restoreTransactions();

            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.TOAST_STRING_ID, R.string.billing_restoringTransactions);
            if (activity != null) activity.sendBroadcast(intent);
        }
    }

    private SharedPreferences getBillingPreferences() {
        return AndFHEMApplication.getContext().getSharedPreferences(BILLING_PREFERENCE, Context.MODE_PRIVATE);
    }
}
