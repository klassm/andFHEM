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

import li.klass.fhem.adapter.weekprofile.BaseWeekProfileAdapter
import li.klass.fhem.adapter.weekprofile.FromToWeekProfileAdapter
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.heating.schedule.WeekProfile
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval

class FromToWeekProfileFragment : BaseWeekProfileFragment<FromToHeatingInterval>() {

    private var myAdapter: FromToWeekProfileAdapter? = null

    override fun updateAdapterWith(weekProfile: WeekProfile<FromToHeatingInterval, *>) {
        myAdapter!!.updateData(weekProfile)
    }

    override fun beforeCreateView() {
        val myActivity = activity ?: return
        myAdapter = FromToWeekProfileAdapter(myActivity)
    }

    override fun getAdapter(): BaseWeekProfileAdapter<*> = myAdapter!!

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
