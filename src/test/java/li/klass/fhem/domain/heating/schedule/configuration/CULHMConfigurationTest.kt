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

package li.klass.fhem.domain.heating.schedule.configuration

import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.DayUtil
import li.klass.fhem.util.Reject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

class CULHMConfigurationTest {
    private val configuration = CULHMConfiguration()
    private var weekProfile: WeekProfile<FilledTemperatureInterval, CULHMConfiguration>? = null

    @Before
    fun before() {
        weekProfile = WeekProfile(configuration)
    }

    @Test
    fun testDayRead() {
        configuration.readNode(weekProfile!!, "tempListSat", "08:00 16.5 19:30 20 24:00 16.0")

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).switchTime).isEqualTo("08:00")
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).switchTime).isEqualTo("19:30")
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).switchTime).isEqualTo("24:00")
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).isTimeFixed).isEqualTo(true)

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).temperature).isEqualTo(16.5)
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 1).temperature).isEqualTo(20.0)
        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 2).temperature).isEqualTo(16.0)
    }

    private fun getHeatingIntervalAt(saturday: DayUtil.Day, position: Int): FilledTemperatureInterval {
        val dayProfile = weekProfile!!.getDayProfileFor(saturday)
        Reject.ifNull(dayProfile)

        val interval = dayProfile.getHeatingIntervalAt(position)
        Reject.ifNull<FilledTemperatureInterval>(interval)
        return interval!!
    }

    @Test
    fun shouldIgnoreR_0_Pefixes() {
        configuration.readNode(weekProfile!!, "R_0_tempListSat", "08:00 16.5 19:30 20 24:00 16.0")
        assertThat(weekProfile!!.getDayProfileFor(DayUtil.Day.SATURDAY)).isNotNull()
    }

    @Test
    fun testGenerateCommand() {
        configuration.readNode(weekProfile!!, "tempListSat", "24:00 16.0 08:00 16.0 19:30 20")

        val device = GenericDevice()
        val xmlListDevice = XmlListDevice("dummy", HashMap(), HashMap(), HashMap(), HashMap())
        xmlListDevice.setInternal("NAME", "name")
        device.xmlListDevice = xmlListDevice

        getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).changedTemperature = 23.0

        val commands = configuration.generateScheduleCommands(device.name, weekProfile)

        assertThat(commands).contains("set name tempListSat 08:00 16.0 19:30 20.0 24:00 23.0")
    }

    @Test
    fun testSetPrefixCanBeStillRead() {
        configuration.readNode(weekProfile!!, "tempListSat", "set_  05:45 17.0 07:00 21.0 18:00 17.0 23:00 21.0 24:00 17.0")

        assertThat(getHeatingIntervalAt(DayUtil.Day.SATURDAY, 0).temperature).isEqualTo(17.0)
    }
}
