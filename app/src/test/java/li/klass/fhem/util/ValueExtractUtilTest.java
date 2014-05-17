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

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingNumericText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;

public class ValueExtractUtilTest {

    @Test
    public void testExtractLeadingNumericText() {
        assertThat(extractLeadingNumericText("abc def", -1), is(""));
        assertThat(extractLeadingNumericText("5 abc def ds", 1), is("5.0"));
        assertThat(extractLeadingNumericText("5.0 abc def ds", 3), is("5.0"));
        assertThat(extractLeadingNumericText("abc", 0), is(""));
    }

    @Test
    public void testExtractLeadingInt() {
        assertEquals(1, extractLeadingInt("1 abc"));
        assertEquals(1, extractLeadingInt("1 23"));
    }

    @Test
    public void testExtractLeadingDouble() {
        assertThat(extractLeadingDouble("1.0 abc"), is(closeTo(1.0, 0.001)));
        assertThat(extractLeadingDouble("2.5 23"), is(closeTo(2.5, 0.001)));
    }
}
