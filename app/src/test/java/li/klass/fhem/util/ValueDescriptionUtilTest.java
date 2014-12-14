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

import static li.klass.fhem.util.ValueDescriptionUtil.secondsToTimeString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ValueDescriptionUtilTest {
    @Test
    public void testSecondsToTimeString() {
        assertThat(secondsToTimeString(30), is("30 (s)"));
        assertThat(secondsToTimeString(60), is("1 (m)"));
        assertThat(secondsToTimeString(90), is("1 (m) 30 (s)"));
        assertThat(secondsToTimeString(3600), is("1 (h)"));
        assertThat(secondsToTimeString(3630), is("1 (h) 30 (s)"));
        assertThat(secondsToTimeString(3690), is("1 (h) 1 (m) 30 (s)"));
    }
}
