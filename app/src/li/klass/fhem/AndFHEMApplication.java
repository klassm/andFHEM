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

package li.klass.fhem;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.constants.PreferenceKeys.APPLICATION_VERSION;

public class AndFHEMApplication extends Application {
    private static Context context;
    public static AndFHEMApplication INSTANCE;

    public static final String AD_UNIT_ID = "a14fae70fa236de";
    public static final String PUBLIC_KEY_ENCODED = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1umqueNUDXDqFzXEsRi/kvum6VcI8qiF0OWE7ME6Lm3mHsYHH4W/XIpLWXyh/7FeVpGl36c1UJfBhWCjjLi3d0qechVr/+0RJmXX+r5QZYzE6ZR9jr1g+BUCZj8bB2h+kGL6068pWJJMgzP0mvUBwCxHJioSpdIaBUK4FFyJDz/Nuu8PnThxLJsYEzB6ppyZ8gWYYyeSwg1oNdqcTafLPsh4rAyLJAMOBa9m8cQ7dyEqFXrrM+shYB1JDOJICM6fBNEUDh6kY12QEvh5m6vrAiB7q2eO11rCjZQqSzUEg2Qnd8PFR27ZBQ7CF9mF8VTL71bFOCoM6l/6rIe83SfKWQIDAQAB";
    public static final String PRODUCT_PREMIUM_ID = "li.klass.fhem.premium";
    public static final String PRODUCT_PREMIUM_DONATOR_ID ="li.klass.fhem.premiumdonator";

    private boolean isUpdate = false;
    private String currentApplicationVersion;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        INSTANCE = this;

        setApplicationInformation();
    }

    private void setApplicationInformation() {
        ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
        String savedVersion = applicationProperties.getStringSharedPreference(APPLICATION_VERSION, null);
        currentApplicationVersion = findOutPackageApplicationVersion();

        if (! currentApplicationVersion.equals(savedVersion)) {
            isUpdate = true;
            ApplicationProperties.INSTANCE.setSharedPreference(APPLICATION_VERSION, currentApplicationVersion);
        }
    }

    public static Context getContext() {
        return context;
    }

    private String findOutPackageApplicationVersion() {
        try {
            String pkg = getPackageName();
            return getPackageManager().getPackageInfo(pkg, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AndFHEMApplication.class.getName(), "cannot find the application version", e);
            return "";
        }
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public String getCurrentApplicationVersion() {
        return currentApplicationVersion;
    }
}
