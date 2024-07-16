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

package li.klass.fhem.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.billing.BillingService
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.billing.PremiumStatus
import li.klass.fhem.constants.Actions
import li.klass.fhem.databinding.ShopPremiumBinding
import javax.inject.Inject

class PremiumActivity : AppCompatActivity() {

    @Inject
    lateinit var billingService: BillingService

    @Inject
    lateinit var licenseService: LicenseService

    lateinit var viewBinding: ShopPremiumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndFHEMApplication.application!!.daggerComponent.inject(this)

        viewBinding = ShopPremiumBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBinding.shopPremiumBought.visibility = View.GONE
        viewBinding.shopPremiumBuy.visibility = View.GONE

        viewBinding.shopPremiumBuy.setOnClickListener {
            Log.i(TAG, "request purchase for product " + AndFHEMApplication.INAPP_PREMIUM_ID)
            GlobalScope.launch(Dispatchers.Main) {
                billingService.requestPurchase(
                    this@PremiumActivity,
                    AndFHEMApplication.INAPP_PREMIUM_ID
                )
                update()
            }
        }
    }

    fun update() {
        viewBinding.shopPremiumBought.visibility = View.GONE
        viewBinding.shopPremiumBuy.visibility = View.GONE

        GlobalScope.launch(Dispatchers.Main) {
            val premiumStatus = licenseService.premiumStatus()
            if (premiumStatus == PremiumStatus.PREMIUM) {
                viewBinding.shopPremiumBought.visibility = View.VISIBLE
            } else {
                viewBinding.shopPremiumBuy.visibility = View.VISIBLE
            }
        }
        sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG).apply { setPackage(packageName) })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        super.onResume()
        update()
    }

    companion object {
        private val TAG = PremiumActivity::class.java.name
    }
}
