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

package li.klass.fhem.infrastructure

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import li.klass.fhem.R
import li.klass.fhem.infrastructure.ConnectionHelper.selectDummyDataAndRefresh
import li.klass.fhem.infrastructure.CustomMatchers.DeviceList.withDeviceName

object NavigationHelper {
    fun openDeviceDetailsFor(deviceName: String) {
        selectDummyDataAndRefresh()
        openAllDevices()

        onView(withId(R.id.devices))
                .perform(scrollToHolder(withDeviceName(deviceName)))
                .perform(actionOnHolderItem(withDeviceName(deviceName), click()))

    }

    private fun openAllDevices() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.nav_drawer))
        onView(withText(R.string.alldevices)).perform(click())
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close())
    }
}