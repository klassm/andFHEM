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

package li.klass.fhem.adapter.devices.core.deviceItems;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.DeviceConfigurationProvider;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static com.google.common.collect.Sets.newHashSet;

@Singleton
public class XmlDeviceItemProvider {
    @Inject
    DeviceConfigurationProvider deviceConfigurationProvider;

    public Set<DeviceViewItem> getDeviceClassItems(XmlListDevice xmlListDevice) {
        Set<DeviceViewItem> items = newHashSet();
        if (xmlListDevice == null) return items;

        try {
            Optional<JSONObject> optConfig = deviceConfigurationProvider.configurationFor(xmlListDevice);
            if (!optConfig.isPresent()) {
                return items;
            }

            items.addAll(statesFor(xmlListDevice, optConfig.get()));
            items.addAll(attributesFor(xmlListDevice, optConfig.get()));

            return items;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<DeviceViewItem> statesFor(XmlListDevice device, JSONObject jsonObject) throws JSONException {
        Set<DeviceViewItem> items = newHashSet();

        JSONArray states = jsonObject.optJSONArray("states");
        if (states == null) {
            return items;
        }

        for (int i = 0; i < states.length(); i++) {
            JSONObject state = states.getJSONObject(i);
            items.add(itemFor(state, device.getStates()));
        }

        return items;
    }

    private Set<DeviceViewItem> attributesFor(XmlListDevice device, JSONObject jsonObject) throws JSONException {
        Set<DeviceViewItem> items = newHashSet();

        JSONArray attributes = jsonObject.optJSONArray("attributes");
        if (attributes == null) {
            return items;
        }

        for (int i = 0; i < attributes.length(); i++) {
            JSONObject attribute = attributes.getJSONObject(i);
            items.add(itemFor(attribute, device.getAttributes()));
        }

        return items;
    }

    private XmlDeviceViewItem itemFor(JSONObject object, Map<String, DeviceNode> valueMap) throws JSONException {
        String key = object.getString("key");
        String desc = object.getString("desc");
        String showAfter = object.optString("showAfter");
        boolean showInOverview = object.optBoolean("showInOverview", false);
        boolean showInDetail = object.optBoolean("showInDetail", true);
        String value = valueMap.get(key).getValue();

        return new XmlDeviceViewItem(key, value, showAfter, showInDetail, showInOverview, ResourceIdMapper.valueOf(desc));
    }
}
