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

package li.klass.fhem.billing

import android.app.Activity
import android.content.Context
import com.android.vending.billing.IabHelper
import com.android.vending.billing.Inventory
import com.android.vending.billing.Purchase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.util.awaitCallback
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingService @Inject
constructor() {

    var iabHelper: IabHelper? = null
    var inventory: Inventory = Inventory.empty()
        private set

    private val isSetup: Boolean
        get() = iabHelper != null && iabHelper!!.isSetupDone

    private val isLoaded: Boolean
        get() = !inventory.allOwnedSkus.isEmpty()

    @Synchronized
    fun stop() {
        try {
            if (iabHelper != null) {
                iabHelper!!.dispose()
                iabHelper = null
            }
        } catch (e: Exception) {
            LOG.debug("stop() - cannot stop", e)
        }

    }

    @Synchronized
    suspend fun requestPurchase(activity: Activity, itemId: String,
                                payload: String?): Purchase? {
        LOG.info("requestPurchase() - requesting purchase of $itemId")
        val success = ensureSetup(activity)
        try {
            if (!success) {
                LOG.error("requestPurchase() - cannot initialize purchase flow, setup was not successful");
                return null
            } else {
                val helper = iabHelper ?: return null
                helper.flagEndAsync()

                return awaitCallback { callback ->
                    helper.launchPurchaseFlow(activity, itemId, 0, { result, info ->
                        if (result.isSuccess) {
                            LOG.info("requestPurchase() - purchase result: SUCCESS");
                            GlobalScope.launch {
                                loadInventory(activity)
                                callback.onComplete(info)
                            }
                        } else {
                            LOG.error("requestPurchase() - purchase result: " + result.toString());
                        }
                    }, payload)
                }

            }
        } catch (e: Exception) {
            LOG.error("requestPurchase() - error while launching purchase flow", e)
            return null
        }
    }

    @Synchronized
    suspend fun loadInventory(context: Context): Boolean {
        if (isLoaded) {
            LOG.debug("loadInventory() - inventory is already setup and loaded, skipping load ($inventory)")
            return true
        } else {
            val success = ensureSetup(context)
            if (success) {
                LOG.debug("loadInventory() - calling load internal")
                loadInternal()
                return true
            } else {
                LOG.debug("loadInventory() - won't load inventory, setup was not successful")
                return false
            }
        }
    }

    @Synchronized
    operator fun contains(sku: String): Boolean {
        return inventory.hasPurchase(sku)
    }

    private suspend fun ensureSetup(context: Context): Boolean {
        return if (isSetup) {
            LOG.debug("ensureSetup() - I am already setup")
            true
        } else {
            val isSetupDoneMessage = if (iabHelper != null) ",isSetupDone=" + iabHelper!!.isSetupDone else ""
            LOG.debug("ensureSetup() - Setting up ... (helper=$iabHelper$isSetupDoneMessage)")
            setup(context)
        }
    }

    @Synchronized
    private suspend fun setup(context: Context): Boolean {
        return awaitCallback { callback ->
            try {
                LOG.debug("setup() - Starting setup " + this)
                iabHelper = createIabHelper(context)
                iabHelper!!.startSetup { result ->
                    try {
                        if (result.isSuccess) {
                            LOG.debug("setup() : setup was successful, setupIsDone=" + iabHelper!!.isSetupDone)
                        } else {
                            LOG.error("setup() : ERROR " + result.toString())
                        }
                        callback.onComplete(true)
                    } catch (e: Exception) {
                        inventory = Inventory.empty()
                        LOG.error("setup() : error during setup", e)
                        callback.onComplete(false)
                    }
                }
            } catch (e: Exception) {
                LOG.info("setup() - Error while trying to start billing", e)
                callback.onException(e)
            }
        }
    }

    private fun createIabHelper(context: Context): IabHelper {
        return IabHelper(context, AndFHEMApplication.PUBLIC_KEY_ENCODED)
    }

    @Synchronized
    private fun loadInternal(): Boolean {

        try {
            if (isLoaded) {
                LOG.debug("loadInternal() - inventory was already loaded, skipping load")
            } else if (!iabHelper!!.isSetupDone) {
                inventory = Inventory.empty()
                LOG.error("loadInternal() - setup was not done, initializing with empty inventory")
            } else {
                LOG.debug("loadInternal() - loading inventory")
                inventory = iabHelper!!.queryInventory(false, null)
                LOG.debug("loadInternal() - query inventory finished, inventory is $inventory")
            }
            return true
        } catch (e: Exception) {
            LOG.error("loadInternal() - cannot load inventory", e)
            return false
        }
    }

    interface ProductPurchasedListener {
        fun onProductPurchased(orderId: String, productId: String)
    }

    interface OnLoadInventoryFinishedListener {
        fun onInventoryLoadFinished(success: Boolean)
    }

    interface SetupFinishedListener {
        fun onSetupFinished(success: Boolean)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BillingService::class.java)
    }
}
