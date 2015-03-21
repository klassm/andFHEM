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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;

public class MyMatchers {


    public static Matcher<String> withContent(final String content) {
        return new TypeSafeMatcher<String>() {
            private Object was;

            @Override
            public boolean matchesSafely(String o) {
                was = o;
                return o.equals(content);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected: " + String.valueOf(content) + ", but was " + String.valueOf(was));
            }
        };
    }

    public static Matcher<Object> withContent(final Object content) {
        return new TypeSafeMatcher<Object>() {
            private Object was;

            @Override
            public boolean matchesSafely(Object o) {
                was = o;
                return o.equals(content);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected: " + String.valueOf(content) + ", but was " + String.valueOf(was));
            }
        };
    }

    public static FhemServerSpecMatcher.Builder withServerSpec() {
        return new FhemServerSpecMatcher.Builder();
    }

    public static class FhemServerSpecMatcher extends BaseMatcher<Object> {

        private String name;
        private ServerType serverType;
        private Object was;

        public FhemServerSpecMatcher(String name, ServerType serverType) {
            this.name = name;
            this.serverType = serverType;
        }

        @Override
        public boolean matches(Object o) {
            was = o;
            if (!(o instanceof FHEMServerSpec)) {
                return false;
            }
            FHEMServerSpec spec = (FHEMServerSpec) o;
            return !(name != null && !name.equals(spec.getName()))
                    && !(serverType != null && serverType != spec.getServerType());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("expected: {name=" + name + ",serverType=" + serverType + "}, but was " + String.valueOf(was));
        }

        public FhemServerSpecMatcher withName(String name) {
            this.name = name;
            return this;
        }

        public FhemServerSpecMatcher withType(ServerType serverType) {
            this.serverType = serverType;
            return this;
        }


        public static class Builder {
            private String name;
            private ServerType serverType;

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withServerType(ServerType serverType) {
                this.serverType = serverType;
                return this;
            }

            public Matcher<Object> build() {
                return new FhemServerSpecMatcher(name, serverType);
            }
        }
    }
}
