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

package li.klass.fhem.domain.core;

import java.util.regex.Pattern;

public enum DeviceStateAdditionalInformationType {
    NUMERIC("[0-9]*", ""),
    DEC_QUARTER("[0-9]*(.(0|25|5[0]?|75))?", ""),
    ANY(".*", ""),
    TIME("[0-9]{2}:[0-9]{2}", "00:00"),
    TIME_WITH_SECOND("[0-9]{2}:[0-9]{2}:[0-9]{2}", "00:00:00"),
    TEMPERATURE("[0-9]*(\\.[0-9]*)?", "00.00");

    private final Pattern pattern;
    private String example;

    DeviceStateAdditionalInformationType(String regex, String example) {
        this.pattern = Pattern.compile(regex);
        this.example = example;
    }

    public boolean matches(String value) {
        return pattern.matcher(value).matches();
    }

    public String getExample() {
        return example;
    }
}
