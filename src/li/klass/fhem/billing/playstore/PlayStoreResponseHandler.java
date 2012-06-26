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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import li.klass.fhem.billing.BillingConstants;
import li.klass.fhem.billing.PurchaseDatabase;

import static li.klass.fhem.billing.BillingConstants.PurchaseState;
import static li.klass.fhem.billing.BillingConstants.ResponseCode;

/**
 * This class contains the methods that handle responses from Android Market.  The
 * implementation of these methods is specific to a particular application.
 * The methods in this example update the database and, if the main application
 * has registered a {@link PlayStorePurchaseObserver}, will also update the UI.  An
 * application might also want to forward some responses on to its own server,
 * and that could be done here (in a background thread) but this example does
 * not do that.
 *
 * You should modify and obfuscate this code before using it.
 */
public class PlayStoreResponseHandler {
    private static final String TAG = PlayStoreResponseHandler.class.getName();

    /**
     * This is a static instance of {@link PlayStorePurchaseObserver} that the
     * application creates and registers with this class. The PlayStorePurchaseObserver
     * is used for updating the UI if the UI is visible.
     */
    private static PlayStorePurchaseObserver purchaseObserver;

    /**
     * Registers an observer that updates the UI.
     * @param observer the observer to register
     */
    public static synchronized void register(PlayStorePurchaseObserver observer) {
        purchaseObserver = observer;
    }

    /**
     * Unregisters a previously registered observer.
     * @param observer the previously registered observer.
     */
    public static synchronized void unregister(PlayStorePurchaseObserver observer) {
        purchaseObserver = null;
    }

    /**
     * Notifies the application of the availability of the MarketBillingService.
     * This method is called in response to the application calling
     * {@link PlayStoreBillingService#checkBillingSupported()}.
     * @param supported true if in-app billing is supported.
     */
    public static void checkBillingSupportedResponse(boolean supported) {
        if (purchaseObserver != null) {
            purchaseObserver.onBillingSupported(supported);
        }
    }

    /**
     * Starts a new activity for the user to buy an item for sale. This method
     * forwards the intent on to the PlayStorePurchaseObserver (if it exists) because
     * we need to start the activity on the activity stack of the application.
     *
     * @param pendingIntent a PendingIntent that we received from Android Market that
     *     will create the new buy page activity
     * @param intent an intent containing a request id in an extra field that
     *     will be passed to the buy page activity when it is created
     */
    public static void buyPageIntentResponse(PendingIntent pendingIntent, Intent intent) {
        if (purchaseObserver == null) {
            if (BillingConstants.DEBUG) {
                Log.d(TAG, "UI is not running");
            }
            return;
        }
        purchaseObserver.startBuyPageActivity(pendingIntent, intent);
    }

    /**
     * Notifies the application of purchase state changes. The application
     * can offer an item for sale to the user via
     * {@link PlayStoreBillingService#requestPurchase}. The PlayStoreBillingService
     * calls this method after it gets the response. Another way this method
     * can be called is if the user bought something on another device running
     * this same app. Then Android Market notifies the other devices that
     * the user has purchased an item, in which case the PlayStoreBillingService will
     * also call this method. Finally, this method can be called if the item
     * was refunded.
     * @param purchaseState the state of the purchase request (PURCHASED,
     *     CANCELED, or REFUNDED)
     * @param productId a string identifying a product for sale
     * @param orderId a string identifying the order
     * @param purchaseTime the time the product was purchased, in milliseconds
     *     since the epoch (Jan 1, 1970)
     * @param developerPayload the developer provided "payload" associated with
     *     the order
     */
    public static void purchaseResponse(
            final Context context, final PurchaseState purchaseState, final String productId,
            final String orderId, final long purchaseTime, final String developerPayload) {

        // Update the database with the purchase state. We shouldn't do that
        // from the main thread so we do the work in a background thread.
        // We don't update the UI here. We will update the UI after we update
        // the database because we need to read and update the current quantity
        // first.
        new Thread(new Runnable() {
            public void run() {
                PurchaseDatabase db = PurchaseDatabase.INSTANCE;
                int quantity = db.updatePurchase(
                        orderId, productId, purchaseState, purchaseTime, developerPayload);
                db.close();

                // This needs to be synchronized because the UI thread can change the
                // value of purchaseObserver.
                synchronized(PlayStoreResponseHandler.class) {
                    if (purchaseObserver != null) {
                        purchaseObserver.postPurchaseStateChange(
                                purchaseState, productId, quantity, purchaseTime, developerPayload);
                    }
                }
            }
        }).start();
    }

    /**
     * This is called when we receive a response code from Android Market for a
     * RequestPurchase request that we made.  This is used for reporting various
     * errors and also for acknowledging that an order was sent successfully to
     * the server. This is NOT used for any purchase state changes. All
     * purchase state changes are received in the {@link PlayStoreBillingReceiver} and
     * are handled in {@link PlayStoreSecurity#verifyPurchase(String, String)}.
     * @param context the context
     * @param request the RequestPurchase request for which we received a
     *     response code
     * @param responseCode a response code from Market to indicate the state
     * of the request
     */
    public static void responseCodeReceived(Context context, PlayStoreBillingService.RequestPurchase request,
                                            ResponseCode responseCode) {
        if (purchaseObserver != null) {
            purchaseObserver.onRequestPurchaseResponse(request, responseCode);
        }
    }

    /**
     * This is called when we receive a response code from Android Market for a
     * RestoreTransactions request.
     * @param context the context
     * @param request the RestoreTransactions request for which we received a
     *     response code
     * @param responseCode a response code from Market to indicate the state
     *     of the request
     */
    public static void responseCodeReceived(Context context, PlayStoreBillingService.RestoreTransactions request,
                                            ResponseCode responseCode) {
        if (purchaseObserver != null) {
            purchaseObserver.onRestoreTransactionsResponse(request, responseCode);
        }
    }
}