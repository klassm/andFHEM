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

package li.klass.fhem.fcm

import android.content.Context
import com.google.common.base.Strings.isNullOrEmpty
import com.google.firebase.iid.FirebaseInstanceId
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.domain.GCMSendDevice
import li.klass.fhem.service.Command
import li.klass.fhem.service.CommandExecutionService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.ArrayUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GCMSendDeviceService @Inject
constructor(val commandExecutionService: CommandExecutionService,
            val applicationProperties: ApplicationProperties) {

    private fun getRegistrationId(context: Context): String? {
        val senderId = applicationProperties.getStringSharedPreference(PreferenceKeys.FCM_SENDER_ID, context)
        if (senderId == null) {
            LOGGER.info("getRegistrationId - no value for senderId found")
            return null
        }
        LOGGER.debug("getRegistrationId - senderId=$senderId")
        return FirebaseInstanceId.getInstance().getToken(senderId, "FCM")
    }

    fun addSelf(device: GCMSendDevice, context: Context): AddSelfResult {

        val registrationId = getRegistrationId(context)
        if (isNullOrEmpty(registrationId)) {
            return AddSelfResult.FCM_NOT_ACTIVE
        }

        if (ArrayUtil.contains<String>(device.regIds, registrationId)) {
            return AddSelfResult.ALREADY_REGISTERED
        }

        val newRegIds = ArrayUtil.addToArray(device.regIds, registrationId)
        setRegIdsAttributeFor(device, newRegIds, context)
        return AddSelfResult.SUCCESS
    }

    private fun setRegIdsAttributeFor(device: GCMSendDevice, newRegIds: Array<String>, context: Context) {
        val regIdsAttribute = ArrayUtil.join(newRegIds, "|")

        commandExecutionService.executeSync(Command(String.format(ATTR_REG_IDS_COMMAND, device.name, regIdsAttribute)), context)
        device.setRegIds(regIdsAttribute)
    }

    fun isDeviceRegistered(device: GCMSendDevice, context: Context): Boolean {
        val registrationId = getRegistrationId(context)

        return registrationId != null && ArrayUtil.contains(device.regIds, registrationId)
    }

    companion object {
        private val ATTR_REG_IDS_COMMAND = "attr %s regIds %s"
        private val LOGGER = LoggerFactory.getLogger(GCMSendDeviceService::class.java)
    }
}
