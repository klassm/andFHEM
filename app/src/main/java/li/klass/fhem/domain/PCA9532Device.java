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

import org.w3c.dom.NamedNodeMap;

import java.util.Map;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;

import static com.google.common.collect.Maps.newHashMap;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

public class PCA9532Device extends Device<PCA9532Device> {

    private Map<String, Boolean> portsIsOnMap = newHashMap();

    private int pwm0;
    private int pwm1;

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);

        if (key.matches("PORT[0-9]+")) {
            portsIsOnMap.put(
                    key.replace("PORT", "Port"),
                    value.equalsIgnoreCase("on") || value.equalsIgnoreCase("1"));
        }
    }

    public void readPWM0(String value) {
        pwm0 = extractLeadingInt(value);
    }

    public void readPWM1(String value) {
        pwm1 = extractLeadingInt(value);
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

    public void setPortState(String port, boolean portState) {
        portsIsOnMap.put(port, portState);
    }
}
