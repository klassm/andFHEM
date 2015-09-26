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

package li.klass.fhem.adapter.devices.core.generic.detail.actions;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.StateAttributeAction;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

public abstract class DeviceDetailActionProvider implements GenericDetailActionProvider {
    private Map<String, StateAttributeAction> stateAttributeActionMap = Maps.newHashMap();

    @Override
    public boolean supports(XmlListDevice xmlListDevice) {
        return xmlListDevice.getType().equalsIgnoreCase(getDeviceType());
    }

    protected abstract String getDeviceType();

    @Override
    public Optional<StateAttributeAction> stateAttributeActionFor(DeviceViewItem item) {
        String key = item.getSortKey().toLowerCase(Locale.getDefault());
        if (stateAttributeActionMap.containsKey(key)) {
            return Optional.of(stateAttributeActionMap.get(key));
        }
        return Optional.absent();
    }

    protected void addStateAttributeAction(String key, StateAttributeAction stateAttributeAction) {
        stateAttributeActionMap.put(key, stateAttributeAction);
    }
}
