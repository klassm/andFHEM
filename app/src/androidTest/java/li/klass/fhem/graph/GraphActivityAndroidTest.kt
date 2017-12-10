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

package li.klass.fhem.graph

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.Toolbar
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.infrastructure.CustomMatchers.Android.withToolbarTitle
import li.klass.fhem.infrastructure.NavigationHelper.openDeviceDetailsFor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test


class GraphActivityAndroidTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule(AndFHEMMainActivity::class.java)

    @Test
    fun should_load_graph_view() {
        openDeviceDetailsFor("wetterstation")

        onView(allOf(withId(R.id.button), withText("weblink_wetterstation"))).perform(click());

        onView(isAssignableFrom(Toolbar::class.java))
                .check(matches(withToolbarTitle(containsString("wetterstation"))))
    }
}