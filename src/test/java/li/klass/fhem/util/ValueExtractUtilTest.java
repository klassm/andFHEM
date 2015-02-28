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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class ValueExtractUtilTest {

    @Test
    public void testExtractLeadingNumericText() {
        assertThat(extractLeadingNumericText("abc def", -1)).isEqualTo("");
        assertThat(extractLeadingNumericText("5 abc def ds", 1)).isEqualTo("5.0");
        assertThat(extractLeadingNumericText("5.0 abc def ds", 3)).isEqualTo("5.0");
        assertThat(extractLeadingNumericText("abc", 0)).isEqualTo("");
    }

    @Test
    public void testExtractLeadingInt() {
        assertThat(extractLeadingInt("1 abc")).isEqualTo(1);
        assertThat(extractLeadingInt("1 23")).isEqualTo(1);
    }

    @Test
    public void testExtractLeadingDouble() {
        assertThat(extractLeadingDouble("1.0 abc")).isCloseTo(1.0, offset(0.001));
        assertThat(extractLeadingDouble("2.5 23")).isCloseTo(2.5, offset(0.001));
    }
}
