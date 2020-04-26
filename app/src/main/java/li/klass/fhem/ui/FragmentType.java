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

package li.klass.fhem.ui;

import androidx.annotation.Nullable;

public enum FragmentType {
    FAVORITES,
    ROOM_LIST,
    ALL_DEVICES,
    CONVERSION,
    DEVICE_DETAIL,
    FROM_TO_WEEK_PROFILE,
    INTERVAL_WEEK_PROFILE,
    FLOORPLAN,
    ROOM_DETAIL,
    SEND_COMMAND,
    DEVICE_SELECTION,
    TIMER_OVERVIEW,
    TIMER_DETAIL,
    CONNECTION_LIST,
    CONNECTION_DETAIL,
    WEB_VIEW,
    OTHER_WIDGETS_FRAGMENT,
    SEARCH,
    FCM_HISTORY;

    @Nullable
    public static FragmentType forEnumName(String name) {
        try {
            return FragmentType.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
