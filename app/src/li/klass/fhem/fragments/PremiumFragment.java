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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.core.BaseFragment;

import java.util.Set;

public class PremiumFragment extends BaseFragment {

    private static final String TAG = PremiumFragment.class.getName();

    @SuppressWarnings("unused")
    public PremiumFragment(Bundle bundle) {
    }

    @SuppressWarnings("unused")
    public PremiumFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onResume();
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
                BillingService.INSTANCE.requestPurchase(AndFHEMApplication.PRODUCT_PREMIUM_ID, null);
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

        Set<String> ownedItems = BillingService.INSTANCE.getOwnedItems();
        if (ownedItems.contains(AndFHEMApplication.PRODUCT_PREMIUM_ID)) {
            view.findViewById(R.id.shop_premium_bought).setVisibility(View.VISIBLE);
        } else if(BillingService.INSTANCE.hasPendingRequestFor(AndFHEMApplication.PRODUCT_PREMIUM_ID)) {
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
        BillingService.INSTANCE.bindActivity(getActivity());
        if (! BillingService.INSTANCE.isBillingSupported()) {
            showBillingNotSupportedToast();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BillingService.INSTANCE.unbindActivity(getActivity());
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
