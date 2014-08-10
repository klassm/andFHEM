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

import com.google.common.base.Joiner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import li.klass.fhem.appwidget.view.WidgetType;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class WidgetConfiguration implements Serializable {
    public static final String SAVE_SEPARATOR = "#";
    public static final String PAYLOAD_SEPARATOR = "+";
    public static final String PAYLOAD_SEPARATOR_REGEXP = "\\" + PAYLOAD_SEPARATOR;
    public static final String ESCAPED_HASH_REPLACEMENT = "\\\\@";

    public final int widgetId;
    public final WidgetType widgetType;
    public final List<String> payload;

    // TODO remove me in one of the next versions (when all old widget configurations have been updated!
    @Deprecated
    public final boolean isOld;

    public WidgetConfiguration(int widgetId, WidgetType widgetType, String... payload) {
        this(widgetId, widgetType, asList(payload), false);
    }

    public WidgetConfiguration(int widgetId, WidgetType widgetType, List<String> payload, boolean isOld) {
        this.widgetId = widgetId;
        this.widgetType = widgetType;
        this.payload = payload;
        this.isOld = isOld;
    }

    public static WidgetConfiguration fromSaveString(String value) {
        if (value == null) return null;

        String[] parts = value.split(SAVE_SEPARATOR);

        boolean isDeprecatedWidget = getWidgetTypeFromName(parts[1]) == null;

        if (!isDeprecatedWidget) {
            return handleWidgetConfiguration(parts);
        } else {
            return handleDeprecatedWidgetConfiguration(parts);
        }
    }

    private static WidgetConfiguration handleWidgetConfiguration(String[] parts) {
        String widgetId = parts[0];
        WidgetType widgetType = getWidgetTypeFromName(parts[1]);

        List<String> payload;
        if (parts.length >= 3) {
            payload = Arrays.asList(unescape(parts[2]).split(PAYLOAD_SEPARATOR_REGEXP));
        } else {
            payload = newArrayList();
        }

        return new WidgetConfiguration(Integer.valueOf(widgetId), widgetType, payload, false);
    }

    private static WidgetConfiguration handleDeprecatedWidgetConfiguration(String[] parts) {

        String widgetTypeName = parts[2];
        WidgetType widgetType = getWidgetTypeFromName(widgetTypeName);

        String widgetId = parts[0];

        List<String> payload = newArrayList();
        payload.add(parts[1]);
        if (parts.length == 4) {
            payload.add(unescape(parts[3]));
        }

        return new WidgetConfiguration(Integer.valueOf(widgetId), widgetType, payload, true);
    }

    private static WidgetType getWidgetTypeFromName(String widgetTypeName) {
        try {
            return WidgetType.valueOf(widgetTypeName);
        } catch (Exception e) {
            Log.v(WidgetConfiguration.class.getName(), "cannot find widget type for name " + widgetTypeName, e);
            return null;
        }
    }

    static String escape(String value) {
        if (value == null) return null;
        return value.replaceAll(SAVE_SEPARATOR, ESCAPED_HASH_REPLACEMENT);
    }

    static String unescape(String value) {
        if (value == null) return null;
        return value.replaceAll(ESCAPED_HASH_REPLACEMENT, SAVE_SEPARATOR);
    }

    public String toSaveString() {
        return Joiner.on(SAVE_SEPARATOR).skipNulls().join(widgetId, widgetType.name(),
                escape(payloadAsSaveString()));
    }

    private String payloadAsSaveString() {
        return Joiner.on(PAYLOAD_SEPARATOR).join(payload);
    }

    @Override
    public String toString() {
        return "WidgetConfiguration{" +
                "widgetId=" + widgetId +
                ", widgetType=" + widgetType +
                ", payload=" + payload +
                ", isOld=" + isOld +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WidgetConfiguration && reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }
}