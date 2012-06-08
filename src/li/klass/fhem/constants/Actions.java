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

    public static final String SHOW_UPDATING_DIALOG = prefix + "SHOW_UPDATING_DIALOG";
    public static final String DISMISS_UPDATING_DIALOG = prefix + "DISMISS_UPDATING_DIALOG";
    public static final String SHOW_EXECUTING_DIALOG = prefix + "SHOW_EXECUTING_DIALOG";
    public static final String DISMISS_EXECUTING_DIALOG = prefix + "DISMISS_EXECUTING_DIALOG";

    public static final String SHOW_TOAST = prefix + "SHOW_TOAST";

    public static final String GET_ALL_ROOMS_DEVICE_LIST = prefix + "ALL_ROOMS_DEVICE_LIST";
    public static final String GET_ROOM_DEVICE_LIST = prefix + "GET_ROOM_DEVICE_LIST";
    public static final String GET_ROOM_NAME_LIST = prefix + "GET_ROOM_NAME_LIST";
    public static final String GET_DEVICE_FOR_NAME = prefix + "GET_DEVICE_FOR_NAME";

    public static final String FAVORITE_ROOM_LIST = prefix + "GET_FAVORITE_LIST";
    public static final String FAVORITE_ADD = prefix + "ADD_FAVORITE";
    public static final String FAVORITE_REMOVE = prefix + "REMOVE_FAVORITE";

    public static final String DEVICE_GRAPH = prefix + "DEVICE_GRAPH";
    public static final String DEVICE_TOGGLE_STATE = prefix + "TOGGLE_STATE";
    public static final String DEVICE_SET_STATE = prefix + "SET_STATE";
    public static final String DEVICE_DIM = prefix + "DEVICE_DIM";
    public static final String DEVICE_SET_MODE = prefix + "DEVICE_SET_MODE";
    public static final String DEVICE_SET_DAY_TEMPERATURE = prefix + "DEVICE_SET_DAY_TEMPERATURE";
    public static final String DEVICE_SET_NIGHT_TEMPERATURE = prefix + "DEVICE_SET_NIGHT_TEMPERATURE";
    public static final String DEVICE_SET_WINDOW_OPEN_TEMPERATURE = prefix + "DEVICE_SET_WINDOW_OPEN_TEMPERATURE";
    public static final String DEVICE_SET_TIMETABLE = prefix + "DEVICE_SET_TIMETABLE";
    public static final String DEVICE_RESET_TIMETABLE = prefix + "DEVICE_RESET_TIMETABLE";
    public static final String DEVICE_SET_DESIRED_TEMPERATURE = prefix + "DEVICE_SET_DESIRED_TEMPERATURE";
    public static final String DEVICE_REFRESH_VALUES = prefix + "DEVICE_REFRESH_VALUES";
    public static final String DEVICE_RENAME = prefix + "DEVICE_RENAME";
    public static final String DEVICE_DELETE = prefix + "DEVICE_DELETE";
    public static final String DEVICE_MOVE_ROOM = prefix + "DEVICE_MOVE_ROOM";
    public static final String DEVICE_SET_ALIAS = prefix + "DEVICE_SET_ALIAS";
    public static final String DEVICE_WAKE = prefix + "DEVICE_WAKE";
    public static final String DEVICE_REFRESH_STATE = prefix + "DEVICE_REFRESH_STATE";
    public static final String DEVICE_FLOORPLAN_MOVE = prefix + "DEVICE_FLOORPLAN_MOVE";

    public static final String FLOORPLAN_IMAGE = prefix + "FLOORPLAN_IMAGE";


    public static final String DO_UPDATE = prefix + "DO_UPDATE";


    public static final String EXECUTE_COMMAND = prefix + "EXECUTE_COMMAND";
    public static final String RECENT_COMMAND_LIST = prefix + "RECENT_COMMANDS_LIST";
}
