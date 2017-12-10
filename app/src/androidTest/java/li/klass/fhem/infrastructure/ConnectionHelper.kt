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

import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.matcher.ViewMatchers
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMServerSpec
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.Description

object ConnectionHelper {
    fun selectDummyDataAndRefresh() {
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(ViewMatchers.withId(R.id.connection_spinner)).perform(click())
        onData(CoreMatchers.allOf(CoreMatchers.`is`(CoreMatchers.instanceOf(FHEMServerSpec::class.java)),
                object : BaseMatcher<FHEMServerSpec>() {
                    override fun matches(item: Any?): Boolean =
                            item != null && item is FHEMServerSpec && item.name == "DummyData"

                    override fun describeTo(description: Description?) {
                    }
                })).perform(click())
        onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.close())
        onView(ViewMatchers.withId(R.id.menu_refresh)).perform(click())
    }
}