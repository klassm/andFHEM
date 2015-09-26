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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimConversionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DimConversionUtil.class);

    public static int toSeekbarProgress(float progress, float lowerBound, float step) {
        if (step == 0) {
            LOGGER.error("dim step is 0!");
            step = 1;
        }

        int progressAsInt = (int) (progress * 10);
        int lowerBoundAsInt = (int) (lowerBound * 10);
        int stepAsInt = (int) (step * 10);
        return (progressAsInt - lowerBoundAsInt) / stepAsInt;
    }

    public static float toDimState(int progress, float lowerBound, float step) {
        if (step == 0) {
            LOGGER.error("dim step is 0!");
            step = 1;
        }

        int lowerBoundAsInt = (int) (lowerBound * 10);
        int stepAsInt = (int) (step * 10);

        return (progress * stepAsInt + lowerBoundAsInt) / 10f;
    }
}
