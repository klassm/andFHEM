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

import static li.klass.fhem.util.StringUtil.concatenate;
import static li.klass.fhem.util.StringUtil.endsWith;
import static li.klass.fhem.util.StringUtil.isBlank;
import static li.klass.fhem.util.StringUtil.prefixPad;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {

    @Test
    public void testConcatenate() {
        assertEquals("a,b,c", concatenate(new String[]{"a", "b", "c"}, ","));
        assertEquals("", concatenate(new String[]{}, ","));
    }

    @Test
    public void testIsBlank() {
        assertTrue(isBlank(""));
        assertTrue(isBlank(" "));
        assertTrue(isBlank(null));
        assertFalse(isBlank("abc"));
    }

    @Test
    public void testEndsWith() {
        StringBuilder buffer = new StringBuilder("hallowelt123");
        assertThat(endsWith(buffer, "123"), is(true));
        assertThat(endsWith(buffer, "1235"), is(false));
        assertThat(endsWith(buffer, "welt123"), is(true));
        assertThat(endsWith(new StringBuilder(""), "welt123"), is(false));
    }

    @Test
    public void testPrefixPad() {
        assertThat(prefixPad("aa", "0", 6), is("0000aa"));
        assertThat(prefixPad("aa", "0", 1), is("aa"));
        assertThat(prefixPad(null, "0", 1), is("0"));
        assertThat(prefixPad("", "0", 1), is("0"));
    }
}
