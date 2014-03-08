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

package li.klass.fhem.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.common.collect.Lists;

import java.util.List;

import li.klass.fhem.AndFHEMApplication;

public class InstalledApplications {

    public static class InstalledApplication {
        private String packageName = "";
        private String versionName = "";
        private int versionCode = 0;
        public String toString() {
            return packageName + "\t" + versionName + "\t" + versionCode;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getVersionName() {
            return versionName;
        }

        public int getVersionCode() {
            return versionCode;
        }
    }

    public static List<InstalledApplication> getInstalledApps() {
        return getInstalledApps(false);
    }

    public static List<InstalledApplication> getInstalledApps(boolean getSysPackages) {
        List<InstalledApplication> result = Lists.newArrayList();
        PackageManager packageManager = AndFHEMApplication.getContext().getPackageManager();
        if (packageManager == null) return result;

        List<PackageInfo> packages = packageManager.getInstalledPackages(0);

        for (PackageInfo app : packages) {
            if ((!getSysPackages) && (app.versionName == null)) {
                continue;
            }
            InstalledApplication newInfo = new InstalledApplication();
            newInfo.packageName = app.packageName;
            newInfo.versionName = app.versionName;
            newInfo.versionCode = app.versionCode;
            result.add(newInfo);
        }
        return result;
    }
}
