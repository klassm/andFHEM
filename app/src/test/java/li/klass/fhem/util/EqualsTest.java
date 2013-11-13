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

package li.klass.fhem.util;

import org.junit.Test;

import static li.klass.fhem.util.Equals.ignoreCaseEither;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EqualsTest {
    @Test
    public void testIgnoreCaseEither() throws Exception {
        assertThat(ignoreCaseEither(null, (String[]) null), is(false));
        assertThat(ignoreCaseEither(null, "abc"), is(false));
        assertThat(ignoreCaseEither("abc", "abc", "def"), is(true));
        assertThat(ignoreCaseEither("abc", "def", "abc"), is(true));
        assertThat(ignoreCaseEither("abc", "def", "ghi"), is(false));
    }
}
