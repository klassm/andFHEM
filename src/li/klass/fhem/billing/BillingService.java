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

package li.klass.fhem.billing;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.vending.billing.IMarketBillingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static li.klass.fhem.billing.BillingConstants.*;
import static li.klass.fhem.billing.Security.*;

public class BillingService extends Service implements ServiceConnection {

    private static final String TAG = BillingService.class.getName();
    private static IMarketBillingService marketBillingService;
    private static List<BillingRequest> pendingRequests = new ArrayList<BillingRequest>();
    private static HashMap<Long, BillingRequest> sentRequests = new HashMap<Long, BillingRequest>();

    public abstract class BillingRequest {
        private final int mStartId;
        protected long requestId;

        public BillingRequest(int startId) {
            mStartId = startId;
        }

        public int getStartId() {
            return mStartId;
        }

        /**
         * Run the request, starting the connection if necessary.
         * @return true if the request was executed or queued; false if there
         * was an error starting the connection
         */
        public boolean runRequest() {
            if (runIfConnected()) {
                return true;
            }

            if (bindToMarketBillingService()) {
                // Add a pending request to run when the service is connected.
                pendingRequests.add(this);
                return true;
            }
            return false;
        }

        /**
         * Try running the request directly if the service is already connected.
         * @return true if the request ran successfully; false if the service
         * is not connected or there was an error when trying to use it
         */
        public boolean runIfConnected() {
            Log.d(TAG, getClass().getSimpleName());
            if (marketBillingService != null) {
                try {
                    requestId = run();
                    Log.d(TAG, "request id: " + requestId);
                    if (requestId >= 0) {
                        sentRequests.put(requestId, this);
                    }
                    return true;
                } catch (RemoteException e) {
                    onRemoteException(e);
                }
            }
            return false;
        }

        /**
         * Called when a remote exception occurs while trying to execute the
         * {@link #run()} method.  The derived class can override this to
         * execute exception-handling code.
         * @param e the exception
         */
        protected void onRemoteException(RemoteException e) {
            Log.w(TAG, "remote billing service crashed", e);
            marketBillingService = null;
        }

        /**
         * The derived class must implement this method.
         * @throws RemoteException
         */
        abstract protected long run() throws RemoteException;

        /**
         * This is called when Android Market sends a response code for this
         * request.
         * @param responseCode the response code
         */
        protected void responseCodeReceived(ResponseCode responseCode) {
        }

        protected Bundle makeRequestBundle(String method) {
            Bundle request = new Bundle();
            request.putString(BILLING_REQUEST_METHOD, method);
            request.putInt(BILLING_REQUEST_API_VERSION, 1);
            request.putString(BILLING_REQUEST_PACKAGE_NAME, getPackageName());
            return request;
        }

        protected void logResponseCode(String method, Bundle response) {
            ResponseCode responseCode = ResponseCode.valueOf(
                    response.getInt(BILLING_RESPONSE_RESPONSE_CODE));
            if (DEBUG) {
                Log.e(TAG, method + " received " + responseCode.toString());
            }
        }

    }


    /**
     * Wrapper class that checks if in-app billing is supported.
     */
    public class CheckBillingSupported extends BillingRequest {
        public CheckBillingSupported() {
            // This object is never created as a side effect of starting this
            // service so we pass -1 as the startId to indicate that we should
            // not stop this service after executing this request.
            super(-1);
        }

        @Override
        protected long run() throws RemoteException {
            Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
            Bundle response = marketBillingService.sendBillingRequest(request);
            int responseCode = response.getInt(BILLING_RESPONSE_RESPONSE_CODE);
            if (DEBUG) {
                Log.i(TAG, "CheckBillingSupported response code: " +
                        ResponseCode.valueOf(responseCode));
            }
            boolean billingSupported = (responseCode == ResponseCode.RESULT_OK.ordinal());
            ResponseHandler.checkBillingSupportedResponse(billingSupported);
            return BILLING_RESPONSE_INVALID_REQUEST_ID;
        }
    }


    /**
     * Wrapper class that requests a purchase.
     */
    public class RequestPurchase extends BillingRequest {
        public final String productId;
        public final String developerPayload;

        public RequestPurchase(String itemId) {
            this(itemId, null);
        }

        public RequestPurchase(String itemId, String developerPayload) {
            // This object is never created as a side effect of starting this
            // service so we pass -1 as the startId to indicate that we should
            // not stop this service after executing this request.
            super(-1);
            productId = itemId;
            this.developerPayload = developerPayload;
        }

        @Override
        protected long run() throws RemoteException {
            Bundle request = makeRequestBundle("REQUEST_PURCHASE");
            request.putString(BILLING_REQUEST_ITEM_ID, productId);
            // Note that the developer payload is optional.
            if (developerPayload != null) {
                request.putString(BILLING_REQUEST_DEVELOPER_PAYLOAD, developerPayload);
            }
            Bundle response = marketBillingService.sendBillingRequest(request);
            PendingIntent pendingIntent
                    = response.getParcelable(BILLING_RESPONSE_PURCHASE_INTENT);
            if (pendingIntent == null) {
                Log.e(TAG, "Error with requestPurchase");
                return BILLING_RESPONSE_INVALID_REQUEST_ID;
            }

            Intent intent = new Intent();
            ResponseHandler.buyPageIntentResponse(pendingIntent, intent);
            return response.getLong(BILLING_RESPONSE_REQUEST_ID,
                    BILLING_RESPONSE_INVALID_REQUEST_ID);
        }

        @Override
        protected void responseCodeReceived(ResponseCode responseCode) {
            ResponseHandler.responseCodeReceived(BillingService.this, this, responseCode);
        }
    }

    /**
     * Wrapper class that confirms a list of notifications to the server.
     */
    public class ConfirmNotifications extends BillingRequest {
        final String[] mNotifyIds;

        public ConfirmNotifications(int startId, String[] notifyIds) {
            super(startId);
            mNotifyIds = notifyIds;
        }

        @Override
        protected long run() throws RemoteException {
            Bundle request = makeRequestBundle("CONFIRM_NOTIFICATIONS");
            request.putStringArray(BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
            Bundle response = marketBillingService.sendBillingRequest(request);
            logResponseCode("confirmNotifications", response);
            return response.getLong(BILLING_RESPONSE_REQUEST_ID,
                    BILLING_RESPONSE_INVALID_REQUEST_ID);
        }
    }

    /**
     * Wrapper class that sends a GET_PURCHASE_INFORMATION message to the server.
     */
    public class GetPurchaseInformation extends BillingRequest {
        long mNonce;
        final String[] mNotifyIds;

        public GetPurchaseInformation(int startId, String[] notifyIds) {
            super(startId);
            mNotifyIds = notifyIds;
        }

        @Override
        protected long run() throws RemoteException {
            mNonce = generateNonce();

            Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
            request.putLong(BILLING_REQUEST_NONCE, mNonce);
            request.putStringArray(BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
            Bundle response = marketBillingService.sendBillingRequest(request);
            logResponseCode("getPurchaseInformation", response);
            return response.getLong(BILLING_RESPONSE_REQUEST_ID,
                    BILLING_RESPONSE_INVALID_REQUEST_ID);
        }

        @Override
        protected void onRemoteException(RemoteException e) {
            super.onRemoteException(e);
            removeNonce(mNonce);
        }
    }

    /**
     * Wrapper class that sends a RESTORE_TRANSACTIONS message to the server.
     */
    public class RestoreTransactions extends BillingRequest {
        long mNonce;

        public RestoreTransactions() {
            // This object is never created as a side effect of starting this
            // service so we pass -1 as the startId to indicate that we should
            // not stop this service after executing this request.
            super(-1);
        }

        @Override
        protected long run() throws RemoteException {
            mNonce = generateNonce();

            Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
            request.putLong(BILLING_REQUEST_NONCE, mNonce);
            Bundle response = marketBillingService.sendBillingRequest(request);
            logResponseCode("restoreTransactions", response);
            return response.getLong(BILLING_RESPONSE_REQUEST_ID,
                    BILLING_RESPONSE_INVALID_REQUEST_ID);
        }

        @Override
        protected void onRemoteException(RemoteException e) {
            super.onRemoteException(e);
            removeNonce(mNonce);
        }

        @Override
        protected void responseCodeReceived(ResponseCode responseCode) {
            ResponseHandler.responseCodeReceived(BillingService.this, this, responseCode);
        }
    }

    public BillingService() {
        super();
    }

    public void setContext(Context context) {
        attachBaseContext(context);
    }

    /**
     * We don't support binding to this service, only starting the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent, startId);
    }

    /**
     * The {@link BillingReceiver} sends messages to this service using intents.
     * Each intent has an action and some extra arguments specific to that action.
     * @param intent the intent containing one of the supported actions
     * @param startId an identifier for the invocation instance of this service
     */
    public void handleCommand(Intent intent, int startId) {
        String action = intent.getAction();
        if (DEBUG) {
            Log.i(TAG, "handleCommand() action: " + action);
        }
        if (ACTION_CONFIRM_NOTIFICATION.equals(action)) {
            String[] notifyIds = intent.getStringArrayExtra(NOTIFICATION_ID);
            confirmNotifications(startId, notifyIds);
        } else if (ACTION_GET_PURCHASE_INFORMATION.equals(action)) {
            String notifyId = intent.getStringExtra(NOTIFICATION_ID);
            getPurchaseInformation(startId, new String[] { notifyId });
        } else if (ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            String signedData = intent.getStringExtra(INAPP_SIGNED_DATA);
            String signature = intent.getStringExtra(INAPP_SIGNATURE);
            purchaseStateChanged(startId, signedData, signature);
        } else if (ACTION_RESPONSE_CODE.equals(action)) {
            long requestId = intent.getLongExtra(INAPP_REQUEST_ID, -1);
            int responseCodeIndex = intent.getIntExtra(INAPP_RESPONSE_CODE,
                    ResponseCode.RESULT_ERROR.ordinal());
            ResponseCode responseCode = ResponseCode.valueOf(responseCodeIndex);
            checkResponseCode(requestId, responseCode);
        }
    }

    /**
     * Binds to the MarketBillingService and returns true if the bind
     * succeeded.
     * @return true if the bind succeeded; false otherwise
     */
    private boolean bindToMarketBillingService() {
        try {
            if (DEBUG) {
                Log.i(TAG, "binding to Market billing service");
            }
            boolean bindResult = bindService(
                    new Intent(MARKET_BILLING_SERVICE_ACTION),
                    this,  // ServiceConnection.
                    Context.BIND_AUTO_CREATE);

            if (bindResult) {
                return true;
            } else {
                Log.e(TAG, "Could not bind to service.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e);
        }
        return false;
    }

    /**
     * Checks if in-app billing is supported.
     * @return true if supported; false otherwise
     */
    public boolean checkBillingSupported() {
        return new CheckBillingSupported().runRequest();
    }

    /**
     * Requests that the given item be offered to the user for purchase. When
     * the purchase succeeds (or is canceled) the {@link BillingReceiver}
     * receives an intent with the action {@link BillingConstants#ACTION_NOTIFY}.
     * Returns false if there was an error trying to connect to Android Market.
     * @param productId an identifier for the item being offered for purchase
     * @param developerPayload a payload that is associated with a given
     * purchase, if null, no payload is sent
     * @return false if there was an error connecting to Android Market
     */
    public boolean requestPurchase(String productId, String developerPayload) {
        return new RequestPurchase(productId, developerPayload).runRequest();
    }

    /**
     * Requests transaction information for all managed items. Call this only when the
     * application is first installed or after a database wipe. Do NOT call this
     * every time the application starts up.
     * @return false if there was an error connecting to Android Market
     */
    public boolean restoreTransactions() {
        return new RestoreTransactions().runRequest();
    }

    /**
     * Confirms receipt of a purchase state change. Each {@code notifyId} is
     * an opaque identifier that came from the server. This method sends those
     * identifiers back to the MarketBillingService, which ACKs them to the
     * server. Returns false if there was an error trying to connect to the
     * MarketBillingService.
     * @param startId an identifier for the invocation instance of this service
     * @param notifyIds a list of opaque identifiers associated with purchase
     * state changes.
     * @return false if there was an error connecting to Market
     */
    private boolean confirmNotifications(int startId, String[] notifyIds) {
        return new ConfirmNotifications(startId, notifyIds).runRequest();
    }

    /**
     * Gets the purchase information. This message includes a list of
     * notification IDs sent to us by Android Market, which we include in
     * our request. The server responds with the purchase information,
     * encoded as a JSON string, and sends that to the {@link BillingReceiver}
     * in an intent with the action {@link BillingConstants#ACTION_PURCHASE_STATE_CHANGED}.
     * Returns false if there was an error trying to connect to the MarketBillingService.
     *
     * @param startId an identifier for the invocation instance of this service
     * @param notifyIds a list of opaque identifiers associated with purchase
     * state changes
     * @return false if there was an error connecting to Android Market
     */
    private boolean getPurchaseInformation(int startId, String[] notifyIds) {
        return new GetPurchaseInformation(startId, notifyIds).runRequest();
    }

    /**
     * Verifies that the data was signed with the given signature, and calls
     * {@link ResponseHandler#purchaseResponse}
     * for each verified purchase.
     * @param startId an identifier for the invocation instance of this service
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     */
    private void purchaseStateChanged(int startId, String signedData, String signature) {
        ArrayList<Security.VerifiedPurchase> purchases;
        purchases = verifyPurchase(signedData, signature);
        if (purchases == null) {
            return;
        }

        ArrayList<String> notifyList = new ArrayList<String>();
        for (VerifiedPurchase verifiedPurchase : purchases) {
            if (verifiedPurchase.notificationId != null) {
                notifyList.add(verifiedPurchase.notificationId);
            }
            Log.i(TAG, "purchase state change (productId: " + verifiedPurchase.productId + ",targetState: " + verifiedPurchase.purchaseState.name());
            ResponseHandler.purchaseResponse(this, verifiedPurchase.purchaseState, verifiedPurchase.productId,
                    verifiedPurchase.orderId, verifiedPurchase.purchaseTime, verifiedPurchase.developerPayload);
        }
        if (!notifyList.isEmpty()) {
            String[] notifyIds = notifyList.toArray(new String[notifyList.size()]);
            confirmNotifications(startId, notifyIds);
        }
    }

    /**
     * This is called when we receive a response code from Android Market for a request
     * that we made. This is used for reporting various errors and for
     * acknowledging that an order was sent to the server. This is NOT used
     * for any purchase state changes.  All purchase state changes are received
     * in the {@link BillingReceiver} and passed to this service, where they are
     * handled in {@link #purchaseStateChanged(int, String, String)}.
     * @param requestId a number that identifies a request, assigned at the
     * time the request was made to Android Market
     * @param responseCode a response code from Android Market to indicate the state
     * of the request
     */
    private void checkResponseCode(long requestId, ResponseCode responseCode) {
        BillingRequest request = sentRequests.get(requestId);
        if (request != null) {
            if (DEBUG) {
                Log.d(TAG, request.getClass().getSimpleName() + ": " + responseCode);
            }
            request.responseCodeReceived(responseCode);
        }
        sentRequests.remove(requestId);
    }

    /**
     * Runs any pending requests that are waiting for a connection to the
     * service to be established.  This runs in the main UI thread.
     */
    private void runPendingRequests() {
        int maxStartId = -1;
        while (pendingRequests.size() > 0) {
            BillingRequest request = pendingRequests.get(0);
            if (request.runIfConnected()) {
                // Remove the request
                pendingRequests.remove(request);

                // Remember the largest startId, which is the most recent
                // request to start this service.
                if (maxStartId < request.getStartId()) {
                    maxStartId = request.getStartId();
                }
            } else {
                // The service crashed, so restart it. Note that this leaves
                // the current request on the queue.
                bindToMarketBillingService();
                return;
            }
        }

        // If we get here then all the requests ran successfully.  If maxStartId
        // is not -1, then one of the requests started the service, so we can
        // stop it now.
        if (maxStartId >= 0) {
            if (DEBUG) {
                Log.i(TAG, "stopping service, startId: " + maxStartId);
            }
            stopSelf(maxStartId);
        }
    }

    /**
     * This is called when we are connected to the MarketBillingService.
     * This runs in the main UI thread.
     */
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (DEBUG) {
            Log.d(TAG, "Billing service connected");
        }
        marketBillingService = IMarketBillingService.Stub.asInterface(service);
        runPendingRequests();
    }

    /**
     * This is called when we are disconnected from the MarketBillingService.
     */
    public void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "Billing service disconnected");
        marketBillingService = null;
    }

    public void unbind() {
        try {
            unbindService(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error while unbinding the service", e);
            // This might happen if the service was disconnected
        }
    }

    public boolean hasPendingRequestFor(String productId) {
        for (BillingRequest pendingRequest : pendingRequests) {
            if (pendingRequest instanceof RequestPurchase) {
                if (((RequestPurchase) pendingRequest).productId.equals(productId)) {
                    return true;
                }
            }
        }
        return false;
    }
}