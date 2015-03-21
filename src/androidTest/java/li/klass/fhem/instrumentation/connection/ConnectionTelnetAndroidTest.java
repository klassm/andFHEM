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
import android.support.test.espresso.ViewInteraction;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.instrumentation.BaseAndroidTest;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static li.klass.fhem.constants.Actions.SHOW_FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.fragments.FragmentType.CONNECTION_LIST;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.isListView;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.withEmptyListView;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.withListViewSize;
import static li.klass.fhem.instrumentation.infrastructure.matchers.MyMatchers.withContent;
import static li.klass.fhem.instrumentation.infrastructure.matchers.MyMatchers.withServerSpec;
import static li.klass.fhem.instrumentation.infrastructure.matchers.SpinnerMatchers.withSelectedItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

public class ConnectionTelnetAndroidTest extends BaseAndroidTest<AndFHEMMainActivity> {

    public ConnectionTelnetAndroidTest() {
        super(AndFHEMMainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity.sendBroadcast(new Intent(SHOW_FRAGMENT)
                .putExtra(FRAGMENT, CONNECTION_LIST));
        instrumentation.waitForIdleSync();
    }

    @SuppressWarnings("unchecked")
    public void test_create_FHEMWEB_connection() throws Exception {
        // given
        String name = "myName";
        String ip = "192.168.0.2";
        String port = "8183";
        String password = "myPassword";

        instrumentation.waitForIdleSync();


        onView(allOf(withId(R.id.create), withId(R.id.connectionList), withId(R.id.emptyText)));

        onView(withId(R.id.connectionList)).check(matches(withEmptyListView()));

        onView(withId(R.id.create)).perform(click());
        instrumentation.waitForIdleSync();

        // when
        onView(withId(R.id.connectionName)).perform(typeText(name));

        selectServerType(ServerType.TELNET);

        onView(withId(R.id.ip))
                .perform(scrollTo())
                .perform(typeText(ip));

        onView(withId(R.id.port))
                .perform(scrollTo())
                .perform(typeText(port));

        onView(withId(R.id.password))
                .perform(scrollTo())
                .perform(typeText(password));

        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());
        instrumentation.waitForIdleSync();

        // then
        onView(withId(R.id.connectionList))
                .check(matches(isDisplayed()))
                .check(matches(withListViewSize(1)));
        onData(withServerSpec().withName(name).withServerType(ServerType.TELNET).build())
                .inAdapterView(withId(R.id.connectionList))
                .perform(click());
        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionName)).check(matches(withText(name)));
        onView(withId(R.id.connectionType)).check(matches(withSelectedItem(ServerType.TELNET)));
        onView(withId(R.id.ip)).check(matches(withText(ip)));
        onView(withId(R.id.port)).check(matches(withText(port)));
        onView(withId(R.id.showPasswordCheckbox)).check(matches(isNotChecked()))
                .check(matches(not(isEnabled())));
    }

    @SuppressWarnings("unchecked")
    protected ViewInteraction selectServerType(ServerType serverType) {
        onView(withId(R.id.connectionType)).check(matches(isDisplayed())).perform(click());
        return onData(withContent(serverType)).inAdapterView(allOf(isDisplayed(), isListView())).perform(click());
    }
}
