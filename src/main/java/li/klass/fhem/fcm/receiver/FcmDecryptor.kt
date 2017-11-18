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

import com.google.common.base.Optional
import com.google.common.collect.ImmutableSet
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class FcmDecryptor @Inject constructor(
        val deviceListService: DeviceListService
) {

    fun decrypt(data: Map<String, String>): Map<String, String> {
        if (!data.containsKey("gcmDeviceName")) {
            return data
        }
        val gcmDeviceName = data["gcmDeviceName"] ?: return data
        val device = deviceListService.getDeviceForName<FhemDevice>(gcmDeviceName)

        return device?.xmlListDevice?.getAttribute("cryptKey")?.orNull()
                ?.let { decrypt(data, it) } ?: data
    }

    private fun decrypt(data: Map<String, String>, cryptKey: String): Map<String, String> {
        val cipherOptional = cipherFor(cryptKey)
        if (!cipherOptional.isPresent) {
            return data
        }

        val cipher = cipherOptional.get()
        return data.entries
                .map {
                    if (DECRYPT_KEYS.contains(it.key)) {
                        it.key to decrypt(cipher, it.value)
                    } else it.key to it.value
                }.toMap()
    }

    private fun decrypt(cipher: Cipher, value: String): String {
        try {
            val hexBytes = Hex.decodeHex(value.toCharArray())
            return String(cipher.doFinal(hexBytes))
        } catch (e: Exception) {
            LOG.error("decrypt($value)", e)
            return value
        }

    }

    private fun cipherFor(key: String): Optional<Cipher> {
        return try {
            val keyBytes = key.toByteArray()
            val skey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(keyBytes)
            cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec)
            Optional.of(cipher)
        } catch (e: Exception) {
            LOG.error("cipherFor - cannot create cipher", e)
            Optional.absent<Cipher>()
        }

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FcmDecryptor::class.java)

        private val DECRYPT_KEYS = ImmutableSet.of("type", "notifyId", "changes",
                "deviceName", "tickerText", "contentText", "contentTitle")
    }
}