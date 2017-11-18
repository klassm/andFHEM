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

package li.klass.fhem.activities.locale

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.activities.locale.LocaleIntentConstants.RESULT_CONDITION_SATISFIED
import li.klass.fhem.activities.locale.LocaleIntentConstants.RESULT_CONDITION_UNSATISFIED
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ConditionQueryLocaleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var deviceListService: DeviceListService

    init {
        val daggerComponent = AndFHEMApplication.application!!.daggerComponent
        daggerComponent.inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        LOG.info("onReceive - " + intent.action)

        val deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME)
        val targetState = intent.getStringExtra(BundleExtraKeys.DEVICE_TARGET_STATE)

        val device = deviceListService.getDeviceForName<FhemDevice>(deviceName, null)
        if (device == null) {
            resultCode = RESULT_CONDITION_UNSATISFIED
            return
        }
        val satisfied = targetState != null && (targetState.equals(device.internalState, ignoreCase = true) || device.internalState.matches(targetState.toRegex()))
        resultCode = if (satisfied) {
            RESULT_CONDITION_SATISFIED
        } else {
            RESULT_CONDITION_UNSATISFIED
        }
    }

    companion object {
        val LOG = LoggerFactory.getLogger(ConditionQueryLocaleReceiver::class.java)!!
    }
}
