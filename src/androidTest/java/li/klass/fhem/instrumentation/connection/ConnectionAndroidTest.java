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

package li.klass.fhem.instrumentation.connection;

import android.content.Intent;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.instrumentation.BaseAndroidTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.withEmptyListView;

public class ConnectionAndroidTest extends BaseAndroidTest<AndFHEMMainActivity> {

    public ConnectionAndroidTest() {
        super(AndFHEMMainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity.sendBroadcast(new Intent(Actions.SHOW_FRAGMENT)
                .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.CONNECTION_LIST));
        instrumentation.waitForIdleSync();
    }

    public void test_is_empty() throws Exception {
        // given
        instrumentation.waitForIdleSync();

        // then
        onView(withId(R.id.emptyText))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.noConnections)));
        onView(withId(R.id.connectionList)).check(matches(withEmptyListView()));
    }
}
