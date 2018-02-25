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

package li.klass.fhem.update.backend.fhemweb

import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class FhemWebConfigurationService @Inject constructor(
        private val fhemWebDeviceInRoomDeviceListSupplier: FhemWebDeviceInRoomDeviceListSupplier,
        private val roomSorter: RoomsSorter,
        private val sortRoomsAttributeProvider: SortRoomsAttributeProvider,
        private val hiddenRoomsAttributeProvider: HiddenRoomsAttributeProvider,
        private val hiddenGroupsAttributeProvider: HiddenGroupsAttributeProvider,
        private val columnAttributeProvider: ColumnAttributeProvider
) {
    fun sortRooms(roomNames: Collection<String>): List<String> {
        val fhemwebDevice = findFhemWebDevice() ?: return roomNames.sorted()
        val sortRoomsAttribute = sortRoomsAttributeProvider.provideFor(fhemwebDevice)
        logger.info("sortRooms - fhemwebDevice=${fhemwebDevice.name}, sortRoomsAttribute=${sortRoomsAttribute.joinToString(separator = ",")}")
        return roomSorter.sort(roomNames, sortRoomsAttribute)
    }

    fun filterHiddenRoomsIn(roomNames: Collection<String>): Set<String> {
        val fhemwebDevice = findFhemWebDevice() ?: return roomNames.toSet()
        val hiddenRooms = hiddenRoomsAttributeProvider.provideFor(fhemwebDevice)
        logger.info("filterHiddenRoomsIn - fhemwebDevice=${fhemwebDevice.name}, hiddenRoomsAttribute=${hiddenRooms.joinToString(separator = ",")}")

        return roomNames.filter { it !in hiddenRooms }.toSet()
    }

    fun filterHiddenGroupsFrom(roomDeviceList: RoomDeviceList): RoomDeviceList {
        val fhemwebDevice = findFhemWebDevice() ?: return roomDeviceList
        val hiddenGroups = hiddenGroupsAttributeProvider.provideFor(fhemwebDevice)
        logger.info("filterHiddenGroupsFrom - fhemwebDevice=${fhemwebDevice.name}, hiddenGroupsAttribute=${hiddenGroups.joinToString(separator = ",")}")

        return roomDeviceList.filter({
            val groups = it.internalDeviceGroupOrGroupAttributes.map { it.toLowerCase(Locale.getDefault()) }
            !hiddenGroups.containsAll(groups)
        })
    }

    fun getColumnAttributeFor(room: String): List<String> {
        val fhemwebDevice = findFhemWebDevice() ?: return emptyList()
        return columnAttributeProvider.getFor(fhemwebDevice, room)
    }

    private fun findFhemWebDevice(): FhemDevice? = fhemWebDeviceInRoomDeviceListSupplier.get()

    companion object {
        private val logger = LoggerFactory.getLogger(FhemWebConfigurationService::class.java)
    }
}