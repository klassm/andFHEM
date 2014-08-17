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

package li.klass.fhem.billing;

import android.app.Activity;
import android.util.Log;

import com.android.vending.billing.IabException;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import li.klass.fhem.AndFHEMApplication;

import static com.google.common.base.Preconditions.checkNotNull;

public class BillingService {

    public static final String TAG = BillingService.class.getName();
    public static final BillingService INSTANCE = new BillingService();
    private IabHelper iabHelper;
    private AtomicReference<Inventory> inventory = new AtomicReference<Inventory>(Inventory.empty());

    private BillingService() {
    }

    public synchronized void stop() {
        iabHelper.dispose();
    }

    public synchronized void requestPurchase(final Activity activity, final String itemId,
                                             final String payload,
                                             final ProductPurchasedListener listener) {
        Log.i(TAG, "requesting purchase of " + itemId);
        ensureSetup(new SetupFinishedListener() {
            @Override
            public void onSetupFinished() {
                try {
                    iabHelper.launchPurchaseFlow(activity, itemId, 0, new IabHelper.OnIabPurchaseFinishedListener() {
                        @Override
                        public void onIabPurchaseFinished(IabResult result, Purchase info) {
                            if (result.isSuccess()) {
                                Log.i(TAG, "purchase result: SUCCESS");
                                loadInventory(null);
                                listener.onProductPurchased(info.getOrderId(), info.getSku());
                            } else {
                                Log.e(TAG, "purchase result: " + result.toString());
                            }
                        }
                    }, payload);
                } catch (Exception e) {
                    Log.e(TAG, "error while launching purchase flow", e);
                }
            }
        });
    }

    public synchronized void loadInventory(final OnLoadInventoryFinishedListener listener) {
        ensureSetup(new SetupFinishedListener() {
            @Override
            public void onSetupFinished() {
                loadInternal(listener);
            }
        });
    }

    public synchronized void getOwnedItems(final OwnedItemsLoadedListener listener) {
        ensureSetup(new SetupFinishedListener() {
            @Override
            public void onSetupFinished() {
                Set<String> ownedItems = inventory.get().getAllOwnedSkus();
                Log.i(TAG, "owned items: " + ownedItems);
                listener.onItemsLoaded(ownedItems);
            }
        });
    }

    private void ensureSetup(SetupFinishedListener listener) {
        if (isSetup()) {
            listener.onSetupFinished();
        } else {
            setup(listener);
        }
    }

    private boolean isSetup() {
        return iabHelper != null && inventory != null && iabHelper.isSetupDone();
    }

    public synchronized void setup(final SetupFinishedListener listener) {
        checkNotNull(listener);

        try {
            Log.d(TAG, "Starting setup");
            iabHelper = new IabHelper(AndFHEMApplication.getContext(), AndFHEMApplication.PUBLIC_KEY_ENCODED);
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    if (result.isSuccess()) {
                        Log.d(TAG, "=> SUCCESS");
                        loadInternal(null);
                    } else {
                        Log.e(TAG, "=> ERROR " + result.toString());
                        inventory.set(Inventory.empty());
                    }
                    listener.onSetupFinished();
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Error while trying to start billing", e);
            listener.onSetupFinished();
        }
    }

    private void loadInternal(OnLoadInventoryFinishedListener listener) {
        try {
            Log.i(TAG, "loading inventory");
            inventory.set(iabHelper.queryInventory(false, null, null));
        } catch (IabException e) {
            Log.e(TAG, "cannot load inventory", e);
            inventory.set(Inventory.empty());
        } finally {
            if (listener != null) {
                listener.onInventoryLoadFinished();
            }
        }
    }

    public interface ProductPurchasedListener {
        void onProductPurchased(String orderId, String productId);
    }

    public interface SetupFinishedListener {
        void onSetupFinished();
    }

    public interface OnLoadInventoryFinishedListener {
        void onInventoryLoadFinished();
    }

    public interface OwnedItemsLoadedListener {
        void onItemsLoaded(Set<String> items);
    }
}
