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
import android.content.Intent
import com.android.billingclient.api.*
import kotlinx.coroutines.runBlocking
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.constants.Actions
import li.klass.fhem.util.awaitCallback
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingService @Inject
constructor() : PurchasesUpdatedListener {
    lateinit var billingClient: BillingClient

    private val ownedSkus = mutableSetOf<String>()

    fun start(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                LOG.info("setup finished -  ${billingResult.debugMessage}, ${billingResult.responseCode}")
            }

            override fun onBillingServiceDisconnected() {
            }
        })
    }

    suspend fun requestPurchase(activity: Activity, itemId: String) {
        LOG.info("requestPurchase() - requesting purchase of $itemId")
        if (!billingClient.isReady) {
            LOG.error("requestPurchase() - cannot initialize purchase flow, setup was not successful")
            return
        }

        val skuDetails = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(itemId))
            .setType(BillingClient.SkuType.INAPP)
            .build()
        val details = billingClient.querySkuDetails(skuDetails)
        val item = details.skuDetailsList?.get(0)
        if (item == null) {
            LOG.error("requestPurchase() - cannot find item for $itemId");
            return
        }

        billingClient.launchBillingFlow(
            activity, BillingFlowParams.newBuilder()
                .setSkuDetails(item)
                .build()
        )
    }

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        LOG.info("onPurchasesUpdated() - purchases: $purchases")
        if (purchases != null) {
            acknowledgePurchases(purchases)
            ownedSkus.addAll(purchases.flatMap { it.skus })
            val application = AndFHEMApplication.application
            application?.sendBroadcast(Intent(Actions.DO_UPDATE).apply { setPackage(application.packageName) })
        }
    }

    suspend fun loadInventory(): Boolean {
        if (!billingClient.isReady) {
            return false
        }

        return awaitCallback { queryComplete ->
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.INAPP
            ) { _, purchases ->
                LOG.info("found purchases - $purchases")
                val skus = purchases.flatMap { it.skus }
                ownedSkus.addAll(skus)

                acknowledgePurchases(purchases)

                queryComplete.onComplete(true)
            }
        }

    }

    private fun acknowledgePurchases(purchases: List<Purchase>) {
        purchases.filterNot { it.isAcknowledged }.forEach { purchase ->
            runBlocking {
                LOG.info("acknowledge purchase - $purchase")
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                )
            }
        }
    }

    @Synchronized
    operator fun contains(sku: String): Boolean = ownedSkus.contains(sku)


    companion object {
        private val LOG = LoggerFactory.getLogger(BillingService::class.java)
    }
}
