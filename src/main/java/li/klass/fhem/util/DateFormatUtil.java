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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateFormatUtil {

    private static final DateTimeFormatter FHEM_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ANDFHEM_TIME_FORMAT = DateTimeFormat.forPattern("HH:mm");
    private static final DateTimeFormatter ANDFHEM_DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

    private static Logger LOGGER = LoggerFactory.getLogger(DateFormatUtil.class);

    public static String toReadable(long ms) {
        return toReadable(new DateTime(ms));
    }

    public static String toReadable(DateTime date) {
        if (date == null) return "--";

        return DATE_FORMAT.print(date);
    }


    public static String formatTime(String input) {
        try {
            DateTime dateTime = FHEM_DATE_FORMAT.parseDateTime(input);
            if (dateTime.toLocalDate().equals(LocalDate.now())) {
                return ANDFHEM_TIME_FORMAT.print(dateTime);
            } else {
                return ANDFHEM_DATE_FORMAT.print(dateTime);
            }
        } catch (Exception e) {
            LOGGER.error("cannot format " + input, e);
            return input;
        }
    }

    public static long toMilliSeconds(String in) {
        try {
            return DATE_FORMAT.parseDateTime(in).getMillis();
        } catch (Exception e) {
            return -1;
        }
    }
}
