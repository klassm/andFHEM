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

package li.klass.fhem.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.billing.*;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.core.BaseFragment;

import java.util.Set;

public class PremiumFragment extends BaseFragment {

    private static final String BILLING_PREFERENCE = "li.klass.fhem.billing";
    private static final String DB_INITIALIZED = "billingDbInitialised";


    private transient BillingService billingService;
    private transient PurchaseDatabase purchaseDatabase;
    private transient BillingObserver billingObserver;
    private static final String TAG = PremiumFragment.class.getName();

    private class BillingObserver extends PurchaseObserver {

        public BillingObserver(Activity activity, Handler handler) {
            super(activity, handler);
        }

        @Override
        public void onBillingSupported(boolean supported) {
            Log.i(TAG, "billing supported: " + supported);
            if (supported) {
                restoreDatabase();
            } else {
                showBillingNotSupportedToast();
            }
        }

        @Override
        public void onPurchaseStateChange(BillingConstants.PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload) {
            Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
            update(false);
        }

        @Override
        public void onRequestPurchaseResponse(BillingService.RequestPurchase request, BillingConstants.ResponseCode responseCode) {
            Log.i(TAG, "request purchase response: " + responseCode.name());
        }

        @Override
        public void onRestoreTransactionsResponse(BillingService.RestoreTransactions request, BillingConstants.ResponseCode responseCode) {
            if (responseCode != BillingConstants.ResponseCode.RESULT_OK) {
                Log.i(TAG, "RestoreTransactions error: " + responseCode);
                return;
            }
            Log.i(TAG, "completed RestoreTransactions request");

            SharedPreferences billingPreferences = getBillingPreferences();
            SharedPreferences.Editor edit = billingPreferences.edit();
            edit.putBoolean(DB_INITIALIZED, true);
            edit.commit();

            update(false);
        }
    }

    @SuppressWarnings("unused")
    public PremiumFragment(Bundle bundle) {
    }

    @SuppressWarnings("unused")
    public PremiumFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        billingService = new BillingService();
        billingService.setContext(getActivity());
        purchaseDatabase = new PurchaseDatabase(getActivity());
        Handler handler = new Handler();
        billingObserver = new BillingObserver(getActivity(), handler);

        ResponseHandler.register(billingObserver);
        if (!billingService.checkBillingSupported()) {
            showBillingNotSupportedToast();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.shop_premium, null);
        view.findViewById(R.id.shop_premium_bought).setVisibility(View.GONE);
        view.findViewById(R.id.shop_premium_pending).setVisibility(View.GONE);
        view.findViewById(R.id.shop_premium_buy).setVisibility(View.GONE);

        view.findViewById(R.id.shop_premium_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "request purchase for product " + AndFHEMApplication.PRODUCT_PREMIUM_ID);
                billingService.requestPurchase(AndFHEMApplication.PRODUCT_PREMIUM_ID, null);
            }
        });
        update(view);

        return view;
    }

    @Override
    public void update(boolean doUpdate) {
        update(getView());
    }

    public void update(View view) {
        view.findViewById(R.id.shop_premium_bought).setVisibility(View.GONE);
        view.findViewById(R.id.shop_premium_pending).setVisibility(View.GONE);
        view.findViewById(R.id.shop_premium_buy).setVisibility(View.GONE);

        Set<String> ownedItems = purchaseDatabase.getOwnedItems();
        if (ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_ID)) {
            view.findViewById(R.id.shop_premium_bought).setVisibility(View.VISIBLE);
        } else if(billingService.hasPendingRequestFor(AndFHEMApplication.PRODUCT_PREMIUM_ID)) {
            view.findViewById(R.id.shop_premium_pending).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.shop_premium_buy).setVisibility(View.VISIBLE);
        }

        Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        ResponseHandler.register(billingObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        ResponseHandler.unregister(billingObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        purchaseDatabase.close();
        billingService.unbind();
    }

    private void restoreDatabase() {
        SharedPreferences preferences = getBillingPreferences();
        boolean initialized = preferences.getBoolean(DB_INITIALIZED, false);
        if (!initialized) {
            billingService.restoreTransactions();
            showToast(R.string.billing_restoringTransactions);
        }
    }

    private SharedPreferences getBillingPreferences() {
        return AndFHEMApplication.getContext().getSharedPreferences(BILLING_PREFERENCE, Context.MODE_PRIVATE);
    }

    private void showBillingNotSupportedToast() {
        showToast(R.string.billing_notSupported);
    }

    private void showToast(int toastString) {
        Intent intent = new Intent(Actions.SHOW_TOAST);
        intent.putExtra(BundleExtraKeys.TOAST_STRING_ID, toastString);
        getActivity().sendBroadcast(intent);
    }
}
