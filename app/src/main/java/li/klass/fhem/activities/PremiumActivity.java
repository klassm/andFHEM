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

package li.klass.fhem.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.util.FhemResultReceiver;

public class PremiumActivity extends AppCompatActivity implements BillingService.ProductPurchasedListener {

    private static final String TAG = PremiumActivity.class.getName();

    @Inject
    BillingService billingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndFHEMApplication.Companion.getApplication().getDaggerComponent().inject(this);

        setContentView(R.layout.shop_premium);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.shop_premium_bought).setVisibility(View.GONE);
        findViewById(R.id.shop_premium_buy).setVisibility(View.GONE);

        findViewById(R.id.shop_premium_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "request purchase for product " + AndFHEMApplication.Companion.getINAPP_PREMIUM_ID());
                billingService.requestPurchase(PremiumActivity.this,
                        AndFHEMApplication.Companion.getINAPP_PREMIUM_ID(), null, PremiumActivity.this);
            }
        });

        update();
    }

    public void update() {
        findViewById(R.id.shop_premium_bought).setVisibility(View.GONE);
        findViewById(R.id.shop_premium_buy).setVisibility(View.GONE);

        startService(new Intent(Actions.IS_PREMIUM)
                .setClass(this, LicenseIntentService.class)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        boolean isPremium = resultCode == ResultCodes.SUCCESS && resultData.getBoolean(BundleExtraKeys.IS_PREMIUM, false);

                        if (isPremium) {
                            findViewById(R.id.shop_premium_bought).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.shop_premium_buy).setVisibility(View.VISIBLE);
                        }
                    }
                }));

        sendBroadcast(new Intent(Actions.DISMISS_EXECUTING_DIALOG));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProductPurchased(String orderId, String productId) {
        update();
    }
}
