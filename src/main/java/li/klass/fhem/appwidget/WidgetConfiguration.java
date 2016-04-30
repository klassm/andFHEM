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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
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
    public static final String JSON_WIDGET_ID = "widgetId";
    public static final String JSON_WIDGET_TYPE = "widgetType";
    public static final String JSON_PAYLOAD = "payload";

    public final int widgetId;
    public final WidgetType widgetType;
    public final List<String> payload;

    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetConfiguration.class);

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

        if (value.startsWith("{")) {
            return handleJsonWidgetConfiguration(value);
        } else {
            return handleDeprecatedWidgetConfiguration(value);
        }
    }

    private static WidgetConfiguration handleJsonWidgetConfiguration(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            return new WidgetConfiguration(
                    jsonObject.getInt(JSON_WIDGET_ID),
                    getWidgetTypeFromName(jsonObject.getString(JSON_WIDGET_TYPE)),
                    payloadToList(jsonObject),
                    false
            );
        } catch (JSONException e) {
            LOGGER.error("handleJsonWidgetConfiguration - cannot handle \"{}\"", value);
            return null;
        }
    }

    private static List<String> payloadToList(JSONObject jsonObject) throws JSONException {
        JSONArray array = jsonObject.getJSONArray(JSON_PAYLOAD);
        List<String> payload = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            payload.add(array.getString(i));
        }
        return payload;
    }

    private static WidgetConfiguration handleDeprecatedWidgetConfiguration(String value) {

        String[] parts = value.split(SAVE_SEPARATOR);

        String widgetId = parts[0];
        WidgetType widgetType = getWidgetTypeFromName(parts[1]);

        List<String> payload;
        if (parts.length >= 3) {
            payload = Arrays.asList(unescape(parts[2]).split(PAYLOAD_SEPARATOR_REGEXP));
        } else {
            payload = newArrayList();
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
        JSONObject jsonObject = new JSONObject();
        try {
            return jsonObject
                    .put(JSON_WIDGET_ID, widgetId)
                    .put(JSON_WIDGET_TYPE, widgetType)
                    .put(JSON_PAYLOAD, new JSONArray(payload))
                    .toString();
        } catch (JSONException e) {
            LOGGER.error("cannot create widget configuration", e);
            return null;
        }
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