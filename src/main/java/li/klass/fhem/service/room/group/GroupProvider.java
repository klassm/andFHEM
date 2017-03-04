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

package li.klass.fhem.service.room.group;

import android.content.Context;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.room.group.device.DeviceGroupProvider;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static li.klass.fhem.behavior.dim.DimmableBehavior.isDimDisabled;

@Singleton
public class GroupProvider {
    private static final Function<DeviceGroupProvider, String> TO_KEY = new Function<DeviceGroupProvider, String>() {
        @Override
        public String apply(DeviceGroupProvider input) {
            return input.getDeviceType();
        }
    };
    private Map<String, DeviceGroupProvider> providerMap;

    @Inject
    public GroupProvider(Set<DeviceGroupProvider> deviceGroupProviders) {
        this.providerMap = Maps.uniqueIndex(deviceGroupProviders, TO_KEY);
    }

    public String functionalityFor(FhemDevice device, Context context) {
        XmlListDevice xmlListDevice = device.getXmlListDevice();
        Optional<String> group = xmlListDevice.getAttribute("group");
        if (group.isPresent()) {
            return group.get();
        }

        if (providerMap.containsKey(xmlListDevice.getType())) {
            Optional<String> providerGroup = providerMap.get(xmlListDevice.getType())
                    .groupFor(xmlListDevice, context);
            if (providerGroup.isPresent()) {
                return providerGroup.get();
            }
        }

        DeviceFunctionality functionality = DeviceFunctionality.UNKNOWN;
        if (DimmableBehavior.behaviorFor(device, null).isPresent() && !isDimDisabled(device)) {
            functionality = DeviceFunctionality.DIMMER;
        } else if (OnOffBehavior.supports(device)) {
            functionality = DeviceFunctionality.SWITCH;
        } else if (device.getDeviceConfiguration().isPresent()) {
            functionality = device.getDeviceConfiguration().get().getDefaultGroup();
        }

        return functionality.getCaptionText(context);
    }
}
