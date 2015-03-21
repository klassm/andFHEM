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

package li.klass.fhem.instrumentation.infrastructure.matchers;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class AdapterViewMatchers {
    public static Matcher<? super View> withEmptyListView() {
        return withListViewSize(0);
    }

    public static Matcher<? super View> withListViewSize(final int expectedCount) {
        return new TypeSafeMatcher<View>() {
            String error;

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof ListView)) {
                    error = "Expected: " + ListView.class.getName() + ", was: " + view.getClass().getName();
                    return false;
                }
                ListAdapter adapter = ((ListView) view).getAdapter();
                if (adapter == null) {
                    error = "Adapter is null";
                    return false;
                }
                int count = adapter.getCount();
                if (count != expectedCount) {
                    error = "Expected items: " + expectedCount + ", was: " + count;
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(error);
            }
        };
    }

    public static Matcher<View> isListView() {
        return new TypeSafeMatcher<View>() {

            private View was;

            @Override
            public boolean matchesSafely(View view) {
                was = view;
                return view instanceof ListView;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected: instance of ListView, was: " + was.getClass().getName());
            }
        };
    }

}
