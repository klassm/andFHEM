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

package li.klass.fhem.appwidget.update;

import android.util.Log;

import com.google.common.base.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import li.klass.fhem.appwidget.ui.widget.WidgetType;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class WidgetConfiguration implements Serializable {
    private static final String SAVE_SEPARATOR = "#";
    private static final String ESCAPED_HASH_REPLACEMENT = "\\\\@";
    private static final String JSON_WIDGET_ID = "widgetId";
    private static final String JSON_WIDGET_TYPE = "widgetType";
    private static final String JSON_PAYLOAD = "payload";
    private static final String JSON_CONNECTION_ID = "connectionId";

    public final int widgetId;
    public final WidgetType widgetType;
    public final List<String> payload;
    public final Optional<String> connectionId;

    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetConfiguration.class);

    public WidgetConfiguration(int widgetId, WidgetType widgetType, Optional<String> connectionId, List<String> payload) {
        this.widgetId = widgetId;
        this.widgetType = widgetType;
        this.payload = payload;
        this.connectionId = connectionId;
    }

    public static WidgetConfiguration fromSaveString(String value) {
        if (value == null) return null;

        return handleJsonWidgetConfiguration(value);
    }

    private static WidgetConfiguration handleJsonWidgetConfiguration(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            return new WidgetConfiguration(
                    jsonObject.getInt(JSON_WIDGET_ID),
                    getWidgetTypeFromName(jsonObject.getString(JSON_WIDGET_TYPE)),
                    getConnectionIdFrom(jsonObject),
                    payloadToList(jsonObject)
            );
        } catch (JSONException e) {
            LOGGER.error("handleJsonWidgetConfiguration - cannot handle \"{}\"", value);
            return null;
        }
    }

    private static Optional<String> getConnectionIdFrom(JSONObject jsonObject) {
        return Optional.fromNullable(StringUtils.trimToNull(jsonObject.optString(JSON_CONNECTION_ID)));
    }

    private static List<String> payloadToList(JSONObject jsonObject) throws JSONException {
        JSONArray array = jsonObject.getJSONArray(JSON_PAYLOAD);
        List<String> payload = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            payload.add(array.getString(i));
        }
        return payload;
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
                    .put(JSON_CONNECTION_ID, connectionId.orNull())
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
                ", connectionId=" + connectionId +
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