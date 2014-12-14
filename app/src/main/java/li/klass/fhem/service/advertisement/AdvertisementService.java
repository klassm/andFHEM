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

package li.klass.fhem.service.advertisement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.util.FhemResultReceiver;

import static li.klass.fhem.constants.BundleExtraKeys.IS_PREMIUM;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

@Singleton
public class AdvertisementService {
    private static final String TAG = AdvertisementService.class.getName();
    private static long lastErrorTimestamp = 0;

    @Inject
    @ForApplication
    Context applicationContext;

    public void addAd(final View view, final Activity activity) {

        applicationContext.startService(new Intent(Actions.IS_PREMIUM)
                        .setClass(applicationContext, LicenseIntentService.class)
                        .putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                boolean isPremium = resultCode == SUCCESS && resultData.getBoolean(IS_PREMIUM, false);
                                showAdsBasedOnPremium(isPremium, view, activity);
                            }
                        })
        );
    }

    private void showAdsBasedOnPremium(boolean isPremium, View view, Activity activity) {
        boolean showAds = true;
        final LinearLayout adContainer = (LinearLayout) view.findViewById(R.id.adContainer);

        if (adContainer == null) {
            Log.i(TAG, "cannot find adContainer");
            return;
        } else if (isPremium) {
            showAds = false;
            Log.i(TAG, "found premium version, skipping ads");
        } else {
            Resources resources = activity.getResources();
            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    && !resources.getBoolean(R.bool.isTablet)) {
                showAds = false;
                Log.i(TAG, "found landscape orientation, skipping ads");
            }
        }

        if (!showAds) {
            adContainer.setVisibility(View.GONE);
        } else if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity) != ConnectionResult.SUCCESS) {
            addErrorView(activity, adContainer);
            Log.e(TAG, "cannot find PlayServices");
        } else {
            adContainer.setVisibility(View.VISIBLE);
            adContainer.removeAllViews();

            if (System.currentTimeMillis() - lastErrorTimestamp < 1000 * 60 * 10) {
                addErrorView(activity, adContainer);
                Log.i(TAG, "still in timeout, showing error view");
                return;
            }

            Log.i(TAG, "showing ad");

            AdView adView = new AdView(activity);
            adView.setAdUnitId(AndFHEMApplication.AD_UNIT_ID);
            adView.setAdSize(AdSize.BANNER);

            addListener(activity, adContainer, adView);
            adView.loadAd(new AdRequest.Builder().build());
            adContainer.addView(adView);
        }
    }

    private static void addErrorView(final Activity activity, LinearLayout container) {
        ImageView selfAd = (ImageView) activity.getLayoutInflater().inflate(R.layout.selfad, container, false);
        selfAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.PREMIUM);
                activity.sendBroadcast(intent);
            }
        });
        container.addView(selfAd);
    }

    private static void addListener(final Activity activity, final LinearLayout adContainer, AdView adView) {
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);

                adContainer.removeAllViews();
                addErrorView(activity, adContainer);
                lastErrorTimestamp = System.currentTimeMillis();
                Log.i(TAG, "failed to receive ads, showing error view");
            }
        });
    }
}
