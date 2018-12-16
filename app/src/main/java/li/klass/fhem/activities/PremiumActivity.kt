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
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.shop_premium.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.billing.BillingService
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.constants.Actions
import javax.inject.Inject

class PremiumActivity : AppCompatActivity() {

    @Inject
    lateinit var billingService: BillingService
    @Inject
    lateinit var licenseService: LicenseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndFHEMApplication.application!!.daggerComponent.inject(this)

        setContentView(R.layout.shop_premium)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        shop_premium_bought.visibility = View.GONE
        shop_premium_buy.visibility = View.GONE

        shop_premium_buy.setOnClickListener {
            Log.i(TAG, "request purchase for product " + AndFHEMApplication.INAPP_PREMIUM_ID)
            GlobalScope.launch(Dispatchers.Main) {
                val result = billingService.requestPurchase(this@PremiumActivity,
                        AndFHEMApplication.INAPP_PREMIUM_ID, null)
                result?.let { update() }
            }
        }

        update()
    }

    fun update() {

        shop_premium_bought.visibility = View.GONE
        shop_premium_buy.visibility = View.GONE

        GlobalScope.launch(Dispatchers.Main) {
            val isPremium = licenseService.isPremium()
            if (isPremium) {
                shop_premium_bought.visibility = View.VISIBLE
            } else {
                shop_premium_buy.visibility = View.VISIBLE
            }
        }
        sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val TAG = PremiumActivity::class.java.name
    }
}
