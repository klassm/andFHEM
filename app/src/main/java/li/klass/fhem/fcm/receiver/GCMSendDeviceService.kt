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

package li.klass.fhem.fcm.receiver

import com.google.firebase.iid.FirebaseInstanceId
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.fcm.AddSelfResult
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GCMSendDeviceService @Inject
constructor(val genericDeviceService: GenericDeviceService,
            val applicationProperties: ApplicationProperties) {

    private fun getRegistrationId(): String? {
        val senderId = applicationProperties.getStringSharedPreference(SettingsKeys.FCM_SENDER_ID)
        if (senderId == null) {
            LOGGER.info("getRegistrationId - no value for senderId found")
            return null
        }
        LOGGER.debug("getRegistrationId - senderId=$senderId")
        return FirebaseInstanceId.getInstance().getToken(senderId, "FCM")?.trim()
    }

    fun addSelf(device: XmlListDevice): AddSelfResult {

        val registrationId = getRegistrationId() ?: return AddSelfResult.FCM_NOT_ACTIVE

        if (isDeviceRegistered(device)) {
            return AddSelfResult.ALREADY_REGISTERED
        }

        val newRegIds = getRegistrationIdsOf(device) + registrationId
        setRegIdsAttributeFor(device, newRegIds)
        return AddSelfResult.SUCCESS
    }

    private fun setRegIdsAttributeFor(device: XmlListDevice, newRegIds: Collection<String>) {
        val regIdsAttribute = newRegIds.joinToString(separator = "|")

        genericDeviceService.setAttribute(device, "regIds", regIdsAttribute)
    }

    fun isDeviceRegistered(device: XmlListDevice): Boolean =
            getRegistrationIdsOf(device).contains(getRegistrationId())

    private fun getRegistrationIdsOf(device: XmlListDevice): List<String> =
            (device.getAttribute("regIds") ?: "")
                    .split("|")

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GCMSendDeviceService::class.java)
    }
}
