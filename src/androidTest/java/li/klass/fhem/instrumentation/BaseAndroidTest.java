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

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;

import li.klass.fhem.instrumentation.infrastructure.matchers.SpoonScreenshotAction;
import li.klass.fhem.service.connection.ConnectionService;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class BaseAndroidTest<A extends Activity> extends ActivityInstrumentationTestCase2<A> {
    protected A activity;
    protected Instrumentation instrumentation;

    public BaseAndroidTest(Class<A> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        instrumentation = getInstrumentation();
        deleteAllPreferences();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteAllPreferences();
        super.tearDown();
    }

    private boolean deleteAllPreferences() {
        return activity.getSharedPreferences(ConnectionService.PREFERENCES_NAME, MODE_PRIVATE).edit().clear().commit();
    }

    @Override
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Exception e) {
            onView(isRoot()).perform(new SpoonScreenshotAction("error", getClass().getName(), getName()));
            throw e;
        }
    }
}
