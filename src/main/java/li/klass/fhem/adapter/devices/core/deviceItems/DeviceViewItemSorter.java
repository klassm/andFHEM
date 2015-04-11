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

package li.klass.fhem.adapter.devices.core.deviceItems;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class DeviceViewItemSorter {
    @Inject
    public DeviceViewItemSorter() {
    }

    public List<DeviceViewItem> sortedViewItemsFrom(Iterable<DeviceViewItem> items) {
        List<DeviceViewItem> result = newArrayList(items);

        Map<String, String> fieldNameMapping = newHashMap();
        for (DeviceViewItem item : items) {
            String showAfterValue = item.getShowAfterValue();
            if (!isNullOrEmpty(showAfterValue)) {
                String lowerCaseName = item.getName().toLowerCase(Locale.getDefault());
                if (DeviceViewItem.FIRST.equals(showAfterValue)) {
                    // make sure we are the first one!
                    fieldNameMapping.put(lowerCaseName, "___" + lowerCaseName);
                } else {
                    fieldNameMapping.put(lowerCaseName,
                            showAfterValue.toLowerCase(Locale.getDefault()));
                }
            }
        }

        final Map<String, String> fieldNameMappingRecursive = handleRecursiveMappings(fieldNameMapping);
        Collections.sort(result, new Comparator<DeviceViewItem>() {
            @Override
            public int compare(DeviceViewItem lhs, DeviceViewItem rhs) {
                String lhsName = lhs.getName().toLowerCase(Locale.getDefault());
                String rhsName = rhs.getName().toLowerCase(Locale.getDefault());

                if (fieldNameMappingRecursive.containsKey(lhsName)) {
                    lhsName = fieldNameMappingRecursive.get(lhsName);
                }
                if (fieldNameMappingRecursive.containsKey(rhsName)) {
                    rhsName = fieldNameMappingRecursive.get(rhsName);
                }
                return lhsName.compareTo(rhsName);
            }
        });

        return result;
    }

    /**
     * Generates a map of showAfter mappings. We also consider mappings pointing to other mappings
     * and so on. To represent the level, we add an underscore as suffix for each level to each
     * mapping value.
     * <p/>
     * Careful: We do not consider round-trip mappings! This will result in an infinite loop!
     *
     * @param fieldNameMapping mapping map.
     * @return new mapping map considering recursive mappings.
     */
    private Map<String, String> handleRecursiveMappings(Map<String, String> fieldNameMapping) {
        Map<String, String> newMapping = newHashMap();
        for (String key : fieldNameMapping.keySet()) {
            String value = fieldNameMapping.get(key);

            int level = 0;

            String newValue = value;
            while (fieldNameMapping.containsKey(newValue)) {
                newValue = fieldNameMapping.get(newValue);
                level++;
            }

            for (int i = 0; i <= level; i++) {
                newValue += "_";
            }
            newMapping.put(key, newValue);
        }

        return newMapping;
    }
}
