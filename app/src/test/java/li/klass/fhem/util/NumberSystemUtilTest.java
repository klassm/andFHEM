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

import static li.klass.fhem.util.NumberSystemUtil.hexToDecimal;
import static li.klass.fhem.util.NumberSystemUtil.hexToQuaternary;
import static li.klass.fhem.util.NumberSystemUtil.quaternaryToHex;
import static org.assertj.core.api.Assertions.assertThat;

public class NumberSystemUtilTest {

    @Test
    public void testHexToQuaternary() {
        assertThat(hexToQuaternary("FA91", 4)).isEqualTo("33222101");
        assertThat(hexToQuaternary("FA91", 10)).isEqualTo("0033222101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalHexToQuaternary() {
        hexToQuaternary("XY1982", 4);
    }

    @Test
    public void testQuaternaryToHex() {
        assertThat(quaternaryToHex("33222101")).isEqualTo("FA91");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalQuaternaryToHex() {
        quaternaryToHex("3352229101");
    }

    @Test
    public void testHexToDecimal() {
        assertThat(hexToDecimal("F")).isEqualTo(15);
        assertThat(hexToDecimal("AB")).isEqualTo(171);
        assertThat(hexToDecimal("244EEB")).isEqualTo(2379499);
        assertThat(hexToDecimal("244eeb")).isEqualTo(2379499);
    }
}
