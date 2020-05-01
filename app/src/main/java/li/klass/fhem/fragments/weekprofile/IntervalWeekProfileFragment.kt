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

package li.klass.fhem.fragments.weekprofile

import androidx.navigation.fragment.navArgs
import li.klass.fhem.adapter.weekprofile.BaseWeekProfileAdapter
import li.klass.fhem.adapter.weekprofile.IntervalWeekProfileAdapter
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval
import li.klass.fhem.util.ApplicationProperties
import javax.inject.Inject

class IntervalWeekProfileFragment @Inject constructor(
        private val applicationProperties: ApplicationProperties
) : BaseWeekProfileFragment<FilledTemperatureInterval>() {

    private val args: IntervalWeekProfileFragmentArgs by navArgs()

    var adapter: IntervalWeekProfileAdapter? = null

    override fun updateAdapterWith(weekProfile: WeekProfile<FilledTemperatureInterval, *>) {
        adapter!!.updateData(weekProfile)
    }

    override fun beforeCreateView() {
        val myActivity = activity ?: return
        adapter = IntervalWeekProfileAdapter(myActivity, applicationProperties)
    }

    override fun getAdapter(): BaseWeekProfileAdapter<*> = adapter!!

    override val deviceName: String
        get() = args.deviceName
    override val deviceDisplayName: String
        get() = args.deviceDisplayName
    override val heatingConfigurationProvider: HeatingConfigurationProvider<FilledTemperatureInterval>
        get() = args.heatingConfigurationProvider as HeatingConfigurationProvider<FilledTemperatureInterval>
}
