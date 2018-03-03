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

package li.klass.fhem.util

import org.slf4j.LoggerFactory

object DimConversionUtil {
    private val LOGGER = LoggerFactory.getLogger(DimConversionUtil::class.java)
    private val BASE = 100

    fun toSeekbarProgress(progress: Double, lowerBound: Double, step: Double): Int {
        val progressAsInt = (progress * BASE).toInt()
        val lowerBoundAsInt = (lowerBound * BASE).toInt()
        val stepAsInt = (step * BASE).toInt()
        val stepZeroSafe = when (stepAsInt) {
            0 -> {
                LOGGER.error("dim step is 0!")
                1
            }
            else -> stepAsInt
        }

        return (progressAsInt - lowerBoundAsInt) / stepZeroSafe
    }

    fun toDimState(progress: Int, lowerBound: Double, step: Double): Double {
        val safeStep = if (step == 0.0) {
            LOGGER.error("dim step is 0!")
            1.0
        } else step
        val lowerBoundAsInt = (lowerBound * BASE).toInt()
        val stepAsInt = (safeStep * BASE).toInt()

        return (progress * stepAsInt + lowerBoundAsInt) / BASE.toDouble()
    }
}
