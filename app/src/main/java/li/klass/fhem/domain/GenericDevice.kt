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

package li.klass.fhem.domain

import android.content.Context
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.genericview.OverviewViewSettings
import li.klass.fhem.domain.genericview.OverviewViewSettingsCache
import li.klass.fhem.update.backend.xmllist.DeviceNode

@OverviewViewSettings(showState = true, showMeasured = true)
open class GenericDevice : FhemDevice() {
    override fun afterDeviceXMLRead(context: Context) {
        super.afterDeviceXMLRead(context)
        val states = xmlListDevice.states


        val node = states.getOrElse("state", {mostRecentlyMeasuredNode})
        if (node != null) {
            setMeasured(node.measured)
        }
    }

    private val mostRecentlyMeasuredNode: DeviceNode?
        get() {
            val states = xmlListDevice.states
            if (states.isEmpty()) return null

            var mostRecent: DeviceNode? = null
            for (node in states.values) {
                if (mostRecent == null || node.measured != null && node.measured.isAfter(mostRecent.measured)) {
                    mostRecent = node
                }
            }
            return mostRecent
        }

    override fun getExplicitOverviewSettings(): OverviewViewSettings =
            OverviewViewSettingsCache(deviceConfiguration.isShowStateInOverview, deviceConfiguration.isShowMeasuredInOverview)
}
