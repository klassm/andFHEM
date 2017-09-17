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

package li.klass.fhem.constants;

public class Actions {
    private static final String prefix = Actions.class.getName() + ".";

    public static final String SHOW_FRAGMENT = prefix + "SHOW_FRAGMENT";

    public static final String SHOW_EXECUTING_DIALOG = prefix + "SHOW_EXECUTING_DIALOG";
    public static final String DISMISS_EXECUTING_DIALOG = prefix + "DISMISS_EXECUTING_DIALOG";
    public static final String RESEND_LAST_FAILED_COMMAND = prefix + "RESEND_LAST_FAILED_COMMAND";
    public static final String BACK = prefix + "BACK";

    public static final String SHOW_TOAST = prefix + "SHOW_TOAST";
    public static final String SHOW_ALERT = prefix + "SHOW_ALERT";

    public static final String DEVICE_GRAPH = prefix + "DEVICE_GRAPH";
    public static final String DEVICE_GRAPH_DEFINITIONS = prefix + "DEVICE_GRAPH_DEFINITIONS";
    public static final String DEVICE_TOGGLE_STATE = prefix + "TOGGLE_STATE";
    public static final String DEVICE_SET_STATE = prefix + "SET_STATE";
    public static final String DEVICE_DIM = prefix + "DEVICE_DIM";
    public static final String DEVICE_SET_MODE = prefix + "DEVICE_SET_MODE";
    public static final String DEVICE_SET_WINDOW_OPEN_TEMPERATURE = prefix + "DEVICE_SET_WINDOW_OPEN_TEMPERATURE";
    public static final String DEVICE_SET_COMFORT_TEMPERATURE = prefix + "DEVICE_SET_COMFORT_TEMPERATURE";
    public static final String DEVICE_SET_ECO_TEMPERATURE = prefix + "DEVICE_SET_ECO_TEMPERATURE";
    public static final String DEVICE_SET_WEEK_PROFILE = prefix + "DEVICE_SET_WEEK_PROFILE";
    public static final String DEVICE_RESET_WEEK_PROFILE = prefix + "DEVICE_RESET_WEEK_PROFILE";
    public static final String DEVICE_SET_DESIRED_TEMPERATURE = prefix + "DEVICE_SET_DESIRED_TEMPERATURE";
    public static final String DEVICE_RENAME = prefix + "DEVICE_RENAME";
    public static final String DEVICE_DELETE = prefix + "DEVICE_DELETE";
    public static final String DEVICE_MOVE_ROOM = prefix + "DEVICE_MOVE_ROOM";
    public static final String DEVICE_SET_ALIAS = prefix + "DEVICE_SET_ALIAS";
    public static final String DEVICE_WIDGET_TOGGLE = prefix + "DEVICE_WIDGET_TOGGLE";

    public static final String DEVICE_TIMER_NEW = prefix + "DEVICE_TIMER_NEW";
    public static final String DEVICE_TIMER_MODIFY = prefix + "DEVICE_TIMER_MODIFY";

    public static final String DEVICE_SET_SUB_STATE = prefix + "DEVICE_SET_SUB_STATE";
    public static final String DEVICE_SET_SUB_STATES = prefix + "DEVICE_SET_SUB_STATES";

    public static final String LOAD_IMAGE = prefix + "LOAD_IMAGE";

    public static final String DO_UPDATE = prefix + "DO_UPDATE";
    public static final String DO_REMOTE_UPDATE = prefix + "DO_REMOTE_UPDATE";
    public static final String UPDATE_NAVIGATION = prefix + "UPDATE_NAVIGATION";
    public static final String REDRAW = prefix + "REDRAW";

    public static final String EXECUTE_COMMAND = prefix + "EXECUTE_COMMAND";

    public static final String REDRAW_WIDGET = prefix + "REDRAW_WIDGET";
    public static final String WIDGET_REQUEST_UPDATE = prefix + "WIDGET_REQUEST_UPDATE";
    public static final String REDRAW_ALL_WIDGETS = prefix + "REDRAW_ALL_WIDGETS";

    public static final String TOP_LEVEL_BACK = prefix + "TOP_LEVEL_BACK";

    public static final String GCM_REGISTERED = prefix + "GCM_REGISTERED";
    public static final String GCM_REMOVE_ID = prefix + "GCM_REMOVE_ID";
    public static final String GCM_ADD_SELF = prefix + "GCM_ADD_SELF";

    public static final String NOTIFICATION_SET_FOR_DEVICE = prefix + "NOTIFICATION_SET_FOR_DEVICE";
    public static final String NOTIFICATION_GET_FOR_DEVICE = prefix + "NOTIFICATION_GET_FOR_DEVICE";
    public static final String NOTIFICATION_TRIGGER = prefix + "NOTIFICATION_TRIGGER";

    public static final String CONNECTIONS_CHANGED = prefix + "CONNECTIONS_CHANGED";
    public static final String CONNECTION_UPDATE = prefix + "CONNECTION_UPDATE";
    public static final String CONNECTION_SET_SELECTED = prefix + "CONNECTION_SET_SELECTED";

    public static final String CONNECTION_ERROR = prefix + "CONNECTION_ERROR";
    public static final String CONNECTION_ERROR_HIDE = prefix + "CONNECTION_ERROR_HIDE";


    public static final String EXT_DEVICE_STATE_NOTIFY = prefix + "EXT_DEVICE_STATE_NOTIFY";

    public static final String IS_PREMIUM = prefix + "IS_PREMIUM";
    public static final String LOAD_PROPERTIES = prefix + "LOAD_PROPERTIES";

    public static final String RECOGNIZE_VOICE_COMMAND = prefix + "RECOGNIZE_VOICE_COMMAND";
    public static final String SAY = prefix + "SAY";

    public static final String UPDATE_NEXT_ALARM_CLOCK = prefix + "NEXT_ALARM_CLOCK";
    public static final String SCHEDULE_ALARM_CLOCK_UPDATE = prefix + "SCHEDULE_ALARM_CLOCK_UPDATE";
}
