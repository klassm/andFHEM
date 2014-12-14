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

import static li.klass.fhem.util.ArrayUtil.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ArrayUtilTest {
    @Test
    public void testContains() {
        String[] values = new String[]{"a", "b", "c", "d"};

        assertTrue(contains(values, "a"));
        assertTrue(contains(values, "a", "b"));
        assertFalse(contains(values, "e"));
        assertFalse(contains(null, "e"));
    }

    @Test
    public void testJoin() {
        String[] values = new String[]{"a", "b", "c", "d"};
        assertThat(ArrayUtil.join(values, "|"), is("a|b|c|d"));
    }

    @Test
    public void addToArray() {
        String[] values = new String[]{"a"};
        assertThat(ArrayUtil.addToArray(values, "b"), is(new String[]{"a", "b"}));

        assertThat(ArrayUtil.addToArray(null, "a"), is(new String[]{"a"}));
    }

    @Test
    public void removeFromArray() {
        String[] values = new String[]{"a", "b"};
        assertThat(ArrayUtil.removeFromArray(values, "b"), is(new String[]{"a"}));

        String[] values1 = new String[]{"b", "b"};
        assertThat(ArrayUtil.removeFromArray(values1, "b"), is(new String[]{}));
    }

    @Test
    public void testCopyArray() {
        String[] values = new String[]{"a", "b", "c", "d"};
        String[] copy = ArrayUtil.copyOf(values);
        assertThat(values, is(copy));
        assertFalse(values == copy);
    }
}
