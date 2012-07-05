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

package li.klass.fhem.util.advertisement;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.widget.LinearLayout;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.license.LicenseManager;

public class AdvertisementUtil {

    public static void addAd(View view, Activity activity) {
        LinearLayout adContainer = (LinearLayout) view.findViewById(R.id.adContainer);
        if (adContainer == null) return;

        if (LicenseManager.INSTANCE.isPro() ||
                activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adContainer.setVisibility(View.GONE);
            return;
        }
        adContainer.setVisibility(View.VISIBLE);

        AdView adView = new AdView(activity, AdSize.BANNER, AndFHEMApplication.AD_UNIT_ID);
        adView.loadAd(new AdRequest());
        adContainer.addView(adView);
    }
}
