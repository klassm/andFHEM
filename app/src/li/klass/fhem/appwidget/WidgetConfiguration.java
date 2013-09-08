/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.appwidget;

import android.util.Log;
import li.klass.fhem.appwidget.view.WidgetType;

import java.io.Serializable;

public class WidgetConfiguration implements Serializable {
    public static final String SAVE_SEPARATOR = "#";
    public static final String ESCAPE_HASH_REPLACEMENT = "\\\\@";

    public final int widgetId;
    public final String deviceName;
    public final WidgetType widgetType;
    public final String payload;

    public WidgetConfiguration(int widgetId, String deviceName, WidgetType widgetType) {
        this(widgetId, deviceName, widgetType, null);
    }

    public WidgetConfiguration(int widgetId, String deviceName, WidgetType widgetType, String payload) {
        this.widgetId = widgetId;
        this.deviceName = deviceName;
        this.widgetType = widgetType;
        this.payload = payload;
    }

    public String toSaveString() {
        String deviceNameValidated = escape(deviceName);
        String saveString = widgetId + SAVE_SEPARATOR
                + deviceNameValidated + SAVE_SEPARATOR
                + widgetType.name();

        if (payload != null) {
            saveString += SAVE_SEPARATOR + escape(payload);
        }

        return saveString;
    }

    public static WidgetConfiguration fromSaveString(String value) {
        if (value == null) return null;

        String[] parts = value.split(SAVE_SEPARATOR);
        if (parts.length < 3) return null;

        String widgetTypeName = parts[2];

        WidgetType widgetType = getWidgetTypeFromName(widgetTypeName);
        if (widgetType == null) {
            return null;
        }

        String widgetId = parts[0];
        String deviceName = unescape(parts[1]);

        String payload = null;
        if (parts.length == 4) {
            payload = unescape(parts[3]);
        }

        return new WidgetConfiguration(Integer.valueOf(widgetId), deviceName, widgetType, payload);
    }

    private static WidgetType getWidgetTypeFromName(String widgetTypeName) {
        try {
            return WidgetType.valueOf(widgetTypeName);
        } catch (Exception e) {
            Log.e(WidgetConfiguration.class.getName(), "cannot find widget type for name " + widgetTypeName, e);
            return null;
        }
    }

    static String escape(String value) {
        if (value == null) return null;
        return value.replaceAll(SAVE_SEPARATOR, ESCAPE_HASH_REPLACEMENT);
    }

    static String unescape(String value) {
        if (value == null) return null;
        return value.replaceAll(ESCAPE_HASH_REPLACEMENT, SAVE_SEPARATOR);
    }
}