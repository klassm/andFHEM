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

import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import li.klass.fhem.AndFHEMApplication;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.AndFHEMApplication.PUBLIC_KEY_ENCODED;

public class BillingService {

    public static final String TAG = BillingService.class.getName();
    public static final BillingService INSTANCE = new BillingService();
    IabHelper iabHelper;
    private AtomicReference<Inventory> inventory = new AtomicReference<Inventory>(Inventory.empty());
    private volatile boolean setupInProgress = false;

    BillingService() {
    }

    public synchronized void stop() {
        if (iabHelper != null) {
            iabHelper.dispose();
            iabHelper = null;
        }
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
                                loadInventory();
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

    private synchronized void loadInventory() {
        loadInventory(null);
    }

    public synchronized void loadInventory(final OnLoadInventoryFinishedListener listener) {
        if (isSetup() && isLoaded()) {
            if (listener != null) listener.onInventoryLoadFinished();
        } else {
            ensureSetup(new SetupFinishedListener() {
                @Override
                public void onSetupFinished() {
                    loadInternal(listener);
                }
            });
        }
    }

    public synchronized void getOwnedItems(final OwnedItemsLoadedListener listener) {
        checkArgument(inventory.get() != null);

        Set<String> ownedItems = inventory.get().getAllOwnedSkus();
        Log.i(TAG, "owned items: " + ownedItems);
        listener.onItemsLoaded(ownedItems, isSetup() && isLoaded());
    }

    private boolean isSetup() {
        return iabHelper != null && iabHelper.isSetupDone();
    }

    private boolean isLoaded() {
        return inventory.get() != null && !inventory.get().getAllOwnedSkus().isEmpty();
    }

    private synchronized void ensureSetup(SetupFinishedListener listener) {
        awaitSetupCompletion();
        if (isSetup()) {
            Log.i(TAG, "I am already setup");
            listener.onSetupFinished();
        } else {
            Log.i(TAG, "Setting up ... (" + iabHelper + (iabHelper != null ? " " + iabHelper.isSetupDone() : "") + ")");
            setup(listener);
        }
    }

    private void awaitSetupCompletion() {
        boolean waited = false;
        while (setupInProgress) {
            waited = true;
            try {
                Log.d(TAG, "wait for setup completion");
                synchronized (BillingService.INSTANCE) {
                    BillingService.INSTANCE.wait();
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "interrupted", e);
            }
        }
        if (waited) {
            Log.d(TAG, "notified => setup complete");
        }
    }

    synchronized void setup(final SetupFinishedListener listener) {
        checkNotNull(listener);

        setupInProgress = true;

        try {
            Log.d(TAG, "Starting setup");
            iabHelper = createIabHelper();
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    try {
                        if (result.isSuccess()) {
                            Log.d(TAG, "setup => SUCCESS");
                        } else {
                            Log.e(TAG, "setup => ERROR " + result.toString());
                        }
                        listener.onSetupFinished();
                    } finally {
                        notifySetupWaitingThreads();
                    }
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Error while trying to start billing", e);
            listener.onSetupFinished();
            notifySetupWaitingThreads();
        }
    }

    IabHelper createIabHelper() {
        return new IabHelper(AndFHEMApplication.getContext(), PUBLIC_KEY_ENCODED);
    }

    private void notifySetupWaitingThreads() {
        synchronized (BillingService.INSTANCE) {
            setupInProgress = false;
            BillingService.INSTANCE.notifyAll();
            Log.d(TAG, "setup complete => notifying all waiting threads");
        }
    }

    private synchronized void loadInternal(final OnLoadInventoryFinishedListener listener) {
        checkNotNull(iabHelper);

        if (!iabHelper.isSetupDone()) {
            inventory.set(Inventory.empty());
            Log.e(TAG, "setup was not done, initializing with empty inventory");
        } else if (isLoaded()) {
            if (listener != null) listener.onInventoryLoadFinished();
            Log.d(TAG, "inventory was already loaded, as found to not being empty, skipping load");
        } else {
            try {
                Log.i(TAG, "loading inventory");
                inventory.set(iabHelper.queryInventory(false, null));
            } catch (Exception e) {
                Log.e(TAG, "cannot load inventory", e);
                inventory.set(Inventory.empty());

            } finally {
                if (listener != null) {
                    listener.onInventoryLoadFinished();
                }
                BillingService.this.notifyAll();
            }
        }
    }

    IabHelper getIabHelper() {
        return iabHelper;
    }

    Inventory getInventory() {
        return inventory.get();
    }

    public interface ProductPurchasedListener {
        void onProductPurchased(String orderId, String productId);
    }

    public interface OnLoadInventoryFinishedListener {
        void onInventoryLoadFinished();
    }

    public interface OwnedItemsLoadedListener {
        void onItemsLoaded(Set<String> items, boolean isInitialized);
    }

    public interface SetupFinishedListener {
        void onSetupFinished();
    }
}
