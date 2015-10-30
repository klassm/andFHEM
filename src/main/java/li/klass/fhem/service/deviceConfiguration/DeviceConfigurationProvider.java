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

package li.klass.fhem.service.deviceConfiguration;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@Singleton
public class DeviceConfigurationProvider {
    public static final String SUPPORTED_WIDGETS = "supportedWidgets";
    public static final String DEFAULT_GROUP = "defaultGroup";
    public static final String SENSOR_DEVICE = "sensorDevice";
    public static final String STATES = "states";
    public static final String SHOW_IN_OVERVIEW = "showInOverview";
    public static final String DESC = "desc";
    public static final String KEY = "key";
    public static final String MARKERS = "markers";
    private static final String SHOW_STATE_IN_OVERVIEW = "showStateInOverview";
    private static final String SHOW_MEASURED_IN_OVERVIEW = "showMeasuredInOverview";
    public static final String SHOW_AFTER = "showAfter";
    private static final String ATTRIBUTES = "attributes";
    private static final String INTERNALS = "internals";
    private final JSONObject options;

    @Inject
    public DeviceConfigurationProvider() {
        try {
            options = new JSONObject(Resources.toString(Resources.getResource(DeviceConfigurationProvider.class, "deviceConfiguration.json"), Charsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<JSONObject> plainConfigurationFor(XmlListDevice device) {
        return plainConfigurationFor(device.getType());
    }

    public Optional<JSONObject> plainConfigurationFor(String type) {
        JSONObject deviceConfig = options.optJSONObject(type);

        return deviceConfig == null ? Optional.<JSONObject>absent() : Optional.of(deviceConfig);
    }


    public Optional<DeviceConfiguration> configurationFor(FhemDevice<?> device) {
        return configurationFor(device.getXmlListDevice());
    }

    public Optional<DeviceConfiguration> configurationFor(XmlListDevice device) {
        return configurationFor(device.getType());
    }

    public Optional<DeviceConfiguration> configurationFor(String type) {
        Optional<JSONObject> configOpt = plainConfigurationFor(type);
        if (!configOpt.isPresent()) {
            return Optional.absent();
        }

        JSONObject jsonObject = configOpt.get();
        String defaultGroupValue = trimToNull(jsonObject.optString(DEFAULT_GROUP));
        DeviceFunctionality defaultGroup = defaultGroupValue != null ? DeviceFunctionality.valueOf(defaultGroupValue) : DeviceFunctionality.UNKNOWN;

        DeviceConfiguration.Builder builder = new DeviceConfiguration.Builder()
                .withSensorDevice(jsonObject.optBoolean(SENSOR_DEVICE, false))
                .withDefaultGroup(defaultGroup)
                .withSupportedWidgets(transformStringJsonArray(jsonObject.optJSONArray(SUPPORTED_WIDGETS)))
                .withShowStateInOverview(jsonObject.optBoolean(SHOW_STATE_IN_OVERVIEW, true))
                .withShowMeasuredInOverview(jsonObject.optBoolean(SHOW_MEASURED_IN_OVERVIEW, true));

        Optional<JSONObject> defaults = plainConfigurationFor("defaults");
        if (defaults.isPresent()) {
            addFields(defaults.get(), builder);
        }

        addFields(jsonObject, builder);

        return Optional.of(builder.build());
    }

    private void addFields(JSONObject jsonObject, DeviceConfiguration.Builder builder) {
        addStates(jsonObject, builder);
        addAttributes(jsonObject, builder);
        addInternals(jsonObject, builder);
    }

    private void addAttributes(JSONObject jsonObject, DeviceConfiguration.Builder builder) {
        JSONArray attributes = jsonObject.optJSONArray(ATTRIBUTES);
        if (attributes != null) {
            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.optJSONObject(i);

                builder.withAttribute(new DeviceConfiguration.ViewItemConfig(attribute.optString(KEY), attribute.optString(DESC), attribute.optString(SHOW_AFTER),
                        attribute.optBoolean(SHOW_IN_OVERVIEW, false), attribute.optBoolean("showInDetail", true),
                        transformStringJsonArray(attribute.optJSONArray(MARKERS))));
            }
        }
    }

    private void addStates(JSONObject jsonObject, DeviceConfiguration.Builder builder) {
        JSONArray states = jsonObject.optJSONArray(STATES);
        if (states != null) {
            for (int i = 0; i < states.length(); i++) {
                JSONObject state = states.optJSONObject(i);

                builder.withState(new DeviceConfiguration.ViewItemConfig(state.optString(KEY), state.optString(DESC), state.optString(SHOW_AFTER),
                        state.optBoolean(SHOW_IN_OVERVIEW, false), state.optBoolean("showInDetail", true),
                        transformStringJsonArray(state.optJSONArray(MARKERS))));
            }
        }
    }

    private void addInternals(JSONObject jsonObject, DeviceConfiguration.Builder builder) {
        JSONArray internals = jsonObject.optJSONArray(INTERNALS);
        if (internals != null) {
            for (int i = 0; i < internals.length(); i++) {
                JSONObject internal = internals.optJSONObject(i);

                builder.withInternal(new DeviceConfiguration.ViewItemConfig(internal.optString(KEY), internal.optString(DESC), internal.optString(SHOW_AFTER),
                        internal.optBoolean(SHOW_IN_OVERVIEW, false), internal.optBoolean("showInDetail", true),
                        transformStringJsonArray(internal.optJSONArray(MARKERS))));
            }
        }
    }

    private Set<String> transformStringJsonArray(JSONArray array) {
        if (array == null) return emptySet();

        Set<String> markersResult = newHashSet();
        for (int i = 0; i < array.length(); i++) {
            markersResult.add(array.optString(i));
        }

        return markersResult;
    }

    public boolean isSensorDevice(XmlListDevice xmlListDevice) {
        Optional<JSONObject> configOpt = plainConfigurationFor(xmlListDevice);
        return configOpt.isPresent() && configOpt.get().optBoolean(SENSOR_DEVICE, false);
    }
}
