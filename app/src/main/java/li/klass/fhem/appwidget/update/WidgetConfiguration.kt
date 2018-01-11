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

package li.klass.fhem.appwidget.update

import android.util.Log
import li.klass.fhem.appwidget.ui.widget.WidgetType
import org.apache.commons.lang3.StringUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.Serializable

data class WidgetConfiguration(val widgetId: Int, val widgetType: WidgetType, val connectionId: String?, val payload: List<String>) : Serializable {

    fun toSaveString(): String? {
        val jsonObject = JSONObject()
        try {
            return jsonObject
                    .put(JSON_WIDGET_ID, widgetId)
                    .put(JSON_WIDGET_TYPE, widgetType)
                    .put(JSON_PAYLOAD, JSONArray(payload))
                    .put(JSON_CONNECTION_ID, connectionId)
                    .toString()
        } catch (e: JSONException) {
            LOGGER.error("cannot create widget configuration", e)
            return null
        }

    }

    companion object {
        private val SAVE_SEPARATOR = "#"
        private val ESCAPED_HASH_REPLACEMENT = "\\\\@"
        private val JSON_WIDGET_ID = "widgetId"
        private val JSON_WIDGET_TYPE = "widgetType"
        private val JSON_PAYLOAD = "payload"
        private val JSON_CONNECTION_ID = "connectionId"

        private val LOGGER = LoggerFactory.getLogger(WidgetConfiguration::class.java)

        fun fromSaveString(value: String?): WidgetConfiguration? =
                if (value == null) null else handleJsonWidgetConfiguration(value)

        private fun handleJsonWidgetConfiguration(value: String): WidgetConfiguration? {
            try {
                val jsonObject = JSONObject(value)
                val widgetType = getWidgetTypeFromName(jsonObject.getString(JSON_WIDGET_TYPE))
                widgetType ?: return null

                return WidgetConfiguration(
                        jsonObject.getInt(JSON_WIDGET_ID),
                        widgetType,
                        getConnectionIdFrom(jsonObject),
                        payloadToList(jsonObject)
                )
            } catch (e: JSONException) {
                LOGGER.error("handleJsonWidgetConfiguration - cannot handle \"{}\"", value)
                return null
            }

        }

        private fun getConnectionIdFrom(jsonObject: JSONObject): String? =
                StringUtils.trimToNull(jsonObject.optString(JSON_CONNECTION_ID))

        @Throws(JSONException::class)
        private fun payloadToList(jsonObject: JSONObject): List<String> {
            val array = jsonObject.getJSONArray(JSON_PAYLOAD)
            return (0 until array.length()).map { array.getString(it) }
        }

        private fun getWidgetTypeFromName(widgetTypeName: String): WidgetType? {
            return try {
                WidgetType.valueOf(widgetTypeName)
            } catch (e: Exception) {
                Log.v(WidgetConfiguration::class.java.name, "cannot find widget type for name " + widgetTypeName, e)
                null
            }
        }

        fun escape(value: String?): String? =
                value?.replace(SAVE_SEPARATOR.toRegex(), ESCAPED_HASH_REPLACEMENT)

        fun unescape(value: String?): String? =
                value?.replace(ESCAPED_HASH_REPLACEMENT.toRegex(), SAVE_SEPARATOR)
    }
}