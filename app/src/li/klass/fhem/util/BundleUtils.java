/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.util;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class BundleUtils {
    public static Map<String, String> bundleToMap(Bundle bundle) {
        Map<String, String> result = new HashMap<String, String>();
        if (bundle == null) return result;

        for (String key : bundle.keySet()) {
            result.put(key, bundle.getString(key));
        }
        return result;
    }

    public static Bundle mapToBundle(Map<String, String> map) {
        Bundle result = new Bundle();
        if (map == null) return result;

        for (String key : map.keySet()) {
            result.putString(key, map.get(key));
        }
        return result;
    }
}
