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

package li.klass.fhem.util.advertisement;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.license.LicenseManager;

public class AdvertisementUtil {
    private static long lastError = 0;

    public static void addAd(View view, final Activity activity) {
        final LinearLayout adContainer = (LinearLayout) view.findViewById(R.id.adContainer);
        if (adContainer == null) return;

        if (LicenseManager.INSTANCE.isPro() ||
                activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adContainer.setVisibility(View.GONE);
            return;
        }
        adContainer.setVisibility(View.VISIBLE);

        if (System.currentTimeMillis() - lastError < 1000 * 60 * 10) {
            addErrorView(activity, adContainer);
            return;
        }

        AdView adView = new AdView(activity, AdSize.BANNER, AndFHEMApplication.AD_UNIT_ID);

        addListener(activity, adContainer, adView);
        adView.loadAd(new AdRequest());
        adContainer.addView(adView);
    }

    private static void addListener(final Activity activity, final LinearLayout adContainer, AdView adView) {
        adView.setAdListener(new AdListener() {
            @Override
            public void onReceiveAd(Ad ad) {
            }

            @Override
            public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode errorCode) {
                adContainer.removeAllViews();
                addErrorView(activity, adContainer);
                lastError = System.currentTimeMillis();
            }

            @Override
            public void onPresentScreen(Ad ad) {
            }

            @Override
            public void onDismissScreen(Ad ad) {
            }

            @Override
            public void onLeaveApplication(Ad ad) {
            }
        });
    }

    private static void addErrorView(final Activity activity, LinearLayout container) {
        ImageView selfAd = (ImageView) activity.getLayoutInflater().inflate(R.layout.selfad, null);
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
}
