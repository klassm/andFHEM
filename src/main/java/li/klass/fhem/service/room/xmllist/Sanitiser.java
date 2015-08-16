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

package li.klass.fhem.service.room.xmllist;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.util.ValueDescriptionUtil;

import static com.google.common.base.Strings.isNullOrEmpty;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.ATTR;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

@Singleton
public class Sanitiser {

    private final JSONObject options;
    private static final Logger LOGGER = LoggerFactory.getLogger(Sanitiser.class);

    @Inject
    public Sanitiser() {
        try {
            options = new JSONObject(Resources.toString(Resources.getResource(Sanitiser.class, "deviceSanitiser.json"), Charsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DeviceNode sanitise(String deviceType, DeviceNode deviceNode) {
        try {
            JSONObject deviceOptions = optionsFor(deviceType);
            return sanitise(deviceNode, deviceOptions);
        } catch (Exception e) {
            LOGGER.error("cannot sanitise {}", deviceNode);
            return deviceNode;
        }
    }

    public void sanitise(String deviceType, XmlListDevice xmlListDevice) {
        try {
            JSONObject typeOptions = options.getJSONObject("deviceTypes").optJSONObject(deviceType);
            if (typeOptions == null) return;

            JSONObject generalOptions = typeOptions.optJSONObject("__general__");
            if (generalOptions == null) return;

            handleGeneralAttributesArray(generalOptions, xmlListDevice);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleGeneralAttributesArray(JSONObject generalOptions, XmlListDevice xmlListDevice) throws JSONException {
        JSONArray attributes = generalOptions.optJSONArray("addAttributesIfNotPresent");
        if (attributes == null) return;

        for (int i = 0; i < attributes.length(); i++) {
            JSONObject object = attributes.getJSONObject(i);
            String key = object.getString("key");
            String value = object.getString("value");

            if (!xmlListDevice.getAttributes().containsKey(key)) {
                xmlListDevice.getAttributes().put(key, new DeviceNode(ATTR, key, value, null));
            }
        }
    }

    private DeviceNode sanitise(DeviceNode deviceNode, JSONObject deviceOptions) {
        JSONObject attributeOptions = deviceOptions.optJSONObject(deviceNode.getKey());
        if (attributeOptions == null) {
            return deviceNode;
        }

        String key = deviceNode.getKey();
        String value = deviceNode.getValue();
        String measured = deviceNode.getMeasured();
        DeviceNode.DeviceNodeType type = deviceNode.getType();

        value = handleExtract(attributeOptions, value);
        value = handleReplace(attributeOptions, value);
        value = handleAppend(attributeOptions, value);

        return new DeviceNode(type, key, value, measured);
    }

    private String handleReplace(JSONObject attributeOptions, String value) {
        String replace = attributeOptions.optString("replace");
        String replaceBy = attributeOptions.optString("replaceBy");
        replaceBy = replaceBy == null ? "" : replaceBy;

        if (!isNullOrEmpty(replace)) {
            value = value.replaceAll(replace, replaceBy);
        }
        return value.trim();
    }

    private String handleAppend(JSONObject attributeOptions, String value) {
        String append = attributeOptions.optString("append");
        if (!isNullOrEmpty(append)) {
            value = ValueDescriptionUtil.append(value, append);
        }
        return value;
    }

    private String handleExtract(JSONObject attributeOptions, String value) {
        String extract = attributeOptions.optString("extract");
        if (!isNullOrEmpty(extract)) {
            switch (extract) {
                case "double":
                    int extractDigits = attributeOptions.optInt("extractDigits", 0);
                    double result = extractDigits != 0 ?
                            extractLeadingDouble(value, extractDigits) :
                            extractLeadingDouble(value);
                    int divFactor = attributeOptions.optInt("extractDivideBy", 0);
                    if (divFactor != 0) {
                        result = Math.round(result / 1000d);
                    }
                    return String.valueOf(result);
                case "int":
                    return String.valueOf(extractLeadingInt(value));
            }
        }
        return value;
    }

    private JSONObject optionsFor(String type) {
        try {
            JSONObject defaults = options.getJSONObject("defaults");
            JSONObject typeOptions = options.getJSONObject("deviceTypes").optJSONObject(type);

            JSONObject result = new JSONObject();
            putAllInto(defaults, result);
            if (typeOptions != null) {
                putAllInto(typeOptions, result);
            }

            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void putAllInto(JSONObject from, JSONObject into) throws JSONException {
        Iterator<String> keys = from.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            into.put(key, from.get(key));
        }
    }
}
