package li.klass.fhem.connection

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.ui.AvailableConnectionDataAdapter
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Description
import org.junit.Rule
import org.junit.Test

class ConnectionDetailAndroidTest {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(AndFHEMMainActivity::class.java)

    @Test
    @Throws(Exception::class)
    fun create_FHEMWEB_connection() {
        openAddConnectionView()

        val name = "Name" + System.currentTimeMillis();

        onView(withId(R.id.connectionName)).perform(scrollTo(), typeText(name))
        onView(withId(R.id.url)).perform(scrollTo(), typeText("http://bla.blub"))
        onView(withId(R.id.username)).perform(scrollTo(), typeText("username"))
        onView(withId(R.id.password)).perform(scrollTo(), typeText("password"))
        onView(withId(R.id.save)).perform(click())

        assertNameAndDelete(name)
    }

    @Test
    @Throws(Exception::class)
    fun create_Telnet_connection() {
        openAddConnectionView()

        val name = "Name" + System.currentTimeMillis();

        onView(withId(R.id.connectionName)).perform(scrollTo(), typeText(name))
        onView(withId(R.id.connectionType)).perform(click())
        onData(anything()).atPosition(1).perform(click())
        onView(withId(R.id.ip)).perform(scrollTo(), typeText("192.168.0.1"))
        onView(withId(R.id.port)).perform(scrollTo(), typeText("80"))
        onView(withId(R.id.password)).perform(scrollTo(), typeText("password"))
        onView(withId(R.id.save)).perform(click())

        assertNameAndDelete(name)
    }

    private fun assertNameAndDelete(name: String) {
        onData(FHEMServerSpecBaseMatcher(name))
                .inAdapterView(withId(R.id.connectionList))
                .onChildView(withId(R.id.connectionListName))
                .check(matches(withText(name)))
                .perform(longClick())

        onView(withText(R.string.delete)).perform(click());
    }

    private fun openAddConnectionView() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.connection_spinner)).perform(click())
        onData(instanceOf(AvailableConnectionDataAdapter.ManagementPill::class.java)).onChildView(withText(R.string.connectionManage)).perform(click())
        onView(withId(R.id.connection_add)).perform(click())
    }

    private class FHEMServerSpecBaseMatcher(private val name: String) : BaseMatcher<FHEMServerSpec>() {

        override fun describeTo(description: Description) {
            description.appendText("withName " + name)
        }

        override fun matches(item: Any): Boolean = item is FHEMServerSpec && item.name == name
    }
}