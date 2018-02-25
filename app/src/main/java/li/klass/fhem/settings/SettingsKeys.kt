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

package li.klass.fhem.settings

object SettingsKeys {
    const val SHOW_HIDDEN_DEVICES = "prefShowHiddenDevices"
    const val DEVICE_COLUMN_WIDTH = "DEVICE_COLUMN_WIDTH"
    const val APPLICATION_VERSION = "APPLICATION_VERSION"
    const val UPDATE_ON_ROOM_OPEN = "UPDATE_ON_ROOM_OPEN"
    const val UPDATE_ON_APPLICATION_START = "UPDATE_ON_APPLICATION_START"
    const val SHOW_SET_VALUE_BUTTONS = "SHOW_SET_VALUE_BUTTONS"
    const val DEVICE_LIST_RIGHT_PADDING = "DEVICE_LIST_PADDING_RIGHT"
    const val GCM_WIDGET_UPDATE = "GCM_WIDGET_UPDATE"
    const val FCM_SENDER_ID = "FCM_SENDER_ID"
    const val SEND_LAST_ERROR = "SEND_LAST_ERROR"
    const val STARTUP_VIEW = "STARTUP_VIEW"
    const val CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT"
    const val COMMAND_EXECUTION_RETRIES = "COMMAND_EXECUTION_RETRIES"
    const val FHEMWEB_DEVICE_NAME = "DEVICE_NAME"
    const val STARTUP_PASSWORD = "PASSWORD"
    const val SEND_APP_LOG = "SEND_APP_LOG"
    const val ALLOW_REMOTE_UPDATE = "prefWidgetRemoteUpdate"
    const val CLEAR_TRUSTED_CERTIFICATES = "CLEAR_TRUSTED_CERTIFICATES"
    const val GRAPH_DEFAULT_TIMESPAN = "GRAPH_DEFAULT_TIMESPAN"
    const val WIDGET_UPDATE_INTERVAL_WLAN = "WIDGET_UPDATE_INTERVAL_WLAN"
    const val WIDGET_UPDATE_INTERVAL_MOBILE = "WIDGET_UPDATE_INTERVAL_MOBILE"
    const val AUTO_UPDATE_TIME = "AUTO_UPDATE_TIME"
    const val EXPORT_SETTINGS = "EXPORT_SETTINGS"
    const val IMPORT_SETTINGS = "IMPORT_SETTINGS"
    const val FCM_KEEP_MESSAGES_DAYS = "FCM_KEEP_MESSAGES_DAYS"
    const val THEME = "THEME"

    const val DEVICE_FUNCTIONALITY_ORDER_VISIBLE = "DEVICE_FUNCTIONALITY_ORDER_VISIBLE"
    const val DEVICE_TYPE_FUNCTIONALITY_ORDER_INVISIBLE = "DEVICE_TYPE_FUNCTIONALITY_ORDER_INVISIBLE"

    const val SELECTED_CONNECTION = "SELECTED_CONNECTION"
}
