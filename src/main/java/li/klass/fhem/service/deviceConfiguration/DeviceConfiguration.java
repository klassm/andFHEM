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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import li.klass.fhem.domain.core.DeviceFunctionality;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class DeviceConfiguration implements Serializable {
    private DeviceFunctionality defaultGroup;
    private boolean sensorDevice;
    private final Set<String> supportedWidgets;
    private Set<ViewItemConfig> states;
    private Set<ViewItemConfig> attributes;
    private Set<ViewItemConfig> internals;
    private boolean showStateInOverview;
    private boolean showMeasuredInOverview;
    private Map<String, Map<String, String>> stateCommandReplace;

    private DeviceConfiguration(Builder builder) {
        defaultGroup = checkNotNull(builder.defaultGroup);
        states = checkNotNull(builder.states);
        attributes = checkNotNull(builder.attributes);
        internals = checkNotNull(builder.internals);
        supportedWidgets = checkNotNull(builder.supportedWidgets);
        sensorDevice = builder.sensorDevice;
        showStateInOverview = builder.showStateInOverview;
        showMeasuredInOverview = builder.showMeasuredInOverview;
        stateCommandReplace = ImmutableMap.copyOf(builder.stateCommandReplace);
    }

    public DeviceFunctionality getDefaultGroup() {
        return defaultGroup;
    }

    public boolean isSensorDevice() {
        return sensorDevice;
    }

    public Set<ViewItemConfig> getStates() {
        return states;
    }

    public Set<ViewItemConfig> getAttributes() {
        return attributes;
    }

    public Set<ViewItemConfig> getInternals() {
        return internals;
    }

    public Set<String> getSupportedWidgets() {
        return supportedWidgets;
    }

    public boolean isShowStateInOverview() {
        return showStateInOverview;
    }

    public boolean isShowMeasuredInOverview() {
        return showMeasuredInOverview;
    }

    public Map<String, String> getCommandReplaceFor(String state) {
        state = state.toUpperCase(Locale.getDefault());
        if (stateCommandReplace.containsKey(state)) {
            return stateCommandReplace.get(state);
        }
        return Collections.emptyMap();
    }

    public static class ViewItemConfig implements Serializable {
        private final Set<String> markers;
        String key;
        String desc;
        String showAfter;
        boolean showInOverview = false;
        boolean showInDetail = false;

        public ViewItemConfig(String key, String desc, String showAfter, boolean showInOverview, boolean showInDetail, Set<String> markers) {
            this.key = checkNotNull(key);
            this.desc = checkNotNull(desc);
            this.markers = checkNotNull(markers);
            this.showInOverview = showInOverview;
            this.showAfter = showAfter;
            this.showInDetail = showInDetail;
        }

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }

        public boolean isShowInOverview() {
            return showInOverview;
        }

        public Set<String> getMarkers() {
            return markers;
        }

        public String getShowAfter() {
            return showAfter;
        }

        public boolean isShowInDetail() {
            return showInDetail;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ViewItemConfig state = (ViewItemConfig) o;

            return !(key != null ? !key.equals(state.key) : state.key != null);

        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }

    public static final class Builder {
        private DeviceFunctionality defaultGroup;
        private boolean sensorDevice = false;
        private Set<ViewItemConfig> states = Sets.newHashSet();
        private Set<ViewItemConfig> attributes = Sets.newHashSet();
        private Set<ViewItemConfig> internals = Sets.newHashSet();
        private Set<String> supportedWidgets = Sets.newHashSet();
        private boolean showStateInOverview;
        private boolean showMeasuredInOverview;
        private Map<String, Map<String, String>> stateCommandReplace = newHashMap();

        public Builder() {
        }

        public Builder withDefaultGroup(DeviceFunctionality defaultGroup) {
            this.defaultGroup = defaultGroup;
            return this;
        }

        public Builder withSensorDevice(boolean sensorDevice) {
            this.sensorDevice = sensorDevice;
            return this;
        }

        public Builder withState(ViewItemConfig state) {
            this.states.add(state);
            return this;
        }

        public Builder withAttribute(ViewItemConfig attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public Builder withInternal(ViewItemConfig internal) {
            this.internals.add(internal);
            return this;
        }

        public Builder withSupportedWidgets(Set<String> supportedWidgets) {
            this.supportedWidgets = supportedWidgets;
            return this;
        }

        public Builder withShowStateInOverview(boolean showStateInOverview) {
            this.showStateInOverview = showStateInOverview;
            return this;
        }

        public Builder withShowMeasuredInOverview(boolean showMeasuredInOverview) {
            this.showMeasuredInOverview = showMeasuredInOverview;
            return this;
        }

        public Builder withCommandReplace(String state, Map<String, String> commandReplace) {
            stateCommandReplace.put(state.toUpperCase(Locale.getDefault()), ImmutableMap.copyOf(commandReplace));
            return this;
        }

        public DeviceConfiguration build() {
            return new DeviceConfiguration(this);
        }
    }
}
