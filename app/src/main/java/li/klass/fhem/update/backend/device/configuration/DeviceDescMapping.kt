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
package li.klass.fhem.update.backend.device.configuration

import android.content.Context
import li.klass.fhem.resources.ResourceIdMapper
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDescMapping @Inject constructor() {
    private var mapping: JSONObject? = null
    fun descFor(key: String, context: Context): String {
        val value = mapping!!.optString(key, key)
        return resourceFor(value, context)
    }

    fun descFor(resourceId: ResourceIdMapper, context: Context): String {
        return context.getString(resourceId.id)
    }

    private fun resourceFor(value: String, context: Context): String {
        return try {
            context.getString(ResourceIdMapper.valueOf(value).id)
        } catch (e: IllegalArgumentException) {
            value
        }
    }

    init {
        try {
            mapping = JSONObject(DeviceDescMapping::class.java.getResource("/deviceDescMapping.json")?.readText(Charsets.UTF_8)
                    ?: "")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}