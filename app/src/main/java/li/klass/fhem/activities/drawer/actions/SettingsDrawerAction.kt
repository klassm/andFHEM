/*
 *  AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2011, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 *    Boston, MA  02110-1301  USA
 */

package li.klass.fhem.activities.drawer.actions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import li.klass.fhem.ApplicationUrls
import li.klass.fhem.R
import li.klass.fhem.settings.SettingsActivity
import javax.inject.Inject

class SettingsDrawerAction @Inject constructor() : AbstractDrawerAction(R.id.menu_settings) {
    override fun execute(activity: AppCompatActivity) {
        val settingsIntent = Intent(activity, SettingsActivity::class.java)
        activity.startActivityForResult(settingsIntent, Activity.RESULT_OK)
    }
}