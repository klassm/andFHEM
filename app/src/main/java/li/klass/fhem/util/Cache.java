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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Cache<T> {

    private final int cacheSize;

    private class CacheItem {
        private long lastAccess;
        private T value;

        private CacheItem(T value) {
            this.value = value;
            this.lastAccess = System.currentTimeMillis();
        }

        public T access() {
            lastAccess = System.currentTimeMillis();
            return value;
        }
    }

    private Map<String, CacheItem> cacheItems = new HashMap<String, CacheItem>();

    public Cache(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public boolean containsKey(String item) {
        return cacheItems.containsKey(item);
    }

    public T get(String item) {
        return cacheItems.get(item).access();
    }

    public void put(String key, T value) {
        if (containsKey(key)) return;

        if (cacheItems.size() >= cacheSize) {
            removeOldestEntry();
        }

        cacheItems.put(key, new CacheItem(value));

    }

    private void removeOldestEntry() {
        String minimumKey = null;
        long minimumTime = -1;

        Set<String> keys = cacheItems.keySet();
        for (String key : keys) {
            CacheItem item = cacheItems.get(key);

            if (minimumTime == -1 || item.lastAccess < minimumTime) {
                minimumKey = key;
                minimumTime = item.lastAccess;
            }
        }

        cacheItems.remove(minimumKey);
    }
}
