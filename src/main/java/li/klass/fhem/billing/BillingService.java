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

import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;

@Singleton
public class BillingService {

    private IabHelper iabHelper = null;
    private Inventory inventory = Inventory.empty();

    private static final Logger LOG = LoggerFactory.getLogger(BillingService.class);

    @Inject
    public BillingService() {
    }

    public synchronized void stop() {
        try {
            if (iabHelper != null) {
                iabHelper.dispose();
                iabHelper = null;
            }
        } catch (Exception e) {
            LOG.debug("stop() - cannot stop", e);
        }
    }

    public synchronized void requestPurchase(final Activity activity, final String itemId,
                                             final String payload,
                                             final ProductPurchasedListener listener) {
        LOG.info("requestPurchase() - requesting purchase of " + itemId);
        ensureSetup(new SetupFinishedListener() {
            @Override
            public void onSetupFinished(boolean success) {
                try {
                    if (!success) {
                        LOG.error("requestPurchase() - cannot initialize purchase flow, setup was not successful");
                    } else {
                        iabHelper.flagEndAsync();
                        iabHelper.launchPurchaseFlow(activity, itemId, 0, new IabHelper.OnIabPurchaseFinishedListener() {
                            @Override
                            public void onIabPurchaseFinished(IabResult result, Purchase info) {
                                if (result.isSuccess()) {
                                    LOG.info("requestPurchase() - purchase result: SUCCESS");
                                    loadInventory(activity);
                                    listener.onProductPurchased(info.getOrderId(), info.getSku());
                                } else {
                                    LOG.error("requestPurchase() - purchase result: " + result.toString());
                                }
                            }
                        }, payload);
                    }
                } catch (Exception e) {
                    LOG.error("requestPurchase() - error while launching purchase flow", e);
                }
            }
        }, activity);
    }

    private synchronized void loadInventory(Context context) {
        loadInventory(null, context);
    }

    public synchronized void loadInventory(final OnLoadInventoryFinishedListener listener, Context context) {
        if (isLoaded()) {
            LOG.debug("loadInventory() - inventory is already setup and loaded, skipping load (" + inventory + ")");
            if (listener != null) listener.onInventoryLoadFinished(true);
        } else {
            ensureSetup(new SetupFinishedListener() {
                @Override
                public void onSetupFinished(boolean success) {
                    if (success) {
                        LOG.debug("loadInventory() - calling load internal");
                        loadInternal(listener);
                    } else {
                        LOG.debug("loadInventory() - won't load inventory, setup was not successful");
                        listener.onInventoryLoadFinished(false);
                    }
                }
            }, context);
        }
    }

    public synchronized boolean contains(final String sku) {
        return inventory != null && inventory.hasPurchase(sku);
    }

    private boolean isSetup() {
        return iabHelper != null && iabHelper.isSetupDone();
    }

    private boolean isLoaded() {
        return inventory != null && !inventory.getAllOwnedSkus().isEmpty();
    }

    private void ensureSetup(SetupFinishedListener listener, Context context) {
        if (isSetup()) {
            LOG.debug("ensureSetup() - I am already setup");
            listener.onSetupFinished(true);
        } else {
            String isSetupDoneMessage = iabHelper != null ? ",isSetupDone=" + iabHelper.isSetupDone() : "";
            LOG.debug("ensureSetup() - Setting up ... (helper=" + iabHelper + isSetupDoneMessage + ")");
            setup(listener, context);
        }
    }

    synchronized void setup(final SetupFinishedListener listener, Context context) {
        try {
            LOG.debug("setup() - Starting setup " + this);
            iabHelper = createIabHelper(context);
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    try {
                        if (result.isSuccess()) {
                            LOG.debug("setup() : setup was successful, setupIsDone=" + iabHelper.isSetupDone());
                        } else {
                            LOG.error("setup() : ERROR " + result.toString());
                        }
                        listener.onSetupFinished(true);
                    } catch (Exception e) {
                        inventory = Inventory.empty();
                        LOG.error("setup() : error during setup", e);
                        listener.onSetupFinished(false);
                    }
                }
            });
        } catch (Exception e) {
            LOG.info("setup() - Error while trying to start billing", e);
            listener.onSetupFinished(false);
        }
    }

    IabHelper createIabHelper(Context context) {
        return new IabHelper(context, AndFHEMApplication.Companion.getPUBLIC_KEY_ENCODED());
    }

    private synchronized void loadInternal(final OnLoadInventoryFinishedListener listener) {

        boolean success = false;
        try {
            if (isLoaded()) {
                LOG.debug("loadInternal() - inventory was already loaded, skipping load");
            } else if (!iabHelper.isSetupDone()) {
                inventory = Inventory.empty();
                LOG.error("loadInternal() - setup was not done, initializing with empty inventory");
            } else {
                LOG.debug("loadInternal() - loading inventory");
                inventory = iabHelper.queryInventory(false, null);
                LOG.debug("loadInternal() - query inventory finished, inventory is " + inventory);
            }
            success = true;
        } catch (Exception e) {
            LOG.error("loadInternal() - cannot load inventory", e);
        } finally {
            if (inventory == null) {
                LOG.error("loadInternal() - inventory was null, setting it to an empty inventory");
                inventory = Inventory.empty();
            }
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
