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
import android.content.Context;
import android.util.Log;

import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.dagger.ForApplication;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.AndFHEMApplication.PUBLIC_KEY_ENCODED;

@Singleton
public class BillingService {

    public static final String TAG = BillingService.class.getName();

    private IabHelper iabHelper = null;
    private Inventory inventory = Inventory.empty();

    @Inject
    @ForApplication
    Context applicationContext;

    public synchronized void stop() {
        try {
            if (iabHelper != null) {
                iabHelper.dispose();
                iabHelper = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "stop() - cannot stop", e);
        }
    }

    public synchronized void requestPurchase(final Activity activity, final String itemId,
                                             final String payload,
                                             final ProductPurchasedListener listener) {
        Log.i(TAG, "requestPurchase() - requesting purchase of " + itemId);
        ensureSetup(new SetupFinishedListener() {
            @Override
            public void onSetupFinished(boolean success) {
                try {
                    if (!success) {
                        Log.e(TAG, "requestPurchase() - cannot initialize purchase flow, setup was not successful");
                    } else {
                        iabHelper.launchPurchaseFlow(activity, itemId, 0, new IabHelper.OnIabPurchaseFinishedListener() {
                            @Override
                            public void onIabPurchaseFinished(IabResult result, Purchase info) {
                                if (result.isSuccess()) {
                                    Log.i(TAG, "requestPurchase() - purchase result: SUCCESS");
                                    loadInventory();
                                    listener.onProductPurchased(info.getOrderId(), info.getSku());
                                } else {
                                    Log.e(TAG, "requestPurchase() - purchase result: " + result.toString());
                                }
                            }
                        }, payload);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "requestPurchase() - error while launching purchase flow", e);
                }
            }
        });
    }

    private synchronized void loadInventory() {
        loadInventory(null);
    }

    public synchronized void loadInventory(final OnLoadInventoryFinishedListener listener) {
        if (isSetup() && isLoaded()) {
            Log.d(TAG, "loadInventory() - inventory is already setup and loaded, skipping load");
            if (listener != null) listener.onInventoryLoadFinished(true);
        } else {
            ensureSetup(new SetupFinishedListener() {
                @Override
                public void onSetupFinished(boolean success) {
                    if (success) {
                        loadInternal(listener);
                    } else {
                        listener.onInventoryLoadFinished(false);
                        Log.d(TAG, "won't load inventory, setup was not successful");
                    }
                }
            });
        }
    }

    public synchronized boolean contains(final String sku) {
        checkArgument(inventory != null);
        checkArgument(isSetup());


        return inventory.hasPurchase(sku);
    }

    private boolean isSetup() {
        return iabHelper != null && iabHelper.isSetupDone();
    }

    private boolean isLoaded() {
        return inventory != null && !inventory.getAllOwnedSkus().isEmpty();
    }

    private void ensureSetup(SetupFinishedListener listener) {
        if (isSetup()) {
            Log.d(TAG, "ensureSetup() - I am already setup");
            listener.onSetupFinished(true);
        } else {
            String isSetupDoneMessage = iabHelper != null ? ",isSetupDone=" + iabHelper.isSetupDone() : "";
            Log.d(TAG, "ensureSetup() - Setting up ... (helper=" + iabHelper + isSetupDoneMessage + ")");
            setup(listener);
        }
    }

    synchronized void setup(final SetupFinishedListener listener) {
        checkNotNull(listener);

        try {
            Log.d(TAG, "setup() - Starting setup " + this);
            iabHelper = createIabHelper();
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    checkNotNull(iabHelper, "setup() - iabHelper may not be null after setup");
                    try {
                        if (result.isSuccess()) {
                            Log.d(TAG, "setup() : setup was successful, setupIsDone=" + iabHelper.isSetupDone());
                        } else {
                            Log.e(TAG, "setup() : ERROR " + result.toString());
                        }
                        listener.onSetupFinished(true);
                    } catch (Exception e) {
                        Log.e(TAG, "setup() : error during setup", e);
                        listener.onSetupFinished(false);
                    }
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "setup() - Error while trying to start billing", e);
            listener.onSetupFinished(false);
        }
    }

    IabHelper createIabHelper() {
        return new IabHelper(applicationContext, PUBLIC_KEY_ENCODED);
    }

    private synchronized void loadInternal(final OnLoadInventoryFinishedListener listener) {
        checkNotNull(iabHelper);

        boolean success = false;
        try {
            if (!iabHelper.isSetupDone()) {
                inventory = Inventory.empty();
                Log.e(TAG, "loadInternal() - setup was not done, initializing with empty inventory");
            } else if (isLoaded()) {
                Log.d(TAG, "loadInternal() - inventory was already loaded, skipping load");
            } else {
                Log.d(TAG, "loadInternal() - loading inventory");
                inventory = iabHelper.queryInventory(false, null);
            }
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "loadInternal() - cannot load inventory", e);
            inventory = Inventory.empty();
        } finally {
            if (listener != null) {
                listener.onInventoryLoadFinished(success);
            }
        }
    }

    IabHelper getIabHelper() {
        return iabHelper;
    }

    Inventory getInventory() {
        return inventory;
    }

    void setIabHelper(IabHelper iabHelper) {
        this.iabHelper = iabHelper;
    }

    public interface ProductPurchasedListener {
        void onProductPurchased(String orderId, String productId);
    }

    public interface OnLoadInventoryFinishedListener {
        void onInventoryLoadFinished(boolean success);
    }

    public interface SetupFinishedListener {
        void onSetupFinished(boolean success);
    }
}
