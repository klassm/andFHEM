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

package li.klass.fhem.testutil;

import org.joda.time.DateTime;

import java.util.Random;

public class ValueProvider {

    public static final int NUMBER_OF_CHARACTERS = 'z' - 'a';
    private final Random random;

    public ValueProvider() {
        random = new Random();
    }

    public DateTime dateTime() {
        return new DateTime(Math.abs(longValue()));
    }

    public long longValue() {
        return random.nextLong();
    }

    public int intValue(int max) {
        return random.nextInt(max);
    }

    public String lowercaseString(int size) {
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append((char) ('a' + intValue(NUMBER_OF_CHARACTERS)));
        }
        return builder.toString();
    }
}
