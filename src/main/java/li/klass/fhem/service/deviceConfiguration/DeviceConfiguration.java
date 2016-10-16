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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import li.klass.fhem.domain.core.DeviceFunctionality;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;

public class DeviceConfiguration implements Serializable {
    public static final Function<DeviceConfiguration, Integer> TO_DELAY_FOR_UPDATE_AFTER_COMMAND = new Function<DeviceConfiguration, Integer>() {
        @Override
        public Integer apply(DeviceConfiguration input) {
            return input.getDelayForUpdateAfterCommand();
        }
    };

    private final DeviceFunctionality defaultGroup;
    private final boolean sensorDevice;
    private final Set<String> supportedWidgets;
    private final Set<ViewItemConfig> states;
    private final Set<ViewItemConfig> attributes;
    private final Set<ViewItemConfig> internals;
    private final boolean showStateInOverview;
    private final boolean showMeasuredInOverview;
    private final Map<String, Map<String, String>> stateCommandReplace;
    private final Map<String, String> subStateReplace;
    private final int delayForUpdateAfterCommand;

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
        subStateReplace = builder.subStateReplace;
        delayForUpdateAfterCommand = builder.delayForUpdateAfterCommand;
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

    public Optional<ViewItemConfig> stateConfigFor(final String key) {
        return from(getStates()).firstMatch(new Predicate<ViewItemConfig>() {
            @Override
            public boolean apply(ViewItemConfig input) {
                return input.getKey().equalsIgnoreCase(key);
            }
        });
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

    public int getDelayForUpdateAfterCommand() {
        return delayForUpdateAfterCommand;
    }

    public Optional<String> getSubStateReplaceFor(String key) {
        return Optional.fromNullable(StringUtils.trimToNull(subStateReplace.get(key)));
    }

    public Map<String, String> getCommandReplaceFor(String state) {
        state = state.toUpperCase(Locale.getDefault());
        if (stateCommandReplace.containsKey(state)) {
            return stateCommandReplace.get(state);
        }
        return Collections.emptyMap();
    }

    public static final class Builder {
        private DeviceFunctionality defaultGroup = DeviceFunctionality.UNKNOWN;
        private boolean sensorDevice = false;
        private Set<ViewItemConfig> states = Sets.newHashSet();
        private Set<ViewItemConfig> attributes = Sets.newHashSet();
        private Set<ViewItemConfig> internals = Sets.newHashSet();
        private Set<String> supportedWidgets = Sets.newHashSet();
        private boolean showStateInOverview;
        private boolean showMeasuredInOverview;
        private Map<String, Map<String, String>> stateCommandReplace = newHashMap();
        private Map<String, String> subStateReplace = new HashMap<>();
        private int delayForUpdateAfterCommand = 0;

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

        public Builder withSubStateReplace(String state, String value) {
            value = StringUtils.trimToNull(value);
            if (value != null) {
                this.subStateReplace.put(state, value);
            }
            return this;
        }

        public Builder withDelayForUpdateAfterCommand(int delay) {
            this.delayForUpdateAfterCommand = delay;
            return this;
        }

        public DeviceConfiguration build() {
            return new DeviceConfiguration(this);
        }
    }
}
