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

package li.klass.fhem.test.hamcrest;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class IntegerMatcher {

    @org.hamcrest.Factory
    public static org.hamcrest.Matcher<Integer> closeTo(final int expected, final int error) {
        return new BaseMatcher<Integer>() {
            @Override
            public boolean matches(Object o) {
                int value = (Integer) o;

                return expected >= value - error && expected <= value + error;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expected)
                        .appendText(" with error ").appendValue(error);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                int value = (Integer) item;
                int diff = Math.abs(expected - value);

                description.appendText("was ").appendValue(item).appendText(" (diff: ")
                        .appendValue(diff).appendText(")");
            }
        };
    }
}
