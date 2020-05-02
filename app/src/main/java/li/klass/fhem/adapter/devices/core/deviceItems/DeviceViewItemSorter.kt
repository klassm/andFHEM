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

package li.klass.fhem.adapter.devices.core.deviceItems

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap

@Singleton
class DeviceViewItemSorter @Inject constructor() {

    fun sortedViewItemsFrom(items: Iterable<XmlDeviceViewItem>): List<XmlDeviceViewItem> {
        val result = items.toMutableList()

        val fieldNameMapping = HashMap<String, String>()
        for (item in items) {
            val showAfterValue = item.showAfter
            if (!showAfterValue.isNullOrEmpty()) {
                val lowerCaseName = item.sortKey.toLowerCase(Locale.getDefault())
                if (XmlDeviceViewItem.FIRST == showAfterValue) {
                    // make sure we are the first one!
                    fieldNameMapping.put(lowerCaseName, "___" + lowerCaseName)
                } else {
                    fieldNameMapping.put(lowerCaseName,
                            showAfterValue.toLowerCase(Locale.getDefault()))
                }
            }
        }

        val fieldNameMappingRecursive = handleRecursiveMappings(fieldNameMapping)
        result.sortWith(Comparator { lhs, rhs ->
            val sortKeyLeft = lhs.sortKey.toLowerCase(Locale.getDefault())
            val sortKeyRight = rhs.sortKey.toLowerCase(Locale.getDefault())

            val left = fieldNameMappingRecursive[sortKeyLeft] ?: sortKeyLeft
            val right = fieldNameMappingRecursive[sortKeyRight] ?: sortKeyRight

            left.compareTo(right)
        })

        return result
    }

    /**
     * Generates a map of showAfter mappings. We also consider mappings pointing to other mappings
     * and so on. To represent the level, we add an underscore as suffix for each level to each
     * mapping value.
     *
     *
     * Careful: We do not consider round-trip mappings! This will result in an infinite loop!
     *
     * @param fieldNameMapping mapping map.
     * @return new mapping map considering recursive mappings.
     */
    private fun handleRecursiveMappings(fieldNameMapping: Map<String, String>): Map<String, String> {
        return fieldNameMapping.entries.map { entry ->
            val key = entry.key
            val value = entry.value

            var level = 0

            var newValue = value
            while (fieldNameMapping.containsKey(newValue)) {
                newValue = fieldNameMapping[newValue]!!
                level++
            }

            for (i in 0..level) {
                newValue += "_"
            }

            key to newValue
        }.toMap()
    }
}
