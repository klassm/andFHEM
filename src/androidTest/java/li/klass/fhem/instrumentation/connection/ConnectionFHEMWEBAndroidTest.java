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
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static li.klass.fhem.constants.Actions.SHOW_FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.fhem.connection.ServerType.FHEMWEB;
import static li.klass.fhem.fragments.FragmentType.CONNECTION_LIST;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.isListView;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.withEmptyListView;
import static li.klass.fhem.instrumentation.infrastructure.matchers.AdapterViewMatchers.withListViewSize;
import static li.klass.fhem.instrumentation.infrastructure.matchers.MyMatchers.withContent;
import static li.klass.fhem.instrumentation.infrastructure.matchers.MyMatchers.withServerSpec;
import static li.klass.fhem.instrumentation.infrastructure.matchers.SpinnerMatchers.withSelectedItem;
import static org.hamcrest.core.AllOf.allOf;

public class ConnectionFHEMWEBAndroidTest extends BaseAndroidTest<AndFHEMMainActivity> {

    public ConnectionFHEMWEBAndroidTest() {
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
        String primaryUrl = "http://www.google.de";
        String secondaryUrl = "http://www.some-secondary-url.de";

        instrumentation.waitForIdleSync();

        onView(allOf(withId(R.id.create), withId(R.id.connectionList), withId(R.id.emptyText)));
        onView(withId(R.id.connectionList)).check(matches(withEmptyListView()));

        onView(withId(R.id.create))
                .perform(click());

        // when
        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionName)).perform(typeText("myName"));

        selectServerType(FHEMWEB);

        onView(withId(R.id.url))
                .perform(scrollTo())
                .perform(typeText(primaryUrl));
        onView(withId(R.id.alternate_url))
                .perform(scrollTo())
                .perform(typeText(secondaryUrl));

        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());
        instrumentation.waitForIdleSync();

        // then
        onView(withId(R.id.connectionList))
                .check(matches(isDisplayed()))
                .check(matches(withListViewSize(1)));
        onData(withServerSpec().withName("myName").withServerType(FHEMWEB).build())
                .inAdapterView(withId(R.id.connectionList))
                .perform(click());
        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionName)).check(matches(withText("myName")));
        onView(withId(R.id.connectionType)).check(matches(withSelectedItem(FHEMWEB)));
        onView(withId(R.id.url)).check(matches(withText(primaryUrl)));
        onView(withId(R.id.alternate_url)).check(matches(withText(secondaryUrl)));
    }

    @SuppressWarnings("unchecked")
    public void test_FHEMWEB_URL_not_blank() {
        // given
        instrumentation.waitForIdleSync();
        onView(allOf(withId(R.id.create), withId(R.id.connectionList), withId(R.id.emptyText)));

        onView(withId(R.id.create)).perform(click());
        instrumentation.waitForIdleSync();

        onView(withId(R.id.connectionName)).perform(typeText("myName"));
        selectServerType(FHEMWEB);

        // when
        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());

        // then
        onView(withText(String.format(activity.getString(R.string.connectionEmptyError), "URL")))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    public void test_URL_starts_with_http() {
        // given
        instrumentation.waitForIdleSync();
        onView(withId(R.id.create)).perform(click());
        instrumentation.waitForIdleSync();
        onView(withId(R.id.connectionName)).perform(typeText("myName"));
        selectServerType(FHEMWEB);

        // when
        onView(withId(R.id.url))
                .perform(scrollTo())
                .perform(typeText("www.google.de"));
        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());

        // then
        onView(withText(R.string.connectionUrlHttp))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    public void test_alternate_URL_starts_with_http() {
        // given
        instrumentation.waitForIdleSync();
        onView(withId(R.id.create)).perform(click());
        instrumentation.waitForIdleSync();
        onView(withId(R.id.connectionName)).perform(typeText("myName"));
        selectServerType(FHEMWEB);

        onView(withId(R.id.url))
                .perform(scrollTo())
                .perform(typeText("http://www.google.de"));

        // when
        onView(withId(R.id.alternate_url))
                .perform(scrollTo())
                .perform(typeText("www.google.de"));
        onView(withId(R.id.save))
                .perform(scrollTo())
                .perform(click());

        // then
        onView(withText(R.string.connectionUrlHttp))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @SuppressWarnings("unchecked")
    protected ViewInteraction selectServerType(ServerType serverType) {
        onView(withId(R.id.connectionType)).check(matches(isDisplayed())).perform(click());
        return onData(withContent(serverType)).inAdapterView(allOf(isDisplayed(), isListView())).perform(click());
    }
}
