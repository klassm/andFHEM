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

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.activities.StartupActivity;
import li.klass.fhem.activities.base.DeviceNameSelectionActivity;
import li.klass.fhem.activities.graph.ChartingActivity;
import li.klass.fhem.dagger.AndroidModule;
import li.klass.fhem.dagger.ApplicationModule;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.InstalledApplications;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.PreferenceKeys.APPLICATION_VERSION;

public class AndFHEMApplication extends Application {
    public static final String TAG = AndFHEMApplication.class.getName();
    public static final String ANDFHEM_MAIL = "andfhem@klass.li";
    public static final String AD_UNIT_ID = "a14fae70fa236de";
    public static final String PUBLIC_KEY_ENCODED = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1umqueNUDXDqFzXEsRi/kvum6VcI8qiF0OWE7ME6Lm3mHsYHH4W/XIpLWXyh/7FeVpGl36c1UJfBhWCjjLi3d0qechVr/+0RJmXX+r5QZYzE6ZR9jr1g+BUCZj8bB2h+kGL6068pWJJMgzP0mvUBwCxHJioSpdIaBUK4FFyJDz/Nuu8PnThxLJsYEzB6ppyZ8gWYYyeSwg1oNdqcTafLPsh4rAyLJAMOBa9m8cQ7dyEqFXrrM+shYB1JDOJICM6fBNEUDh6kY12QEvh5m6vrAiB7q2eO11rCjZQqSzUEg2Qnd8PFR27ZBQ7CF9mF8VTL71bFOCoM6l/6rIe83SfKWQIDAQAB";
    public static final String INAPP_PREMIUM_ID = "li.klass.fhem.premium";
    public static final String INAPP_PREMIUM_DONATOR_ID = "li.klass.fhem.premiumdonator";
    public static final String PREMIUM_PACKAGE = "li.klass.fhempremium";
    public static final int PREMIUM_ALLOWED_FREE_CONNECTIONS = 1;

    private static Context context;
    private static AndFHEMApplication application;
    @Inject
    ApplicationProperties applicationProperties;
    private boolean isUpdate = false;
    private String currentApplicationVersion;
    private boolean isTablet;
    private ObjectGraph graph;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context newContext) {
        context = newContext;
    }

    public static AndFHEMApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setDefaultUncaughtExceptionHandler();
        setStrictMode();

        context = getApplicationContext();
        application = this;

        graph = ObjectGraph.create(getModules().toArray());

        inject(this);
        setApplicationInformation();
    }

    private static void setDefaultUncaughtExceptionHandler() {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    Log.e(TAG, String.format("Uncaught Exception detected in thread %s", t.toString()), e);
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Could not set the Default Uncaught Exception Handler", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setStrictMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectAll()
                        .setClassInstanceLimit(ChartingActivity.class, 3)
                        .setClassInstanceLimit(StartupActivity.class, 3)
                        .setClassInstanceLimit(AndFHEMMainActivity.class, 3)
                        .setClassInstanceLimit(DeviceNameSelectionActivity.class, 3)
                        .penaltyLog()
                        .build());

                StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder();

                StrictMode.setThreadPolicy(builder
                        .detectDiskReads()
                        .detectDiskWrites()
                        .permitDiskReads()
                        .permitDiskWrites()
                        .detectCustomSlowCalls()
                        .detectNetwork()
                        .penaltyLog()
                        .build());
            }
        } catch (Exception e) {
            Log.v(TAG, "cannot enable strict mode", e);
            // ignore
        }
    }

    protected List<Object> getModules() {
        return newArrayList(
                new ApplicationModule(),
                new AndroidModule(this)
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }

    private void setApplicationInformation() {
        String savedVersion = applicationProperties.getStringSharedPreference(APPLICATION_VERSION, null);
        currentApplicationVersion = findOutPackageApplicationVersion();

        if (!currentApplicationVersion.equals(savedVersion)) {
            isUpdate = true;
            applicationProperties.setSharedPreference(APPLICATION_VERSION, currentApplicationVersion);
        }
    }

    public static int getAndroidSDKLevel() {
        return Build.VERSION.SDK_INT;
    }

    private String findOutPackageApplicationVersion() {
        try {
            String pkg = getPackageName();
            return getPackageManager().getPackageInfo(pkg, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(AndFHEMApplication.class.getName(), "cannot find the application version", e);
            return "";
        }
    }

    public boolean isAndFHEMAlreadyInstalled() {
        List<InstalledApplications.InstalledApplication> installedApps = InstalledApplications.getInstalledApps();
        for (InstalledApplications.InstalledApplication installedApp : installedApps) {
            if (installedApp.getPackageName().startsWith("li.klass.fhem")
                    && !installedApp.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public ObjectGraph getGraph() {
        return graph;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    @SuppressWarnings("unused")
    public String getCurrentApplicationVersion() {
        return currentApplicationVersion;
    }

    public void setIsTablet(boolean tablet) {
        isTablet = tablet;
    }
}
