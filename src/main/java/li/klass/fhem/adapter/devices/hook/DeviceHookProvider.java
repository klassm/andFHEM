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

package li.klass.fhem.adapter.devices.hook;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.room.xmllist.DeviceNode;

@Singleton
public class DeviceHookProvider {

    static final String HOOK_ON_OFF = "onOffDevice";
    static final String HOOK_ON = "onDevice";
    static final String HOOK_OFF = "offDevice";
    static final String HOOK_WEBCMD = "webcmdDevice";
    static final String HOOK_TOGGLE = "toggleDevice";

    private static final ImmutableMap<String, ButtonHook> HOOK_MAPPING =
            ImmutableMap.<String, ButtonHook>builder()
                    .put(HOOK_ON_OFF, ButtonHook.ON_OFF_DEVICE)
                    .put(HOOK_ON, ButtonHook.ON_DEVICE)
                    .put(HOOK_OFF, ButtonHook.OFF_DEVICE)
                    .put(HOOK_WEBCMD, ButtonHook.WEBCMD_DEVICE)
                    .put(HOOK_TOGGLE, ButtonHook.TOGGLE_DEVICE)
                    .build();

    @Inject
    public DeviceHookProvider() {
    }

    public ButtonHook buttonHookFor(FhemDevice device) {
        Map<String, DeviceNode> attributes = device.getXmlListDevice().getAttributes();

        for (Map.Entry<String, ButtonHook> entry : HOOK_MAPPING.entrySet()) {
            String key = entry.getKey();
            if (attributes.containsKey(key) &&
                    "true".equalsIgnoreCase(attributes.get(key).getValue())) {
                return entry.getValue();
            }
        }
        return ButtonHook.NORMAL;
    }
}
