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

package li.klass.fhem.domain;

import android.util.Log;

import com.google.common.base.Optional;

import java.util.Locale;

import li.klass.fhem.util.NumberUtil;
import li.klass.fhem.util.ValueExtractUtil;

public enum MAXMode {
    ECO, COMFORT, BOOST, AUTO, TEMPORARY, MANUAL;

    public static Optional<MAXMode> modeFor(String value) {
        if (NumberUtil.isDecimalNumber(value)) {
            int mode = ValueExtractUtil.extractLeadingInt(value);
            switch (mode) {
                case 0:
                    return Optional.of(MAXMode.AUTO);
                case 1:
                    return Optional.of(MAXMode.MANUAL);
                case 2:
                    return Optional.of(MAXMode.TEMPORARY);
                case 3:
                    return Optional.of(MAXMode.BOOST);
                default:
                    throw new IllegalArgumentException("don't know how to handle heating mode " + mode);
            }
        } else {
            try {
                return Optional.of(MAXMode.valueOf(value.toUpperCase(Locale.getDefault())));
            } catch (Exception e) {
                Log.e(MAXMode.class.getName(), "cannot set heating mode from value " + value, e);
                return Optional.absent();
            }
        }
    }
}
