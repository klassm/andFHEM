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

package li.klass.fhem.domain;

import java.util.Map;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static com.google.common.collect.Maps.newHashMap;

public class PCA9532Device extends FhemDevice<PCA9532Device> {

    private Map<String, Boolean> portsIsOnMap = newHashMap();

    @XmllistAttribute("pwm0")
    private int pwm0;

    @XmllistAttribute("pwm1")
    private int pwm1;

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        if (key.matches("Port[0-9]+")) {
            portsIsOnMap.put(key, value.equalsIgnoreCase("on") || value.equalsIgnoreCase("1"));
        }
    }

    @ShowField(description = ResourceIdMapper.pwm0)
    public int getPwm0() {
        return pwm0;
    }

    @ShowField(description = ResourceIdMapper.pwm1)
    public int getPwm1() {
        return pwm1;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.SWITCH;
    }

    public Map<String, Boolean> getPortsIsOnMap() {
        return portsIsOnMap;
    }
}
