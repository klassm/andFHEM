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
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.billing.BillingConstants;
import li.klass.fhem.billing.BillingProvider;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class PlayStoreProvider extends PlayStorePurchaseObserver implements BillingProvider {
    public static final String TAG = PlayStoreProvider.class.getName();

    private PlayStoreBillingService billingService;
    private volatile boolean isRestoreDatabaseRequestInProgress = false;

    public static final PlayStoreProvider INSTANCE = new PlayStoreProvider();

    private PlayStoreProvider() {
        super(new Handler());
    }

    @Override
    public void requestPurchase(String productId, String payload) {
        if (billingService == null) return;
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
        return billingService != null && billingService.checkBillingSupported();
    }

    @Override
    public boolean hasPendingRequestFor(String productId) {
        return billingService != null && billingService.hasPendingRequestFor(productId);
    }

    @Override
    public void onActivityUpdate() {
        restoreDatabase();
    }

    @Override
    public void rebuildDatabaseFromRemote() {
        restoreDatabase();
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
        Log.e(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);

        BillingService.INSTANCE.markProductAsPurchased("O" + purchaseTime, itemId, purchaseState, purchaseTime, developerPayload);
        doUpdate();
    }

    private void doUpdate() {
        Intent intent = new Intent(Actions.DO_UPDATE);
        AndFHEMApplication.getContext().sendBroadcast(intent);
    }

    @Override
    public void onRequestPurchaseResponse(PlayStoreBillingService.RequestPurchase request, BillingConstants.ResponseCode responseCode) {
        Log.i(TAG, "request purchase response: " + responseCode.name());
        doUpdate();
    }

    @Override
    public void onRestoreTransactionsResponse(PlayStoreBillingService.RestoreTransactions request, BillingConstants.ResponseCode responseCode) {
        Log.e(TAG, "restore transactions response with result " + responseCode.name());
        isRestoreDatabaseRequestInProgress = false;

        if (responseCode != BillingConstants.ResponseCode.RESULT_OK) {
            return;
        }

        BillingService.INSTANCE.setBillingDatabaseInitialised(true);
        doUpdate();
    }

    private void restoreDatabase() {
        if (billingService == null || BillingService.INSTANCE.isBillingDatabaseInitialised()
                || isRestoreDatabaseRequestInProgress) {
            return;
        }

        Log.e(TAG, "execute restore database");

        isRestoreDatabaseRequestInProgress = true;
        billingService.restoreTransactions();

        Intent intent = new Intent(Actions.SHOW_TOAST);
        intent.putExtra(BundleExtraKeys.STRING_ID, R.string.billing_restoringTransactions);
        AndFHEMApplication.getContext().sendBroadcast(intent);
    }
}
