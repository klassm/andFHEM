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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static li.klass.fhem.util.ArrayListUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ArrayListUtilTest {

    private ArrayList<String> values;

    @Before
    public void setUp() {
        values = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
    }

    @Test
    public void testSwapValid() {
        swap(values, 0, 1);
        assertEquals("b", values.get(0));
        assertEquals("a", values.get(1));

        swap(values, 3, 2);
        assertEquals("c", values.get(3));
        assertEquals("d", values.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwapInvalidFirstRange() {
        swap(values, -1, 1);
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwapInvalidSecondRange() {
        swap(values, 0, values.size());
        fail();
    }

    @Test
    public void testMoveUp() {
        moveUp(values, 0);
        assertEquals("a", values.get(0));

        moveUp(values, 1);
        assertEquals("b", values.get(0));
        assertEquals("a", values.get(1));
    }

    @Test
    public void testMoveDown() {
        moveDown(values, 3);
        assertEquals("d", values.get(3));

        moveDown(values, 2);
        assertEquals("d", values.get(2));
        assertEquals("c", values.get(3));
    }

    @Test
    public void testFilter() {
        ArrayList<String> result = ArrayListUtil.filter(values, new Filter<String>() {
            @Override
            public boolean doFilter(String object) {
                return object.equals("a");
            }
        });
        assertEquals(new ArrayList<String>(Arrays.asList("a")), result);
        assertEquals(1, result.size());
    }
}
