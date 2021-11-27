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
package li.klass.fhem.constants

object Actions {
    private val prefix = Actions::class.java.name + "."
    val SHOW_FRAGMENT = prefix + "SHOW_FRAGMENT"
    val SHOW_EXECUTING_DIALOG = prefix + "SHOW_EXECUTING_DIALOG"
    val DISMISS_EXECUTING_DIALOG = prefix + "DISMISS_EXECUTING_DIALOG"
    val BACK = prefix + "BACK"
    val SHOW_TOAST = prefix + "SHOW_TOAST"
    val SHOW_ALERT = prefix + "SHOW_ALERT"
    val DEVICE_WIDGET_TOGGLE = prefix + "DEVICE_WIDGET_TOGGLE"
    val DEVICE_WIDGET_TARGET_STATE = prefix + "DEVICE_WIDGET_TARGET_STATE"
    val DO_UPDATE = prefix + "DO_UPDATE"
    val DO_REMOTE_UPDATE = prefix + "DO_REMOTE_UPDATE"
    val DEVICES_UPDATED = prefix + "REMOTE_DEVICES_UPDATED"
    val REDRAW = prefix + "REDRAW"
    val EXECUTE_COMMAND = prefix + "EXECUTE_COMMAND"
    val REDRAW_WIDGET = prefix + "REDRAW_WIDGET"
    val WIDGET_REQUEST_UPDATE = prefix + "WIDGET_REQUEST_UPDATE"
    val TOP_LEVEL_BACK = prefix + "TOP_LEVEL_BACK"
    val NOTIFICATION_SET_FOR_DEVICE = prefix + "NOTIFICATION_SET_FOR_DEVICE"
    val NOTIFICATION_GET_FOR_DEVICE = prefix + "NOTIFICATION_GET_FOR_DEVICE"
    val CONNECTIONS_CHANGED = prefix + "CONNECTIONS_CHANGED"
    val CONNECTION_UPDATE = prefix + "CONNECTION_UPDATE"
    val CONNECTION_ERROR = prefix + "CONNECTION_ERROR"
    val CONNECTION_ERROR_HIDE = prefix + "CONNECTION_ERROR_HIDE"
    val EXT_DEVICE_STATE_NOTIFY = prefix + "EXT_DEVICE_STATE_NOTIFY"
    val UPDATE_NEXT_ALARM_CLOCK = prefix + "NEXT_ALARM_CLOCK"
}