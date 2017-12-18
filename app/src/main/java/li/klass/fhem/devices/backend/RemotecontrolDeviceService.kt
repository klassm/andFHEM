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

package li.klass.fhem.devices.backend

import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import javax.inject.Inject

class RemotecontrolDeviceService @Inject constructor() {

    fun getRowsFor(device: FhemDevice): List<Row> {
        val xmlListDevice = device.xmlListDevice
        if (xmlListDevice.type != "remotecontrol") {
            return emptyList()
        }
        val iconPrefix = iconPrefixFor(xmlListDevice)

        return xmlListDevice.attributes
                .filterKeys { it.startsWith("row") }
                .map { Integer.valueOf(it.key.replace(Regex("row[0]?"), "")) to it.value.value }
                .sortedBy { it.first }
                .map { Row(it.first, rowOf(it.second, iconPrefix)) }
                .filter { it.entries.isNotEmpty() }
                .toList()
    }

    private fun rowOf(content: String, iconPrefix: String) =
            if (content.isBlank() || "," == content) emptyList()
            else content.split(",")
                    .map { it.split(":") }
                    .map { row -> Entry(row[0], row.getOrElse(1, { row[0] }).let { iconPathFor(iconPrefix, it) }) }
                    .toList()

    private fun iconPathFor(prefix: String, icon: String) = "$prefix$icon.png"

    private fun iconPrefixFor(xmlListDevice: XmlListDevice): String {
        return "/" +
                xmlListDevice.getAttribute("rc_iconpath").or("icons/remotecontrol") +
                "/" + xmlListDevice.getAttribute("rc_iconprefix").or("black_btn_")
    }

    data class Row(val index: Int, val entries: List<Entry>)

    data class Entry(val command: String, val icon: String?)
}