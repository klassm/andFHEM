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

package li.klass.fhem.instrumentation;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.test.ActivityInstrumentationTestCase2;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.service.connection.ConnectionService;

import static android.app.Activity.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.isListView;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.withEmptyListView;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.withListViewSize;
import static li.klass.fhem.instrumentation.infrastructure.matchers.MyMatchers.withContent;
import static li.klass.fhem.instrumentation.infrastructure.matchers.MyMatchers.withServerSpec;
import static li.klass.fhem.instrumentation.infrastructure.matchers.SpinnerMatchers.withSelectedItem;
import static org.hamcrest.core.AllOf.allOf;

public class ConnectionEditAndroidTest extends ActivityInstrumentationTestCase2<AndFHEMMainActivity> {

    private AndFHEMMainActivity activity;
    private Instrumentation instrumentation;

    public ConnectionEditAndroidTest() {
        super(AndFHEMMainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
        instrumentation = getInstrumentation();

        activity.sendBroadcast(new Intent(Actions.SHOW_FRAGMENT)
                .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.CONNECTION_LIST));
        instrumentation.waitForIdleSync();
    }

    public void test_create_connection() throws Exception {
        instrumentation.waitForIdleSync();

        onView(allOf(withId(R.id.create), withId(R.id.connectionList), withId(R.id.emptyText)));

        onView(withId(R.id.connectionList)).check(matches(withEmptyListView()));

        onView(withId(R.id.create))
                .perform(click());

        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionName)).perform(typeText("myName"));

        selectServerType(ServerType.FHEMWEB);

        onView(withId(R.id.url))
                .perform(scrollTo())
                .perform(typeText("http://www.google.de"));

        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());
        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionList))
                .check(matches(isDisplayed()))
                .check(matches(withListViewSize(1)));
        onData(withServerSpec().withName("myName").withServerType(ServerType.FHEMWEB).build())
                .inAdapterView(withId(R.id.connectionList))
                .perform(click());
        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionName)).check(matches(withText("myName")));
        onView(withId(R.id.connectionType)).check(matches(withSelectedItem(ServerType.FHEMWEB)));
        onView(withId(R.id.url)).check(matches(withText("http://www.google.de")));
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

    public void test_FHEMWEB_URL_not_blank() {
        // given
        instrumentation.waitForIdleSync();
        onView(allOf(withId(R.id.create), withId(R.id.connectionList), withId(R.id.emptyText)));

        onView(withId(R.id.create)).perform(click());
        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionName)).perform(typeText("myName"));
        selectServerType(ServerType.FHEMWEB);

        // when
        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());

        // then
        onView(withText(String.format(activity.getString(R.string.connectionEmptyError), "URL")))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    public void test_FHEMWEB_URL_starts_with_http() {
        // given
        instrumentation.waitForIdleSync();
        onView(withId(R.id.create)).perform(click());
        instrumentation.waitForIdleSync();
        onView(withId(R.id.connectionName)).perform(typeText("myName"));
        selectServerType(ServerType.FHEMWEB);

        onView(withId(R.id.url))
                .perform(scrollTo())
                .perform(typeText("www.google.de"));

        // when
        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());

        // then
        onView(withText(R.string.connectionUrlHttp))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Override
    protected void tearDown() throws Exception {
        activity.getSharedPreferences(ConnectionService.PREFERENCES_NAME, MODE_PRIVATE).edit().clear().commit();
        super.tearDown();
    }

    protected ViewInteraction selectServerType(ServerType serverType) {
        onView(withId(R.id.connectionType)).check(matches(isDisplayed())).perform(click());
        return onData(withContent(serverType)).inAdapterView(allOf(isDisplayed(), isListView())).perform(click());
    }
}
