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
import android.widget.Spinner;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SpinnerMatchers {
    public static Matcher<? super View> withSelectedItem(final Object expected) {
        return new BaseMatcher<View>() {

            String error = null;

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Spinner)) {
                    error = "expected: " + Spinner.class.getName() + ", was: " + o.getClass().getName();
                    return false;
                }
                Spinner spinner = (Spinner) o;
                Object selectedItem = spinner.getSelectedItem();
                boolean equals = selectedItem.equals(expected);
                if (!equals) {
                    error = "expected: " + expected + ", was: " + selectedItem;
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
}
